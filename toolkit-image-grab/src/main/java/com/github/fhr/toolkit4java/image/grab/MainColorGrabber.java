package com.github.fhr.toolkit4java.image.grab;

import com.github.fhr.toolkit4java.image.AlphaImageCropper;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fan Huaran
 * created on 2019/12/11
 * @description 主色抓取器
 */
public class MainColorGrabber {
    /**
     * 种子点数量
     */
    private static final int ROOT_POINT_NUM = 32;

    /**
     * 计算阈值
     */
    private static final int THRESHOLD = 100;

    public static List<Color> grab(BufferedImage inputImage, List<Color> ignoreColors, int mainColorCount) {
        /**
         先来介绍一下算法的思路：
         将每个像素点的RGB值映射到xyz三维坐标中，这样相当于一张图片所有的像素点都分散在三维坐标当中

         在图片上随机取N个点作为种子点，然后计算出图片上的所有像素点与这N个种子点的距离，每个像素点可以得到与其距离最近的种子点，就可以把该像素点加入到该种子点的点群中（这里体现了聚类的思想）。

         遍历过整张图片后，每个像素都会加入到其中一个种子点群中（种子点群会记录下他拥有的全部像素点RGB总和，拥有的像素点个数），然后每个种子点群求得自己所拥有点的平均RGB值，作为新的种子点代替原来的种子结点。

         重复这个过程，直到种子结点不再移动（收敛于某个RGB值）或者迭代次数超过阈值，最后得到的权重（点群中 点的数量）最高的RGB值便是图片的主色调， 也可以搭配权重前3的RGB值生成一套不错的配色方案
         ————————————————
         版权声明：本文为CSDN博主「Pot_back」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
         原文链接：https://blog.csdn.net/Pot_back/article/details/51010634
         **/
        // 如果图像有Alpha通道，清空poi周边的像素，方便选取样点
        BufferedImage image;
        if (inputImage.getColorModel().hasAlpha()) {
            image = AlphaImageCropper.crop(inputImage);
        } else {
            image = inputImage;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int widthOffset = (int) (width / Math.sqrt(ROOT_POINT_NUM));
        int heightOffset = (int) (height / Math.sqrt(ROOT_POINT_NUM));
        Set<Color> ignoreColorSet = Collections.emptySet();
        if (ignoreColors != null){
            ignoreColorSet = new HashSet<>(ignoreColors);
        }

        ColorModel colorModel = ColorModel.getRGBdefault();
        List<ColorPixelGroup> rootColorPixelGroups = new ArrayList<>();
        Set<Color> chooseColors = new HashSet<>();

        Color[] pixelValues = new Color[width * height];
        int pixelCount = 0;

        // 随机选取种子点
        for (int i = 0; i < width; i += widthOffset) {
            for (int j = 0; j < height; j += heightOffset) {
                int pixel = image.getRGB(i, j);
                int alpha = colorModel.getAlpha(pixel);
                if (alpha == 0) { // alpha == 0 prove: this pixel is transparent.
                    continue;
                }

                Color color = new Color(colorModel.getRed(pixel), colorModel.getGreen(pixel), colorModel.getBlue(pixel));
                if (ignoreColorSet.contains(color)){
                    continue;
                }

                if (!chooseColors.contains(color)) {
                    ColorPixelGroup colorPixelGroup = new ColorPixelGroup();
                    colorPixelGroup.setColor(color);
                    rootColorPixelGroups.add(colorPixelGroup);
                }
                pixelValues[pixelCount++] = color;
            }
        }

        // 进入迭代
        int computeCount = 0;
        while (computeCount++ < THRESHOLD) {
            // 聚类
            for (int i = 0; i < pixelCount; i++) {
                Color color = pixelValues[i];
                ColorPixelGroup nearestColorPointGroup = null;
                BigDecimal minColorDistance = BigDecimal.valueOf(Double.MAX_VALUE);
                for (ColorPixelGroup rootColorPointGroup : rootColorPixelGroups) {
                    Color rootColor = rootColorPointGroup.getColor();
                    BigDecimal distance = BigDecimal.valueOf(rootColor.colorDistance(color));
                    if (minColorDistance.compareTo(distance) > 0) {
                        minColorDistance = distance;
                        nearestColorPointGroup = rootColorPointGroup;
                    }
                }
                nearestColorPointGroup.addPixel(color);
            }
            // 从大到小排序
            rootColorPixelGroups.sort(Comparator.comparingInt(ColorPixelGroup::getPixelCount));

            // 判断是否收敛
            boolean convergence = true;
            List<ColorPixelGroup> newRootColorPixelGroups = new ArrayList<>();
            for (ColorPixelGroup rootColorPixelGroup : rootColorPixelGroups) {
                Color newColor = rootColorPixelGroup.computeAvgColor();
                if (newColor != null) {
                    ColorPixelGroup newRootColorPixelGroup = new ColorPixelGroup();
                    newRootColorPixelGroup.setColor(newColor);
                    newRootColorPixelGroups.add(newRootColorPixelGroup);
                }
            }
            // 种子色的数量发生了变化，未收敛
            if (newRootColorPixelGroups.size() != rootColorPixelGroups.size()) {
                continue;
            }
            for (int i = 0, n = newRootColorPixelGroups.size(); i < n; i++) {
                if (!newRootColorPixelGroups.get(i).getColor().equals(rootColorPixelGroups.get(i).getColor())) {
                    convergence = false;
                }
            }

            // 收敛需要提前退出循环
            if (convergence) {
                break;
            } else {
                rootColorPixelGroups = newRootColorPixelGroups;
            }
        }

        // 取top mainColorCount 的颜色
        return rootColorPixelGroups.stream()
                .limit(mainColorCount)
                .map(ColorPixelGroup::getColor)
                .collect(Collectors.toList());
    }


}
