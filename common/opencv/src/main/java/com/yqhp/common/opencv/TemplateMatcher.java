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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Feature2D;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
public class TemplateMatcher {

    public static TemplateMatchResult match(Mat img, Mat template) {
        // opencv matchTemplate
        int resultCols = img.cols() - template.cols() + 1;
        int resultRows = img.rows() - template.rows() + 1;
        Mat opencvResult = new Mat(resultRows, resultCols, CvType.CV_32FC1);
        Imgproc.matchTemplate(img, template, opencvResult, Imgproc.TM_CCOEFF_NORMED);

        // 转换opencvResult
        Core.MinMaxLocResult mmr = Core.minMaxLoc(opencvResult);
        Rect rect = new Rect((int) mmr.maxLoc.x, (int) mmr.maxLoc.y, template.width(), template.height());
        return new TemplateMatchResult(rect, mmr.maxVal);
    }

    public static List<KeyPoint> match(Mat img, Mat template, Feature2D feature2D) {
        Validate.notNull(img);
        Validate.notNull(template);
        Validate.notNull(feature2D);

        // 检测和计算 图像的关键点和描述符
        MatOfKeyPoint imgKeyPoints = new MatOfKeyPoint();
        Mat imgDescriptors = new Mat();
        feature2D.detectAndCompute(img, new Mat(), imgKeyPoints, imgDescriptors);
        MatOfKeyPoint templateKeyPoints = new MatOfKeyPoint();
        Mat templateDescriptors = new Mat();
        feature2D.detectAndCompute(template, new Mat(), templateKeyPoints, templateDescriptors);

        // 使用Brute-Force匹配器对描述符进行匹配
        MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        matcher.match(imgDescriptors, templateDescriptors, matches);

        // 筛选匹配结果
        List<KeyPoint> imgKeyPointsList = imgKeyPoints.toList();
        double distance = 0.1 * 0.5 * Math.max(img.cols(), img.rows());
        return matches.toList().stream()
                .filter(match -> match.distance < distance)
                .map(match -> imgKeyPointsList.get(match.queryIdx))
                .collect(Collectors.toList());
    }

}
