package com.github.fhr.toolkit4java.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fhr.toolkit4java.http.HttpTool;
import com.github.fhr.toolkit4java.http.Response;
import com.github.fhr.toolkit4java.image.ImageUtils;
import com.github.fhr.toolkit4java.security.MD5Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Fan Huaran
 * created on 2019/10/30
 * @description
 */
public class LubanTemplateCrawler {
    private static final Logger logger = LoggerFactory.getLogger("LubanTemplateCrawlerLogger");
    private static final String TEMPLATE_LIST_URL = "https://luban.aliyun.com/api/jsonrpc/officialTemplate?_tag=getTemplates";
    private static final String COPY_TEMPLATE_URL = "Https://luban.aliyun.com/api/jsonrpc/newtopic?_tag=copy";
    private static final String QUERY_DATA_URL = "https://luban.aliyun.com/api/jsonrpc/newtopic?_tag=queryData";
    private static final String GET_DATA_URL = "https://luban.aliyun.com/api/jsonrpc/newtopic?_tag=getData";
    private static final String REMOVE_DATA_URL = "https://luban.aliyun.com/api/jsonrpc/newtopic?_tag=removeTopic";

    private String csrfToken;
    private String cookie;
    private String templateListUrl;
    private String copyTemplateUrl;
    private String queryDataUrl;
    private String getDataUrl;
    private String removeDataUrl;
    private String savePath;

    private final HttpTool httpTool = new HttpTool();
    private final AtomicLong idGenerator = new AtomicLong(1572414942190L);

    public LubanTemplateCrawler(String csrfToken, String cookie, String savePath) {
        this.csrfToken = csrfToken;
        this.cookie = cookie;
        this.templateListUrl = TEMPLATE_LIST_URL + "&_csrf=" + csrfToken;
        this.copyTemplateUrl = COPY_TEMPLATE_URL + "&_csrf=" + csrfToken;
        this.queryDataUrl = QUERY_DATA_URL + "&_csrf=" + csrfToken;
        this.getDataUrl = GET_DATA_URL + "&_csrf=" + csrfToken;
        this.removeDataUrl = REMOVE_DATA_URL + "&_csrf=" + csrfToken;
        this.savePath = savePath;
    }

    /**
     * save templates
     *
     * @throws IOException
     */
    public void crawTemplates() throws IOException {
        String responseStr = this.getTemplateList();
        JSONObject response = JSONObject.parseObject(responseStr);
        JSONObject result = response.getJSONObject("result");
        JSONArray array = result.getJSONArray("data");
        logger.info("total count:{}", array.size());
        int successCount = 0;
        int failCount = 0;
        for (Object value : array) {
            boolean success = this.saveTemplate((JSONObject) value);
            if (success) {
                successCount++;
            } else {
                failCount++;
            }
        }
        logger.info("success:{},fail:{}", successCount, failCount);
    }

    private boolean saveTemplate(JSONObject templateInfo) {
        try {
            Long templateId = templateInfo.getLong("templateId");
            String name = templateInfo.getString("name");
            String previewUrl = templateInfo.getString("previewUrl");
            String templateDirName = String.format("%s%s%s_%s", savePath, File.separator, name, templateId);
            String infoFileName = templateDirName + File.separator + "info.json";
            String previewImageFileName = templateDirName + File.separator + "preview.png";
            String metaFileName = templateDirName + File.separator + "meta.json";
            String resourceDir = templateDirName + File.separator + "resources";

            // 不需要重复爬取
            if (new File(infoFileName).exists() && new File(previewImageFileName).exists() && new File(metaFileName).exists()) {
                return true;
            }

            try {
                Thread.sleep(15000 + new Random().nextInt(5000));
            } catch (InterruptedException e) {
                // ignore
            }

            ImageUtils.saveImageToFile(ImageIO.read(new URL(previewUrl)), "png", previewImageFileName);
            FileUtils.write(new File(infoFileName), JSON.toJSONString(templateInfo));
            String templateMeta = getTemplateMeta(templateId);
            FileUtils.write(new File(metaFileName), templateMeta);
            JSONObject metaObject = JSON.parseObject(templateMeta);
            JSONObject resultObject = metaObject.getJSONObject("result");
            String backgroundUrl = resultObject.getString("backgroundUrl");
            if (StringUtils.isNotBlank(backgroundUrl)) {
                saveImage(backgroundUrl, resourceDir);
            }
            JSONArray layers = resultObject.getJSONArray("layers");
            if (layers != null) {
                for (Object value : layers) {
                    JSONObject layer = (JSONObject) value;
                    String picUrl = layer.getString("picUrl");
                    if (StringUtils.isNotBlank(picUrl)) {
                        saveImage(picUrl, resourceDir);
                    }
                }
            }

            logger.info("save template success,templateId:{}", templateId);
            return true;
        } catch (Exception er) {
            logger.error("save template fail,templateInfo:{}", templateInfo, er);
            return false;
        }
    }

    private void saveImage(String imageUrl, String resourceDir) {
        String imageFile = resourceDir + File.separator + MD5Utils.md5(imageUrl) + MD5Utils.md5(imageUrl + "dummy") + ".png";

        ImageUtils.saveImageToFile(ImageUtils.readImageFromUrl(imageUrl), "png", imageFile);
    }

    private String getTemplateList() throws IOException {
        Map<String, Object> headers = getHeaders();
        JSONObject content = JSONObject.parseObject("{\"jsonrpc\":2,\"method\":\"getTemplates\",\"params\":[{\"name\":\"\",\"cat1\":\"\",\"cat2\":\"\",\"cat3\":\"\",\"cat4\":\"\",\"pageSize\":1500,\"pageNumber\":1}],\"id\":1572403714632}");
        Response response = httpTool.postWithJson(templateListUrl, headers, content);
        String data = new String(response.getContent(), StandardCharsets.UTF_8);
        logger.info("<template_list> statusCode:{}", response.getStatusCode());
        logger.info("<template_list> data:{}", data);
        return data;
    }

    private String getTemplateMeta(long templateId) throws IOException {
        Long copyId1 = copyTemplate(templateId);
        if (copyId1 == null) {
            throw new RuntimeException("clone fail:" + templateId);
        }
        Long copyId2 = queryData(copyId1);
        if (copyId2 == null) {
            throw new RuntimeException("clone fail:" + templateId);
        }
        String data = getData(copyId1, copyId2);

        removeTemplate(copyId1);

        return data;
    }

    private String removeTemplate(long templateId) throws IOException {
        Map<String, Object> headers = getHeaders();

        JSONArray params = new JSONArray();
        params.add(templateId);

        JSONObject content = new JSONObject();
        content.put("jsonrpc", 2);
        content.put("method", "removeTopic");
        content.put("params", params);
        content.put("id", idGenerator.incrementAndGet());

        Response response = httpTool.postWithJson(removeDataUrl, headers, content);
        String data = new String(response.getContent(), StandardCharsets.UTF_8);
        logger.info("<removeTemplate> statusCode:{}", response.getStatusCode());
        logger.info("<removeTemplate> data:{}", data);
        return data;
    }

    private Long copyTemplate(long targetId) throws IOException {
        Map<String, Object> headers = getHeaders();

        JSONArray params = new JSONArray();
        JSONObject param = new JSONObject();
        param.put("targetId", targetId);
        param.put("copyType", 1);
        param.put("scene", "officialTemplate");
        params.add(param);

        JSONObject content = new JSONObject();
        content.put("jsonrpc", 2);
        content.put("method", "copy");
        content.put("params", params);
        content.put("id", idGenerator.incrementAndGet());

        Response response = httpTool.postWithJson(copyTemplateUrl, headers, content);
        String data = new String(response.getContent(), StandardCharsets.UTF_8);
        logger.info("<copyTemplate> statusCode:{}", response.getStatusCode());
        logger.info("<copyTemplate> data:{}", data);
        return JSONObject.parseObject(data).getLong("result");
    }

    private Long queryData(Long copyId1) throws IOException {
        Map<String, Object> headers = getHeaders();
        JSONArray params = new JSONArray();
        JSONObject param = new JSONObject();
        param.put("topicId", copyId1);
        params.add(param);

        JSONObject content = new JSONObject();
        content.put("jsonrpc", 2);
        content.put("method", "queryData");
        content.put("params", params);
        content.put("id", idGenerator.incrementAndGet());

        Response response = httpTool.postWithJson(queryDataUrl, headers, content);
        String data = new String(response.getContent(), StandardCharsets.UTF_8);

        logger.info("<queryData> statusCode:{}", response.getStatusCode());
        logger.info("<queryData> data:{}", data);

        JSONObject object = JSONObject.parseObject(data);
        JSONArray result = object.getJSONArray("result");
        if (result != null && !result.isEmpty()) {
            return ((JSONObject) (result.get(0))).getLong("id");
        }
        return null;
    }

    private String getData(Long copyId1, Long copyId2) throws IOException {
        Map<String, Object> headers = getHeaders();
        JSONArray params = new JSONArray();
        params.add(copyId1);
        params.add(copyId2);

        JSONObject content = new JSONObject();
        content.put("jsonrpc", 2);
        content.put("method", "getData");
        content.put("params", params);
        content.put("id", idGenerator.incrementAndGet());
        Response response = httpTool.postWithJson(getDataUrl, headers, content);
        String data = new String(response.getContent(), StandardCharsets.UTF_8);

        logger.info("<getData> statusCode:{}", response.getStatusCode());
        logger.info("<getData> data:{}", data);

        return data;
    }

    private Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<>();

        headers.put("Cookie", cookie);
        headers.put("Host", "luban.aliyun.com");
        headers.put("Origin", "https://luban.aliyun.com");
        headers.put("Pragma", "no-cache");
        headers.put("Referer", "https://luban.aliyun.com/web/topic_total");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36");
        headers.put("X-XSRF-TOKEN", csrfToken);

        return headers;
    }

}
