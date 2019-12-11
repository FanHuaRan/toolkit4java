package com.github.fhr.toolkit4java.image.grab;

import org.junit.Test;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ImageUtilTest {

    @Test
    public void getColorSolution() throws IOException {
        ImageUtil imageUtil = new ImageUtil();
        System.out.println(imageUtil.getColorSolution(ImageIO.read(new File("C:\\Users\\dmaller\\Pictures\\tom.jpg")),
                32*32,3));
    }
}