package com.github.fhr.toolkit4java.image;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class AlphaImageCropperTest {

    @org.junit.Test
    public void crop() throws IOException {
        ImageIO.write(AlphaImageCropper.crop(ImageIO.read(new File("E:\\1.png"))),"png",new File("E:\\1_sub.png"));
        ImageIO.write(AlphaImageCropper.crop(ImageIO.read(new File("E:\\楼层条.png"))),"png",new File("E:\\楼层条_sub.png"));
        ImageIO.write(AlphaImageCropper.crop(ImageIO.read(new File("E:\\tom.jpg"))),"png",new File("E:\\tom.png"));
    }
}