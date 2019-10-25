package com.fhr.github.toolkit4java.http;

import java.util.Map;

/**
 * @author Fan Huaran
 * created on 2019/10/24
 * @description 返回结果封装
 */
public class Response {
    private int statusCode;

    private String contentType;

    private Map<String, String> headers;

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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
