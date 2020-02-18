package com.github.fhr.toolkit4java.image.grab;

/**
 * @author Fan Huaran
 * created on 2019/12/11
 * @description
 */
public class ColorPixelGroup {

    private long totalRed;

    private long totalGreen;

    private long totalBlue;

    private int pixelCount;

    private Color color;

    public int getPixelCount() {
        return pixelCount;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void addPixel(Color color){
        this.pixelCount++;
        this.totalRed += color.getRed();
        this.totalGreen += color.getGreen();
        this.totalBlue += color.getBlue();
    }

    public Color computeAvgColor(){
        if (this.pixelCount == 0){
            return null;
        }
        int avgRed = (int) (this.totalRed / this.pixelCount);
        int avgGreen = (int) (this.totalGreen / this.pixelCount);
        int avgBlue = (int) (this.totalBlue / this.pixelCount);
        return new Color(avgRed, avgGreen, avgBlue);
    }

    @Override
    public String toString() {
        return "ColorPointGroup{" +
                "totalRed=" + totalRed +
                ", totalGreen=" + totalGreen +
                ", totalBlue=" + totalBlue +
                ", pixelCount=" + pixelCount +
                ", color=" + color +
                '}';
    }
}
