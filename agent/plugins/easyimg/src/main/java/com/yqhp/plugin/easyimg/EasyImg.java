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
package com.yqhp.plugin.easyimg;

import com.yqhp.common.commons.util.FileUtils;
import com.yqhp.common.opencv.MatchTemplateResult;
import com.yqhp.common.opencv.OpencvEngine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.List;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
@Slf4j
public class EasyImg {

    private File imgDir;
    private final RemoteWebDriver driver;

    public EasyImg(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public void setImgDir(String dir) {
        imgDir = new File(dir);
    }

    /**
     * 查找颜色，找不到返回null
     *
     * @param timeout 查找超时时间
     * @since 0.0.2
     */
    public RectX findColors(List<Color> colors, Duration timeout) {
        return findColors(colors, timeout, Duration.ofMillis(500));
    }

    /**
     * 查找颜色，找不到返回null
     *
     * @param timeout  查找超时时间
     * @param interval 查找间隔时间
     * @since 0.0.2
     */
    @SneakyThrows
    public RectX findColors(List<Color> colors, Duration timeout, Duration interval) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        long intervalMs = interval.toMillis();
        for (; ; ) {
            RectX rect = findColors(colors);
            if (rect != null) {
                return rect;
            }
            if (System.currentTimeMillis() > endTime) {
                // 超时
                return null;
            }
            Thread.sleep(intervalMs);
        }
    }

    /**
     * 查找颜色，找不到返回null
     *
     * @since 0.0.2
     */
    @SneakyThrows
    public RectX findColors(List<Color> colors) {
        File img = screenshot();
        try {
            return findColors(ImageIO.read(img), colors);
        } finally {
            // 删除截图
            if (img != null) img.delete();
        }
    }

    /**
     * 查找颜色，找不到返回null
     *
     * @since 0.0.2
     */
    private RectX findColors(BufferedImage img, List<Color> colors) {
        Validate.notNull(img);
        Validate.notEmpty(colors);

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Color color : colors) {
            if (color.argb != img.getRGB(color.x, color.y)) {
                return null;
            }
            minX = Math.min(minX, color.x);
            maxX = Math.max(maxX, color.x);
            minY = Math.min(minY, color.y);
            maxY = Math.max(maxY, color.y);
        }

        return new RectX(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /**
     * 查找图片，找不到返回null
     *
     * @param templateUri 模版图片 httpUrl or filePath
     * @since 0.0.2
     */
    public RectX findImage(String templateUri) {
        return findImage(templateUri, null);
    }

    /**
     * 查找图片，找不到返回null
     *
     * @param templateUri 模版图片 httpUrl or filePath
     * @param timeout     查找超时时间
     * @since 0.0.2
     */
    @SneakyThrows
    public RectX findImage(String templateUri, Duration timeout) {
        File templateFile = templateUri.startsWith("http")
                ? FileUtils.downloadIfAbsent(templateUri, imgDir)
                : new File(templateUri);
        return timeout == null
                ? findImage(templateFile, 0.9)
                : findImage(templateFile, timeout, Duration.ofMillis(500), 0.9);
    }

    /**
     * 查找图片，找不到返回null
     *
     * @param template  模版图片
     * @param timeout   查找超时时间
     * @param interval  查找间隔时间
     * @param threshold 相似度
     * @since 0.0.2
     */
    @SneakyThrows
    private RectX findImage(File template, Duration timeout, Duration interval, double threshold) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        long intervalMs = interval.toMillis();
        for (; ; ) {
            RectX rect = findImage(template, threshold);
            if (rect != null) {
                return rect;
            }
            if (System.currentTimeMillis() > endTime) {
                // 超时
                return null;
            }
            Thread.sleep(intervalMs);
        }
    }

    @SneakyThrows
    private RectX findImage(File templatePath, double threshold) {
        File img = screenshot();
        try {
            return findImage(img.getAbsolutePath(), templatePath.getAbsolutePath(), threshold);
        } finally {
            // 删除截图
            if (img != null) img.delete();
        }
    }

    private RectX findImage(String imgPath, String templatePath, double threshold) {
        Mat img = Imgcodecs.imread(imgPath, Imgproc.COLOR_BGR2GRAY);
        Mat template = Imgcodecs.imread(templatePath, Imgproc.COLOR_BGR2GRAY);
        return findImage(img, template, threshold);
    }

    private RectX findImage(Mat img, Mat template, double threshold) {
        // 模版匹配
        MatchTemplateResult matchTemplateResult = OpencvEngine.matchTemplate(img, template);
        if (matchTemplateResult.score >= threshold) {
            return new RectX(matchTemplateResult.rect);
        }

        // 特征匹配
        Rect rect = OpencvEngine.matchFeature(img, template, SIFT.create(), threshold);
        return rect != null ? new RectX(rect) : null;
    }

    private File screenshot() {
        return driver.getScreenshotAs(OutputType.FILE);
    }
}
