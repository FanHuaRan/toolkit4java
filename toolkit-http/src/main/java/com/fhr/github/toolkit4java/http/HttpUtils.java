package com.fhr.github.toolkit4java.http;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Fan Huaran
 * created on 2019/10/15
 * @description Http辅助组件
 */
public class HttpUtils {
    // 连接池总大小
    private static final int MAX_TOTAL = 100;
    // 每个domain的连接最大限制，MAX_PER_ROUTE<=MAX_TOTAL才有意义
    private static final int MAX_PER_ROUTE = MAX_TOTAL;
    // 可用空闲连接过期时间,重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建
    private static final int VALIDATE_AFTER_INACTIVITY = 10 * 1000;
    // 连接超时时间
    private static final int CONNECTION_TIMEOUT = 7000;
    // socket读取数据超时时间
    private static final int SOCKET_TIMEOUT = 7000;
    // 从连接池当中获取时间的超时时间
    private static final int CONNECTION_REQUEST_TIMEOUT = 7000;

    /**
     * http连接池管理器
     */
    private static final PoolingHttpClientConnectionManager HTTP_CLIENT_CONNECTION_MANAGER;
    /**
     * 请求配置
     */
    private static final RequestConfig REQUEST_CONFIG;

    static {
        // step1 配置连接池管理器
        HTTP_CLIENT_CONNECTION_MANAGER = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        HTTP_CLIENT_CONNECTION_MANAGER.setMaxTotal(MAX_TOTAL);
        // 设置每个domain的最大连接数限制
        HTTP_CLIENT_CONNECTION_MANAGER.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        // 设置连接空闲时间
        HTTP_CLIENT_CONNECTION_MANAGER.setValidateAfterInactivity(VALIDATE_AFTER_INACTIVITY);

        // step2 请求配置
        REQUEST_CONFIG = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)// 设置连接超时
                .setSocketTimeout(SOCKET_TIMEOUT)// 设置读取超时
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)// 设置从连接池获取连接实例的超时
                .build();
    }

    /**
     * 发送文件上传请求
     *
     * @param url
     * @param headers
     * @param params
     * @param file
     * @return
     * @throws IOException
     */
    public static Response postWithFile(String url, Map<String, String> headers, Map<String, String> params, File file) throws IOException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        if (headers == null) {
            headers = Collections.emptyMap();
        }

        HttpPost httpPost = new HttpPost(url);
        httpPost.setProtocolVersion(HttpVersion.HTTP_1_0);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }

        // todo 添加param
        List<NameValuePair> pairList = new ArrayList<>(params.size());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            pairList.add(pair);
        }

        httpPost.setConfig(REQUEST_CONFIG);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        //解决上传文件，文件名中文乱码问题
        builder.setCharset(StandardCharsets.UTF_8);
        //设置浏览器兼容模式
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        //将java.io.File对象添加到HttpEntity（org.apache.http.HttpEntity）对象中
        builder.addPart("file", new FileBody(file));
        httpPost.setEntity(builder.build());

        return doHttpExecute(httpPost);
    }

    /**
     * 发送 GET 请求（HTTP），K-V形式
     *
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public static Response get(String url, Map<String, String> headers, Map<String, Object> params) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        if (params == null) {
            params = Collections.emptyMap();
        }

        String apiUrl = url + buildUrlParams(params);

        HttpGet httpGet = new HttpGet(apiUrl);
        httpGet.setConfig(REQUEST_CONFIG);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }

        return doHttpExecute(httpGet);
    }

    /**
     * 发送 POST 请求（HTTP），表单提交
     *
     * @param apiUrl  API接口URL
     * @param headers header
     * @param params  参数map
     * @return
     */
    public static Response postWithForm(String apiUrl, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        if (params == null) {
            params = Collections.emptyMap();
        }

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(REQUEST_CONFIG);

        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }

        List<NameValuePair> pairList = new ArrayList<>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            pairList.add(pair);
        }

        httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));

        return doHttpExecute(httpPost);
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式
     *
     * @param apiUrl  API接口URL
     * @param headers header
     * @param content json对象
     * @return
     */
    public static Response postWithJson(String apiUrl, Map<String, Object> headers, Object content) throws IOException {
        return postWithJsonStr(apiUrl, headers, JSON.toJSONString(content));
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式
     *
     * @param apiUrl  API接口URL
     * @param headers header
     * @param jsonStr json字符串
     * @return
     */
    public static Response postWithJsonStr(String apiUrl, Map<String, Object> headers, String jsonStr) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(REQUEST_CONFIG);

        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
        }

        StringEntity stringEntity = new StringEntity(jsonStr, "UTF-8");//解决中文乱码问题
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);

        return doHttpExecute(httpPost);
    }


    /**
     * 发送 SSL POST 请求（HTTPS），K-V形式
     *
     * @param apiUrl  API接口URL
     * @param headers header
     * @param params  参数map
     * @return
     */
    public static Response postSSLWithForm(String apiUrl, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        if (params == null) {
            params = Collections.emptyMap();
        }

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(REQUEST_CONFIG);

        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
        }

        List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                    .getValue().toString());
            pairList.add(pair);
        }

        return doSSLHttpExecute(httpPost);
    }

    /**
     * 发送 SSL POST 请求（HTTPS），JSON形式
     *
     * @param apiUrl  API接口URL
     * @param content JSON对象
     * @return
     */
    public static Response postSSLWithJson(String apiUrl, Map<String, Object> headers, Object content) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(REQUEST_CONFIG);

        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
        }

        StringEntity stringEntity = new StringEntity(JSON.toJSONString(content), "UTF-8");//解决中文乱码问题
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);

        return doSSLHttpExecute(httpPost);
    }

    /**
     * 执行http调用
     *
     * @param httpUriRequest
     * @return
     * @throws IOException
     */
    private static Response doHttpExecute(HttpUriRequest httpUriRequest) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpUriRequest)) {
            Response response = new Response();
            response.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
            response.setContentType(closeableHttpResponse.getEntity().getContentType().getValue());
            response.setContent(EntityUtils.toByteArray(closeableHttpResponse.getEntity()));

            return response;
        } catch (Exception er) {
            er.printStackTrace();
            throw er;
        }
    }

    /**
     * 执行https调用
     *
     * @param httpUriRequest
     * @return
     * @throws IOException
     */
    private static Response doSSLHttpExecute(HttpUriRequest httpUriRequest) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(createSSLConnSocketFactory())
                .setConnectionManager(HTTP_CLIENT_CONNECTION_MANAGER)
                .setDefaultRequestConfig(REQUEST_CONFIG)
                .build();
             CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpUriRequest)) {

            Response response = new Response();
            response.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
            response.setContentType(closeableHttpResponse.getEntity().getContentType().getValue());
            response.setContent(EntityUtils.toByteArray(closeableHttpResponse.getEntity()));

            return response;
        } catch (Exception er) {
            er.printStackTrace();
            throw er;
        }
    }

    /**
     * 创建SSL安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslConnectionSocketFactory = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();

            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

            });
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new RuntimeException("create SSLConnectionSocketFactory is wrong.");
        }
        return sslConnectionSocketFactory;
    }

    /**
     * 拼接参数（url后缀形式）
     *
     * @param params
     * @return
     */
    private static StringBuffer buildUrlParams(Map<String, Object> params) {
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            param.append(i == 0 ? "?" : "&");
            param.append(entry.getKey()).append("=").append(entry.getValue());
            i++;
        }
        return param;
    }


}
