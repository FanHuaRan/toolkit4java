package com.github.fhr.toolkit4java.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Fan Huaran
 * created on 2019/8/28
 * @description
 */
public class ImageUtils {
    /**
     * 从本地文件系统中读取图片
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static BufferedImage readImageFromFile(String fileName) {
        File srcFile = new File(fileName);
        try {
            return ImageIO.read(srcFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从URL当中读取图片,支持http协议
     *
     * @param url
     * @return
     */
    public static BufferedImage readImageFromUrl(URL url) {
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从URL当中读取图片,支持http协议
     *
     * @param url
     * @return
     */
    public static BufferedImage readImageFromUrl(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            throw new RuntimeException("read image fail,url:" + url, e);
        }
    }

    /**
     * 保存图片到本地文件系统当中
     *
     * @param image
     * @param format
     * @param fileName
     * @throws IOException
     */
    public static void saveImageToFile(BufferedImage image, String format, String fileName) {
        File srcFile = new File(fileName);
        if (!srcFile.getParentFile().exists()) {
            srcFile.getParentFile().mkdir();
        }
        try {
            ImageIO.write(image, format, srcFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
