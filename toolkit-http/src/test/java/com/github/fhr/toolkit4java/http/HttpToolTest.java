package com.github.fhr.toolkit4java.http;

import org.junit.Test;

import java.io.IOException;

public class HttpToolTest {

    private HttpTool httpTool = new HttpTool();
    {
        httpTool.init();
    }

    @Test
    public void postWithFile() {
    }

    @Test
    public void get() throws IOException {
        Response response = httpTool.get("http://www.baidu.com", null, null);
    }

    @Test
    public void postWithForm() {
    }

    @Test
    public void postWithJson() {
    }
}