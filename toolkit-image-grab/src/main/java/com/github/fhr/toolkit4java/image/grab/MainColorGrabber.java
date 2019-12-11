package com.github.fhr.toolkit4java.image.grab;

import java.awt.*;
import java.util.List;

/**
 * @author Fan Huaran
 * created on 2019/12/11
 * @description 主色抓取器
 */
public class MainColorGrabber {

    public static List<String> grab(Image image, int mainColorCount){
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

        return null;
    }

}
