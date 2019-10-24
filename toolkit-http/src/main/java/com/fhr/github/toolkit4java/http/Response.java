package com.fhr.github.toolkit4java.http;

/**
 * @author Fan Huaran
 * created on 2019/10/24
 * @description 返回结果封装
 */
public class Response {
    private int statusCode;

    private String contentType;

    private byte[] content;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
