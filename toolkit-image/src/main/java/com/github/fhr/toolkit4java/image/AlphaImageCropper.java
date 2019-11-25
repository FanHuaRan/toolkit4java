package com.github.fhr.toolkit4java.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * @author Fan Huaran
 * created on 2019/11/25
 * @description cut the image with alpha(keep poi)
 */
public class AlphaImageCropper {

    /**
     * crop
     *
     * @param inputImage
     * @return
     */
    public static BufferedImage crop(BufferedImage inputImage) {
        // step1: param check
        if (inputImage == null) {
            throw new NullPointerException("inputImage must not be null");
        }
        ColorModel colorModel = inputImage.getColorModel();
        if (!colorModel.hasAlpha()) {
            throw new IllegalArgumentException("image is must support alpha");
        }

        // step2: get poi
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        ColorModel doColorModel = ColorModel.getRGBdefault();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = inputImage.getRGB(i, j);
                // int alpha = pixel >> 32 & 0Xff; some image will return 255,
                // int alpha = colorModel.getAlpha(pixel);
                // must use ColorModel.getRGBdefault().getAlpha do this
                int alpha = doColorModel.getAlpha(pixel);
                if (alpha != 0) { // alpha == 0 prove: this pixel is transparent.
                    minX = minX > i ? i : minX;
                    maxX = maxX < i ? i : maxX;
                    minY = minY > j ? j : minY;
                    maxY = maxY < j ? j : maxY;
                }
            }
        }

        // step3: cut image
        int cropWidth = maxX - minX;
        int cropHeight = maxY - minY;
        return inputImage.getSubimage(minX, minY, cropWidth, cropHeight);
    }


}
