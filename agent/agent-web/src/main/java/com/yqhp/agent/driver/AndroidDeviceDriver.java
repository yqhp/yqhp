/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.agent.driver;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import com.yqhp.agent.androidtools.AndroidUtils;
import com.yqhp.agent.androidtools.browser.Browser;
import com.yqhp.agent.androidtools.browser.ChromeDevtools;
import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.devicediscovery.android.AndroidDevice;
import com.yqhp.agent.scrcpy.Scrcpy;
import com.yqhp.common.commons.model.Size;
import com.yqhp.console.repository.enums.ViewType;
import io.appium.java_client.Setting;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public class AndroidDeviceDriver extends DeviceDriver {

    private static final ExecutorService LOGCAT_RECEIVER_TASK_THREAD_POOL = Executors.newCachedThreadPool();
    private LogCatReceiverTask logcatReceiverTask;

    @Getter
    private final Scrcpy scrcpy;

    public AndroidDeviceDriver(AndroidDevice androidDevice) {
        super(androidDevice);
        this.scrcpy = new Scrcpy(androidDevice.getIDevice());
    }

    @Override
    public void installApp(File app, String... extraArgs) {
        AndroidUtils.installApp(getIDevice(), app, extraArgs);
    }

    @Override
    protected ViewType viewType() {
        return ViewType.ANDROID_NATIVE;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        IDevice iDevice = getIDevice();

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setBrand(AndroidUtils.getBrand(iDevice));
        deviceInfo.setManufacturer(AndroidUtils.getManufacturer(iDevice));
        deviceInfo.setMemSize(AndroidUtils.getMemSizeKB(iDevice));
        deviceInfo.setModel(AndroidUtils.getModel(iDevice));
        deviceInfo.setSystemVersion(AndroidUtils.getSystemVersion(iDevice));

        Size physicalSize = AndroidUtils.getPhysicalSize(iDevice);
        deviceInfo.setScreenWidth(physicalSize.width);
        deviceInfo.setScreenHeight(physicalSize.height);

        return deviceInfo;
    }

    @Override
    protected RemoteWebDriver newWebDriver() {
        // https://github.com/appium/appium-uiautomator2-driver
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.UDID, device.getId());
        // 本地端口 -> 设备uiautomator2/espresso服务端口
        capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, LocalPortProvider.getAndroidSystemAvailablePort());
        // webview 本地端口 -> devtools communication
        capabilities.setCapability(AndroidMobileCapabilityType.WEBVIEW_DEVTOOLS_PORT, LocalPortProvider.getAndroidWebviewDevtoolsAvailablePort());
        // webview 启动chromedriver时 --port参数
        capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_PORT, LocalPortProvider.getAndroidChromeDriverAvailablePort());

        if (capabilities.getCapability(AndroidMobileCapabilityType.RECREATE_CHROME_DRIVER_SESSIONS) == null) {
            // webview切换到native时，kill chromedriver
            capabilities.setCapability(AndroidMobileCapabilityType.RECREATE_CHROME_DRIVER_SESSIONS, true);
        }
        if (capabilities.getCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT) == null) {
            capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 60 * 24); // seconds
        }
        if (capabilities.getCapability(MobileCapabilityType.AUTOMATION_NAME) == null) {
            // 默认uiautomator2
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
        }
        if (capabilities.getCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS) == null) {
            capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
        }
        if (capabilities.getCapability(AndroidMobileCapabilityType.SKIP_LOGCAT_CAPTURE) == null) {
            // appium默认会执行logcat，关闭logcat捕获提升性能
            capabilities.setCapability(AndroidMobileCapabilityType.SKIP_LOGCAT_CAPTURE, true);
        }

        AndroidDriver androidDriver = new AndroidDriver(getOrStartDriverService().getUrl(), capabilities);
        // appium-uiautomator2-server在很多地方加了Device.waitForIdle()，默认10秒
        // 导致设备在动态变化的时候很慢，如：点击，获取布局信息等
        // 设置waitForIdle超时时间为0，可以加速执行速度
        androidDriver.setSetting(Setting.WAIT_FOR_IDLE_TIMEOUT, 0);
        return androidDriver;
    }

    public IDevice getIDevice() {
        return ((AndroidDevice) device).getIDevice();
    }

    @Override
    public synchronized void receiveDeviceLog(Consumer<String> consumer) {
        if (logcatReceiverTask != null) {
            throw new IllegalStateException("Receiving");
        }

        log.info("[{}]Receive deviceLog", device.getId());
        logcatReceiverTask = new LogCatReceiverTask(getIDevice());
        logcatReceiverTask.addLogCatListener(messages -> {
            for (LogCatMessage message : messages) {
                consumer.accept(message.toString());
            }
        });
        LOGCAT_RECEIVER_TASK_THREAD_POOL.submit(logcatReceiverTask);
    }

    @Override
    public synchronized void stopReceiveDeviceLog() {
        if (logcatReceiverTask != null) {
            log.info("[{}]Stop to receive deviceLog", device.getId());
            logcatReceiverTask.stop();
            logcatReceiverTask = null;
        }
    }

    public List<Browser> listBrowser() {
        return ChromeDevtools.listBrowser(getIDevice());
    }

    @Override
    public void release() {
        super.release();
        scrcpy.stop();
    }
}
