package com.github.fhr.toolkit4java.image.grab;

import org.junit.Test;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class MainColorGrabberTest {

    @Test
    public void grab() throws IOException {
        BufferedImage image = ImageIO.read(new File("F:\\工作文档\\图片系统\\商品主色\\油.png"));
        List<Color> colors = MainColorGrabber.grab(image, null,3);
        for (int i = 0; i < colors.size(); i++) {
            Color color = colors.get(i);
            // 初始化
            Graphics2D graphics = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB).createGraphics();
            // 获取一张去白底的透明图片
            BufferedImage outImage = graphics.getDeviceConfiguration().createCompatibleImage(50, 50, Transparency.TRANSLUCENT);
            // 释放资源
            graphics.dispose();
            Graphics2D graphics2D = (Graphics2D) outImage.getGraphics();
            Rectangle rectangle = new Rectangle(0, 0, 50, 50);
            java.awt.Color useColor = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
            graphics2D.setColor(useColor);
            graphics2D.fill(rectangle);
            graphics2D.dispose();
            ImageIO.write(outImage, "png", new File(String.format("F:\\工作文档\\图片系统\\商品主色\\油%s.png", (i+1))));
        }
    }
}