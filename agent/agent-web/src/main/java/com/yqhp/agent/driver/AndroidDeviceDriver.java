package com.yqhp.agent.driver;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import com.yqhp.agent.androidtools.AndroidUtils;
import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.devicediscovery.android.AndroidDevice;
import com.yqhp.agent.scrcpy.Scrcpy;
import com.yqhp.common.commons.model.Size;
import com.yqhp.console.repository.enums.ViewType;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    private LogCatListener logcatListener;

    @Getter
    private final Scrcpy scrcpy;

    public AndroidDeviceDriver(AndroidDevice androidDevice) {
        super(androidDevice);
        this.scrcpy = new Scrcpy(androidDevice.getIDevice());
    }

    @Override
    public void installApp(File app) {
        AndroidUtils.installApp(getIDevice(), app);
    }

    @Override
    protected ViewType viewType() {
        return isNativeContext() ? ViewType.ANDROID_NATIVE : ViewType.ANDROID_WEB;
    }

    @Override
    public File screenshot() throws IOException {
        return AndroidUtils.screenshot(getIDevice());
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
    protected AndroidDriver newAppiumDriver(URL appiumServiceURL, DesiredCapabilities capabilities) {
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.UDID, device.getId());

        String automationName = (String) capabilities.getCapability(MobileCapabilityType.AUTOMATION_NAME);
        if (automationName == null) {
            // 默认uiautomator2
            automationName = AutomationName.ANDROID_UIAUTOMATOR2;
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, automationName);
        }

        // 本地端口 -> 设备uiautomator2/espresso服务端口
        capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, LocalPortProvider.getAppiumAndroidSystemAvailablePort());

        if (capabilities.getCapability(AndroidMobileCapabilityType.SKIP_LOGCAT_CAPTURE) == null) {
            // appium默认会执行logcat，关闭logcat捕获提升性能
            capabilities.setCapability(AndroidMobileCapabilityType.SKIP_LOGCAT_CAPTURE, true);
        }
        return new AndroidDriver(appiumServiceURL, capabilities);
    }

    public IDevice getIDevice() {
        return ((AndroidDevice) device).getIDevice();
    }

    @Override
    public synchronized void receiveDeviceLog(Consumer<String> consumer) {
        if (logcatReceiverTask != null) {
            throw new IllegalStateException("receiving");
        }

        log.info("[{}]receive device log", device.getId());
        this.logcatListener = messages -> {
            for (LogCatMessage message : messages) {
                consumer.accept(message.toString());
            }
        };
        logcatReceiverTask = new LogCatReceiverTask(getIDevice());
        logcatReceiverTask.addLogCatListener(logcatListener);
        LOGCAT_RECEIVER_TASK_THREAD_POOL.submit(logcatReceiverTask);
    }

    @Override
    public synchronized void stopReceiveDeviceLog() {
        if (logcatReceiverTask != null) {
            log.info("[{}]stop receive device log", device.getId());
            logcatReceiverTask.removeLogCatListener(logcatListener);
            logcatListener = null;
            logcatReceiverTask.stop();
            logcatReceiverTask = null;
        }
    }

    @Override
    public void release() {
        super.release();
        scrcpy.stop();
    }
}
