package com.github.fhr.toolkit4java.http;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fan Huaran
 * created on 2019/10/15
 * @description Http辅助组件
 */
public class HttpTool {
    // 默认连接池总大小
    private static final int DEFAULT_MAX_TOTAL = 100;
    // 默认每个domain的连接最大限制，MAX_PER_ROUTE<=MAX_TOTAL才有意义
    private static final int DEFAULT_MAX_PER_ROUTE = DEFAULT_MAX_TOTAL;
    // 默认可用空闲连接过期时间,重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建
    private static final int DEFAULT_VALIDATE_AFTER_INACTIVITY = 10 * 1000;
    // 默认连接超时时间
    private static final int DEFAULT_CONNECTION_TIMEOUT = 7000;
    // 默认socket读取数据超时时间
    private static final int DEFAULT_SOCKET_TIMEOUT = 7000;
    // 默认从连接池当中获取时间的超时时间
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 7000;
    // https
    public static final String HTTPS = "https";

    /**
     * 连接池总大小
     */
    private int maxTotal = DEFAULT_MAX_TOTAL;
    /**
     * 每个domain的连接最大限制，MAX_PER_ROUTE<=MAX_TOTAL才有意义
     */
    private int maxPerRoute = DEFAULT_MAX_PER_ROUTE;
    /**
     * 可用空闲连接过期时间,重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建
     */
    private int validateAfterInactivity = DEFAULT_VALIDATE_AFTER_INACTIVITY;
    /**
     * 连接超时时间
     */
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    /**
     * socket读取数据超时时间
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    /**
     * 从连接池当中获取时间的超时时间
     */
    private int connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;

    /**
     * http连接池管理器
     */
    private volatile PoolingHttpClientConnectionManager httpClientConnectionManager;
    /**
     * 请求配置
     */
    private volatile RequestConfig requestConfig;
    /**
     * 初始化标记位
     */
    private volatile boolean init;

    /**
     * 初始化lock
     */
    private volatile Object initLocker = new Object();

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public int getValidateAfterInactivity() {
        return validateAfterInactivity;
    }

    public void setValidateAfterInactivity(int validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    /**
     * 初始化
     *
     * @return
     */
    public void init() {
        if (!init) {
            synchronized (initLocker) {
                if (!init) {
                    doInit();
                }
            }
        }
    }

    /**
     * 实际的初始化操作
     *
     * @return true
     */
    private void doInit() {
        // step1 配置连接池管理器
        httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        httpClientConnectionManager.setMaxTotal(maxTotal);
        // 设置每个domain的最大连接数限制
        httpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
        // 设置连接空闲时间
        httpClientConnectionManager.setValidateAfterInactivity(validateAfterInactivity);

        // step2 请求配置
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)// 设置连接超时
                .setSocketTimeout(socketTimeout)// 设置读取超时
                .setConnectionRequestTimeout(connectionRequestTimeout)// 设置从连接池获取连接实例的超时
                .build();
    }

    /**
     * 发送文件上传请求
     *
     * @param apiUrl
     * @param headers
     * @param params
     * @param files
     * @return
     * @throws IOException
     */
    public Response postWithFile(String apiUrl, Map<String, String> headers, Map<String, String> params, Map<String, File> files) throws IOException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        if (headers == null) {
            headers = Collections.emptyMap();
        }

        HttpPost httpPost = new HttpPost(apiUrl);
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

        httpPost.setConfig(requestConfig);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        //解决上传文件，文件名中文乱码问题
        builder.setCharset(StandardCharsets.UTF_8);
        //设置浏览器兼容模式
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        //将java.io.File对象添加到HttpEntity（org.apache.http.HttpEntity）对象中
        for (Map.Entry<String, File> entry : files.entrySet()) {
            builder.addPart(entry.getKey(), new FileBody(entry.getValue()));
        }
        httpPost.setEntity(builder.build());

        return doHttpExecute(httpPost, isHttps(apiUrl));
    }

    /**
     * 发送 GET 请求（HTTP），K-V形式
     *
     * @param apiUrl
     * @param headers
     * @param params
     * @return
     */
    public Response get(String apiUrl, Map<String, String> headers, Map<String, Object> params) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        if (params == null) {
            params = Collections.emptyMap();
        }

        String withParamsApiUrl = apiUrl + buildUrlParams(params);

        HttpGet httpGet = new HttpGet(apiUrl);
        httpGet.setConfig(requestConfig);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }

        return doHttpExecute(httpGet, isHttps(withParamsApiUrl));
    }

    /**
     * 发送 POST 请求（HTTP），表单提交
     *
     * @param apiUrl  API接口URL
     * @param headers header
     * @param params  参数map
     * @return
     */
    public Response postWithForm(String apiUrl, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        if (params == null) {
            params = Collections.emptyMap();
        }

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(requestConfig);

        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }

        List<NameValuePair> pairList = new ArrayList<>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            pairList.add(pair);
        }

        httpPost.setEntity(new UrlEncodedFormEntity(pairList, StandardCharsets.UTF_8));

        return doHttpExecute(httpPost, isHttps(apiUrl));
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式
     *
     * @param apiUrl  API接口URL
     * @param headers header
     * @param content json对象
     * @return
     */
    public Response postWithJson(String apiUrl, Map<String, Object> headers, Object content) throws IOException {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(requestConfig);

        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
        }

        StringEntity stringEntity = new StringEntity(JSON.toJSONString(content), "UTF-8");//解决中文乱码问题
        stringEntity.setContentEncoding(StandardCharsets.UTF_8.toString());
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);

        return doHttpExecute(httpPost, isHttps(apiUrl));
    }

    /**
     * 执行http调用
     *
     * @param httpUriRequest
     * @param useSSL
     * @return
     * @throws IOException
     */
    private Response doHttpExecute(HttpUriRequest httpUriRequest, boolean useSSL) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(httpClientConnectionManager)
                .setSSLSocketFactory(useSSL ? createSSLConnSocketFactory() : null)
                .setDefaultRequestConfig(requestConfig)
//                .setRetryHandler(new HttpRequestRetryHandler())
                .build();
             CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpUriRequest)) {
            Response response = new Response();
            response.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
            response.setHeaders(Stream.of(closeableHttpResponse.getAllHeaders()).collect(Collectors.toMap(header -> header.getName(), header -> header.getValue(), (p,q) ->p+";"+"q")));
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
        SSLConnectionSocketFactory sslConnectionSocketFactory;
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
    private StringBuffer buildUrlParams(Map<String, Object> params) {
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (StringUtils.isBlank(key) || value == null) {
                continue;
            }
            param.append(i == 0 ? "?" : "&");

            param.append(urlEncode(key))
                    .append("=")
                    .append(urlEncode(String.valueOf(value)));
            i++;
        }
        return param;
    }

    /**
     * url编码
     *
     * @param value
     * @return
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // ignore
            return null;
        }
    }

    /**
     * 是否是https请求
     *
     * @param url
     * @return
     */
    private static boolean isHttps(String url) {
        return StringUtils.startsWithIgnoreCase(url, HTTPS);
    }

}
