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
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Feature2D;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

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

        // debug
//        Imgproc.rectangle(img, rect, new Scalar(0, 255, 0));
//        Imgcodecs.imwrite("/Users/jiangyitao/Desktop/opencv_test/match_template_result.png", img);

        return new MatchTemplateResult(rect, mmr.maxVal);
    }

    public static Rect matchFeature(Mat img, Mat template, Feature2D feature2D, double distanceThreshold) {
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

        List<DMatch> goodMatches = new ArrayList<>();
        for (MatOfDMatch match : matches) {
            DMatch[] dMatches = match.toArray();
            if (dMatches[0].distance < dMatches[1].distance * distanceThreshold) {
                goodMatches.add(dMatches[0]);
            }
        }
        if (goodMatches.size() < 4) {
            return null;
        }

        KeyPoint[] imgKPArr = imgKP.toArray();
        KeyPoint[] templateKPArr = templateKP.toArray();
        List<Point> scenePoints = new ArrayList<>();
        List<Point> objPoints = new ArrayList<>();
        for (DMatch goodMatch : goodMatches) {
            scenePoints.add(imgKPArr[goodMatch.trainIdx].pt);
            objPoints.add(templateKPArr[goodMatch.queryIdx].pt);
        }
        MatOfPoint2f scenePointsMat = new MatOfPoint2f();
        scenePointsMat.fromList(scenePoints);
        MatOfPoint2f objPointsMat = new MatOfPoint2f();
        objPointsMat.fromList(objPoints);
        // 使用 findHomography 寻找匹配上的关键点的变换
        Mat homography = Calib3d.findHomography(objPointsMat, scenePointsMat, Calib3d.RANSAC, 3);

        // 透视变换(Perspective Transformation)是将图片投影到一个新的视平面(Viewing Plane)，也称作投影映射(Projective Mapping)
        Mat templateCorners = new Mat(4, 1, CvType.CV_32FC2);
        Mat templateTransformResult = new Mat(4, 1, CvType.CV_32FC2);
        templateCorners.put(0, 0, 0, 0);
        templateCorners.put(1, 0, template.cols(), 0);
        templateCorners.put(2, 0, template.cols(), template.rows());
        templateCorners.put(3, 0, 0, template.rows());
        // 使用 perspectiveTransform 将模板图进行透视变以矫正图象得到标准图片
        Core.perspectiveTransform(templateCorners, templateTransformResult, homography);

        // A --- B
        // |     |
        // D --- C
        double[] pointA = templateTransformResult.get(0, 0);
        double[] pointB = templateTransformResult.get(1, 0);
        double[] pointC = templateTransformResult.get(2, 0);
        double[] pointD = templateTransformResult.get(3, 0);

        // debug
//        Mat subMat = img.submat((int) pointA[1], (int) pointC[1], (int) pointD[0], (int) pointB[0]);
//        Imgcodecs.imwrite("/Users/jiangyitao/Desktop/opencv_test/原图中的匹配图.png", subMat);
//        // 将匹配的图像用四条线框出来
//        Imgproc.line(img, new Point(pointA), new Point(pointB), new Scalar(0, 255, 0), 4); // 上 A->B
//        Imgproc.line(img, new Point(pointB), new Point(pointC), new Scalar(0, 255, 0), 4); // 右 B->C
//        Imgproc.line(img, new Point(pointC), new Point(pointD), new Scalar(0, 255, 0), 4); // 下 C->D
//        Imgproc.line(img, new Point(pointD), new Point(pointA), new Scalar(0, 255, 0), 4); // 左 D->A
//        MatOfDMatch goodMatchesMat = new MatOfDMatch();
//        goodMatchesMat.fromList(goodMatches);
//        Mat matchOutput = new Mat(img.rows() * 2, img.cols() * 2, Imgcodecs.IMREAD_COLOR);
//        Features2d.drawMatches(template, templateKP, img, imgKP, goodMatchesMat, matchOutput, new Scalar(0, 255, 0), new Scalar(255, 0, 0), new MatOfByte(), 2);
//        Imgcodecs.imwrite("/Users/jiangyitao/Desktop/opencv_test/特征点匹配过程.png", matchOutput);
//        Imgcodecs.imwrite("/Users/jiangyitao/Desktop/opencv_test/模板图在原图中的位置.png", img);

        int x = (int) pointA[0];
        int y = (int) pointA[1];
        int width = (int) (pointB[0] - pointA[0]);
        int height = (int) (pointD[1] - pointA[1]);
        if (x <= 0 || y <= 0 || width <= 0 || height <= 0) {
            return null;
        }
        return new Rect(x, y, width, height);
    }

}