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

import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.yqhp.agent.androidtools.AndroidUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Validate;

import java.awt.*;
import java.time.Duration;
import java.util.List;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
public class EasyImg {

    private final IDevice iDevice;

    public EasyImg(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 通过颜色查找坐标，找不到返回null
     *
     * @param timeout 查找超时时间
     * @since 0.0.1
     */
    public Point findPoint(List<Color> colors, Duration timeout) {
        return findPoint(colors, timeout, Duration.ofMillis(500));
    }

    /**
     * 通过颜色查找坐标，找不到返回null
     *
     * @param timeout 查找超时时间
     * @param sleep   每次查找间隔时间
     * @since 0.0.1
     */
    @SneakyThrows
    public Point findPoint(List<Color> colors, Duration timeout, Duration sleep) {
        long maxFindTime = System.currentTimeMillis() + timeout.toMillis();
        long sleepMs = sleep.toMillis();
        do {
            Point point = findPoint(colors);
            if (point != null) {
                return point;
            }
            Thread.sleep(sleepMs);
        } while (maxFindTime > System.currentTimeMillis());
        // 超时，最后再找一次
        return findPoint(colors);
    }

    /**
     * 通过颜色查找坐标，找不到返回null
     *
     * @since 0.0.1
     */
    public Point findPoint(List<Color> colors) {
        RawImage rawImage = AndroidUtils.screenshotAsRawImage(iDevice);
        return findPoint(rawImage, colors);
    }

    /**
     * 通过颜色查找坐标，找不到返回null
     *
     * @since 0.0.1
     */
    @SneakyThrows
    public Point findPoint(RawImage image, List<Color> colors) {
        Validate.notNull(image);
        Validate.notEmpty(colors);

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Color color : colors) {
            if (color.argb != getARGB(image, color.x, color.y)) {
                return null;
            }
            minX = Math.min(minX, color.x);
            maxX = Math.max(maxX, color.x);
            minY = Math.min(minY, color.y);
            maxY = Math.max(maxY, color.y);
        }

        return new Point(minX + (maxX - minX) / 2, minY + (maxY - minY) / 2);
    }

    private int getARGB(RawImage img, int x, int y) {
        return img.getARGB((x + y * img.width) * (img.bpp / 8));
    }
}
