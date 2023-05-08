package com.yqhp.agent.driver;

import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.devicediscovery.ios.IOSDevice;
import com.yqhp.console.repository.enums.ViewType;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
public abstract class IOSDeviceDriver extends DeviceDriver {

    public IOSDeviceDriver(IOSDevice iosDevice) {
        super(iosDevice);
    }

    @Override
    public void installApp(File app) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ViewType viewType() {
        return isNativeContext() ? ViewType.iOS_NATIVE : ViewType.iOS_WEB;
    }

    @Override
    public File screenshot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void receiveDeviceLog(Consumer<String> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopReceiveDeviceLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IOSDriver newAppiumDriver(URL appiumServiceURL, DesiredCapabilities capabilities) {
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        capabilities.setCapability(MobileCapabilityType.UDID, device.getId());

        if (capabilities.getCapability(MobileCapabilityType.AUTOMATION_NAME) == null) {
            // 默认XCuiTest
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
            // 本地端口 -> 设备wda服务端口
            capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, LocalPortProvider.getAppiumIOSWdaAvailablePort());
        }

        return new IOSDriver(appiumServiceURL, capabilities);
    }
}
