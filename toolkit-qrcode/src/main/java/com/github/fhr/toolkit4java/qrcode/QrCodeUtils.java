package com.github.fhr.toolkit4java.qrcode;

import com.github.fhr.toolkit4java.image.ImageUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fan Huaran
 * created on 2019/10/9
 * @description 二维码工具
 */
public class QrCodeUtils {
    /**
     * black rgb
     */
    private static final int BLACK = 0x000000;

    /**
     * white rgb
     */
    private static final int WHITE = 0xFFFFFF;

    /**
     * 生成二维码
     *
     * @param url
     * @param width
     * @param height
     * @param withMargin
     * @return
     * @throws Exception
     */
    public static Image generateQrCode(String url, int width, int height, boolean withMargin) throws Exception {
        Map<EncodeHintType, String> hints = new HashMap<>(4);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        // 4个边距，从小到大，1,2,3,4，这儿选取最小的边距
        hints.put(EncodeHintType.MARGIN, "1");
        BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hints);
        if (withMargin) {
            return toBufferedImage(bitMatrix);
        } else {
            // 如果需要去掉边距，需要特殊处理
            return toNonMarginBufferedImage(bitMatrix);
        }
    }

    /**
     * 生成二维码到文件
     *
     * @param url
     * @param width
     * @param height
     * @param fileName
     * @param withMargin
     * @throws Exception
     */
    public static void generateQrCode(String url, int width, int height, String fileName, boolean withMargin) throws Exception {
        Image image = generateQrCode(url, width, height, withMargin);
        ImageUtils.saveImageToFile((BufferedImage) image, "png", fileName);
    }

    /**
     * 位图 -> rgb
     *
     * @param matrix
     * @return
     */
    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 使用黑白分别代表二维码的位图信息
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    /**
     * 位图 —> 不带边框rgb
     *
     * @param matrix
     * @return
     * @throws IOException
     */
    public static BufferedImage toNonMarginBufferedImage(BitMatrix matrix) throws IOException {
        // step1: 获取感兴趣区的范围,该数组依次为起始x,起始y,宽度,高度
        int[] poiRectangle = matrix.getEnclosingRectangle();
        int startX = poiRectangle[0];
        int startY = poiRectangle[1];
        int poiWidth = poiRectangle[2];
        int poiHeight = poiRectangle[3];

        // step2: poi -> image
        BufferedImage image = new BufferedImage(poiWidth, poiWidth, BufferedImage.TYPE_INT_RGB);
        for (int i = 1; i < poiWidth; i++) {
            for (int j = 1; j < poiHeight; j++) {
                int rgb = matrix.get(i + startX, j + startY) ? BLACK : WHITE;
                image.setRGB(i - 1, j - 1, rgb);
            }
        }

        // step3: scale
        int outWidth = matrix.getWidth();
        int outHeight = matrix.getHeight();
        BufferedImage outImage = new BufferedImage(outWidth, outWidth, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = outImage.getGraphics();
        graphics.drawImage(image, 0, 0, outWidth, outHeight, null);
        graphics.dispose();
        return outImage;
    }

}
