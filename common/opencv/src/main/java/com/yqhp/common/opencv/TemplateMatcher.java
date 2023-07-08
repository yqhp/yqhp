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
package com.yqhp.common.opencv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @author jiangyitao
 */
@Slf4j
public class TemplateMatcher {

    @Data
    @AllArgsConstructor
    public static class Result {
        public final Rect rect;
        public final double score;
    }

    static {
        log.info("opencv loading...");
        OpenCV.loadShared();
        log.info("opencv loaded");
    }

    public static Rect match(String imgPath, String templatePath, double threshold) {
        Mat img = Imgcodecs.imread(imgPath);
        Mat template = Imgcodecs.imread(templatePath);
        return match(img, template, threshold);
    }

    public static Rect match(Mat img, Mat template, double threshold) {
        Result result = match(img, template);
        return result.score >= threshold ? result.rect : null;
    }

    private static Result match(Mat img, Mat template) {
        // opencv matchTemplate
        int resultCols = img.cols() - template.cols() + 1;
        int resultRows = img.rows() - template.rows() + 1;
        Mat opencvResult = new Mat(resultRows, resultCols, CvType.CV_32FC1);
        Imgproc.matchTemplate(img, template, opencvResult, Imgproc.TM_CCOEFF_NORMED);

        // 转换opencvResult
        Core.MinMaxLocResult mmr = Core.minMaxLoc(opencvResult);
        Rect rect = new Rect((int) mmr.maxLoc.x, (int) mmr.maxLoc.y, template.width(), template.height());
        return new Result(rect, mmr.maxVal);
    }

}
