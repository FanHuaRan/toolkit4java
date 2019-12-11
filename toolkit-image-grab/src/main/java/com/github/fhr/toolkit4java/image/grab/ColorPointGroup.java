package com.github.fhr.toolkit4java.image.grab;

/**
 * @author Fan Huaran
 * created on 2019/12/11
 * @description
 */
public class ColorPointGroup {

    private long totalRed;

    private long totalGreen;

    private long totalBlue;

    private int pointCount;

    private Color color;

    public long getTotalRed() {
        return totalRed;
    }

    public void setTotalRed(long totalRed) {
        this.totalRed = totalRed;
    }

    public long getTotalGreen() {
        return totalGreen;
    }

    public void setTotalGreen(long totalGreen) {
        this.totalGreen = totalGreen;
    }

    public long getTotalBlue() {
        return totalBlue;
    }

    public void setTotalBlue(long totalBlue) {
        this.totalBlue = totalBlue;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
