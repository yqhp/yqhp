package com.yqhp.agent.androidtools;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.RawImage;
import com.yqhp.common.commons.model.Size;
import com.yqhp.common.commons.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jiangyitao
 */
@Slf4j
public class AndroidUtils {

    public static void installAppFromUrl(IDevice iDevice, String apkUrl) throws IOException {
        File apkFile = FileUtils.downloadAsTempFile(apkUrl);
        try {
            installApp(iDevice, apkFile);
        } finally {
            if (!apkFile.delete()) {
                log.warn("delete {} fail", apkFile);
            }
        }
    }

    public static void installApp(IDevice iDevice, File apkFile) {
        installApp(iDevice, apkFile, true);
    }

    public static void installApp(IDevice iDevice, File apkFile, boolean reinstall, String... extraArgs) {
        try {
            iDevice.installPackage(apkFile.getAbsolutePath(), reinstall, extraArgs);
        } catch (InstallException e) {
            throw new InstallAppException(e);
        }
    }

    public static File screenshot(IDevice iDevice) throws IOException {
        File imgFile = Files.createTempFile(null, ".png").toFile();
        screenshot(iDevice, "png", imgFile);
        return imgFile;
    }

    public static void screenshot(IDevice iDevice, String formatName, File imgFile) throws IOException {
        RawImage rawImg;
        try {
            rawImg = iDevice.getScreenshot();
        } catch (Exception e) {
            throw new ScreenshotException(e);
        }

        BufferedImage image = rawImg.asBufferedImage();
        ImageIO.write(image, formatName, imgFile);
    }

    public static String getSystemVersion(IDevice iDevice) {
        return iDevice.getProperty(IDevice.PROP_BUILD_VERSION);
    }

    /**
     * ??????
     */
    public static String getBrand(IDevice iDevice) {
        return iDevice.getProperty("ro.product.brand");
    }

    /**
     * ????????????
     */
    public static String getModel(IDevice iDevice) {
        return iDevice.getProperty(IDevice.PROP_DEVICE_MODEL);
    }

    /**
     * ?????????
     */
    public static String getManufacturer(IDevice iDevice) {
        return iDevice.getProperty(IDevice.PROP_DEVICE_MANUFACTURER);
    }

    /**
     * ??????????????????
     */
    public static Long getMemSizeKB(IDevice iDevice) {
        String memInfo = executeShellCommand(iDevice, "cat /proc/meminfo |grep MemTotal"); // MemTotal:        1959700 kB
        try {
            String kB = StringUtils.splitByWholeSeparator(memInfo, null)[1];
            return Long.valueOf(kB);
        } catch (Exception e) {
            log.warn("get memSize err, memInfo={}", memInfo, e);
            return null;
        }
    }

    /**
     * ?????????????????????
     */
    public static Size getPhysicalSize(IDevice iDevice) {
        // Physical size: 1080x2400
        // Override size: 720x1600
        String wmSize = executeShellCommand(iDevice, "wm size");
        Matcher matcher = Pattern.compile("Physical size: (\\d+)x(\\d+)").matcher(wmSize);
        if (matcher.find()) {
            int width = Integer.parseInt(matcher.group(1));
            int height = Integer.parseInt(matcher.group(2));
            return new Size(width, height);
        }

        return new Size(-1, -1);
    }

    public static String executeShellCommand(IDevice iDevice, String cmd) {
        Validate.notBlank(cmd);

        try {
            CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
            iDevice.executeShellCommand(cmd, outputReceiver);
            return outputReceiver.getOutput();
        } catch (Exception e) {
            throw new ShellCommandException(e);
        }
    }
}
