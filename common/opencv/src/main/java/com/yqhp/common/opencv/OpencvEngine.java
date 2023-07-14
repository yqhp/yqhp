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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencv.core.CvType.CV_32F;

/**
 * @author jiangyitao
 */
@Slf4j
public class OpencvEngine {

    public static MatchTemplateResult matchTemplate(Mat img, Mat template) {
        Validate.notNull(img);
        Validate.notNull(template);

        // opencv matchTemplate
        int resultCols = img.cols() - template.cols() + 1;
        int resultRows = img.rows() - template.rows() + 1;
        Mat opencvResult = new Mat(resultRows, resultCols, CvType.CV_32FC1);
        Imgproc.matchTemplate(img, template, opencvResult, Imgproc.TM_CCOEFF_NORMED);

        // 转换opencvResult
        Core.MinMaxLocResult mmr = Core.minMaxLoc(opencvResult);
        Rect rect = new Rect((int) mmr.maxLoc.x, (int) mmr.maxLoc.y, template.width(), template.height());
        return new MatchTemplateResult(rect, mmr.maxVal);
    }

    public static List<Point> matchFeature(Mat img, Mat template, Feature2D feature2D,
                                           double distanceThreshold, int clusterCount) {
        Validate.notNull(img);
        Validate.notNull(template);
        Validate.notNull(feature2D);

        // 检测和计算关键点和描述符
        MatOfKeyPoint imgKP = new MatOfKeyPoint();
        Mat imgDesc = new Mat();
        feature2D.detectAndCompute(img, new Mat(), imgKP, imgDesc);
        MatOfKeyPoint templateKP = new MatOfKeyPoint();
        Mat templateDesc = new Mat();
        feature2D.detectAndCompute(template, new Mat(), templateKP, templateDesc);

        // 匹配
        List<MatOfDMatch> matches = new ArrayList<>();
        // matches: [[DMatch,DMatch],[DMatch,DMatch]]
        DescriptorMatcher.create(DescriptorMatcher.FLANNBASED)
                .knnMatch(templateDesc, imgDesc, matches, 2);

        // debug
//        Mat outImg = new Mat();
//        org.opencv.features2d.Features2d.drawMatchesKnn(template, templateKP, img, imgKP, matches, outImg);
//        org.opencv.highgui.HighGui.imshow("", outImg);
//        org.opencv.highgui.HighGui.waitKey(0);

        List<DMatch> goodMatches = new ArrayList<>();
        if (matches.size() == 1) {
            Collections.addAll(goodMatches, matches.get(0).toArray());
        } else {
            for (MatOfDMatch match : matches) {
                DMatch[] dMatches = match.toArray();
                if (dMatches[0].distance < dMatches[1].distance * distanceThreshold) {
                    goodMatches.add(dMatches[0]);
                }
            }
        }

        KeyPoint[] imgKPArr = imgKP.toArray();
        // img goodPoints
        List<Point> goodPoints = goodMatches.stream()
                .map(good -> imgKPArr[good.trainIdx].pt)
                .collect(Collectors.toList());
        return getLargestClusterPoints(goodPoints, clusterCount);
    }

    // 这个函数由chatgpt生成, cool
    private static List<Point> getLargestClusterPoints(List<Point> points, int clusterCount) {
        if (points == null || points.size() == 0) {
            return new ArrayList<>();
        }

        if (points.size() < clusterCount) {
            clusterCount = 1;
        }

        // 转换点列表为MatOfPoint2f
        MatOfPoint2f matOfPoint = new MatOfPoint2f();
        matOfPoint.fromList(points);

        // 转换为CV_32F类型的Mat
        Mat samples = new Mat(matOfPoint.rows(), matOfPoint.cols(), CV_32F);
        matOfPoint.convertTo(samples, CV_32F);

        // 设置KMeans参数
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        int attempts = 5;
        int flags = Core.KMEANS_PP_CENTERS;

        // 进行KMeans聚类
        Mat labels = new Mat();
        Mat centers = new Mat();
        Core.kmeans(samples, clusterCount, labels, criteria, attempts, flags, centers);

        // 计算每个簇的点数
        int[] clusterSizes = new int[clusterCount];
        for (int i = 0; i < labels.rows(); i++) {
            int label = (int) labels.get(i, 0)[0];
            clusterSizes[label]++;
        }

        // 找到包含最多点的簇的索引
        int largestClusterIndex = 0;
        int largestClusterSize = 0;
        for (int i = 0; i < clusterCount; i++) {
            if (clusterSizes[i] > largestClusterSize) {
                largestClusterIndex = i;
                largestClusterSize = clusterSizes[i];
            }
        }

        // 获取包含最多点的簇的点列表
        List<Point> largestClusterPoints = new ArrayList<>();
        for (int i = 0; i < labels.rows(); i++) {
            int label = (int) labels.get(i, 0)[0];
            if (label == largestClusterIndex) {
                largestClusterPoints.add(points.get(i));
            }
        }

        return largestClusterPoints;
    }
}