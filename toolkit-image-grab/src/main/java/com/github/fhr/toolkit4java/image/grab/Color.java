package com.github.fhr.toolkit4java.image.grab;

/**
 * @author Fan Huaran
 * created on 2019/12/11
 * @description
 */
public class Color {

    private final int red;

    private final int green;

    private final int blue;

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    /**
     * 计算颜色距离
     *
     * @param color
     * @return
     */
    public double colorDistance(Color color) {
        int absR = color.getRed() - this.red;
        int absG = color.getGreen() - this.green;
        int absB = color.getBlue() - this.blue;

        return  Math.sqrt(absR * absR + absG * absG + absB * absB);
    }

    @Override
    public String toString() {
        return "Color{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                '}';
    }
}
