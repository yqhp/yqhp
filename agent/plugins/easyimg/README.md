## 初始化

```groovy
import com.yqhp.plugin.easyimg.*;

EasyImg img = new EasyImg(device.getIDevice());
img.setAppiumDriver(device.appiumDriver()); // 可以不设置，但截图速度比较慢
img.setImgDir("download"); // 远程图片存放路径
```

## API

[查看](https://github.com/yqhp/yqhp/blob/main/agent/plugins/easyimg/src/main/java/com/yqhp/plugin/easyimg/EasyImg.java)