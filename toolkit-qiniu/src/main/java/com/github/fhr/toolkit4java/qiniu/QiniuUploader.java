package com.github.fhr.toolkit4java.qiniu;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;


/**
 * @author Fan Huaran
 * created on 2020/2/28
 * @description
 */
public class QiniuUploader{

    private Region region = Region.region1();
    private String accessKey;
    private String secretKey;
    private String bucket;
    /**
     * 图片访问地址
     */
    private String cdnDomain;

    private final UploadManager uploadManager;

    public QiniuUploader() {
        Configuration configuration = new Configuration(region);
        this.uploadManager = new UploadManager(configuration);
    }

//    public Region getRegion() {
//        return region;
//    }
//
//    public void setRegion(Region region) {
//        this.region = region;
//    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getCdnDomain() {
        return cdnDomain;
    }

    public void setCdnDomain(String cdnDomain) {
        this.cdnDomain = cdnDomain;
    }

    public String uploadFile(String fileName) throws Exception {
        return cdnDomain + "/" + doUpload(fileName, null);
    }

    public String uploadFile(String fileName, String path) throws Exception {
        return cdnDomain + "/" + doUpload(fileName, path);
    }


    /**
     * key就是相对访问路径，如果为null，则由七牛云存储自动生成
     *
     * @param uploadData
     * @param key
     * @return 相对路径
     */
    private String doUpload(Object uploadData, String key) throws Exception {
        if (uploadData == null){
            throw new NullPointerException("uploadData must not be  null");
        }

        // step0: 构建uploadManager
        // 已经预构建，uploadManager线程安全，且带连接池（基于okhttp），所以需要避免每次都实例化

        // step1: 生成上传凭证，然后准备上传
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            // step2: 上传
            Response response;
            if (uploadData instanceof String){
                response = uploadManager.put((String) uploadData, key, upToken);
            } else if (uploadData instanceof byte[]){
                response = uploadManager.put((byte[]) uploadData, key, upToken);
            } else {
                throw new IllegalArgumentException("un support type:" + uploadData.getClass().toString());
            }

            // step3: 解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            return putRet.key;
        } catch (QiniuException ex) {
            throw new Exception("upload fail", ex);
        }
    }

    public static void main(String[] args) {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region1());
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "9cPg4qz8nkWRwx3jdC5sN6ll2eyFAT5rDMdz4IaL";
        String secretKey = "zIzfiN3nyUI50hPYxwrQ2mDf78_XMOVu88Q7GrmP";
        String bucket = "stwww-qy-test1";
//如果是Windows情况下，格式是 D:\\qiniu\\test.png
        String localFilePath = "E:\\d2.png";
//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = "smart_image/test/1/1";
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(localFilePath, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
    }

}
