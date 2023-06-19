## 初始化

### 方式1

```groovy
import com.yqhp.plugin.appium.*;

var d = new AppiumDriverWrapper(yqhp.appiumDriver());
```

### 方式2（推荐）

> 扩展AppiumDriverWrapper，根据团队需要，提供更多API

```groovy
import com.yqhp.plugin.appium.*;

class CustomAppiumDriver extends AppiumDriverWrapper {

    CustomAppiumDriver() {
        super(yqhp.appiumDriver());
    }

    void pressAndroidKey(AndroidKey key) {
        androidDriver().pressKey(new KeyEvent(key));
    }

    void pressHome() {
        pressAndroidKey(AndroidKey.HOME);
    }

    void pressBack() {
        pressAndroidKey(AndroidKey.BACK);
    }

    /**
     * 停止app
     * 
     * @param appId  android: package name | ios: bundle id
     */
    boolean stopApp(String appId) {
        return ((InteractsWithApps) driver()).terminateApp(appId);
    }

    /**
     * 启动app
     * 
     * @param appId  android: package name | ios: bundle id
     */
    void startApp(String appId) {
        ((InteractsWithApps) driver()).activateApp(appId);
    }

    /**
     * 如果app不存在则安装
     * 
     * @param appUri url or filePath
     * @param appId  android: package name | ios: bundle id
     */
    void installAppIfAbsent(String appUri, String appId) {
        boolean isAppInstalled = ((InteractsWithApps) driver()).isAppInstalled(appId);
        if (!isAppInstalled) {
            yqhp.installApp(appUri);
        }
    }

    /**
     * 清除apk数据, 相当于重新安装了app
     *
     * @param pkg  package name
     */
    void clearApkData(String pkg) {
        yqhp.androidShell("pm clear " + pkg);
    }
}

var d = new CustomAppiumDriver();
```

## API

[查看](https://github.com/yqhp/yqhp/blob/main/agent/plugins/appium/src/main/java/com/yqhp/plugin/appium/AppiumDriverWrapper.java)
 
