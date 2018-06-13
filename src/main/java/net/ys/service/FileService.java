package net.ys.service;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: LiWenC
 * Date: 18-1-11
 */
public class FileService {

    static Header storageUrl;
    static Header authToken;
    static Header authTokenExpires;

    static {
        //authToken有过期时间，为了测试搞成静态的，实际开发应该实时获取或者判断token是否过期，若过期需要重新获取
        Map<String, Header> map = genUrlAndToken("http://10.30.30.101/auth/v1.0", "test:tester", "testing");
        storageUrl = map.get("storageUrl");
        authToken = map.get("authToken");
        authTokenExpires = map.get("authTokenExpires");//单位秒
    }

    public static boolean upload(InputStream fis, String fileName) throws IOException {
        if (storageUrl == null || authToken == null) {
            return false;
        }
        return upload(storageUrl, authToken, fis, "zl_files", fileName);
    }

    public static HttpEntity download(String fileName) throws IOException {
        if (storageUrl == null || authToken == null) {
            return null;
        }
        return downloadFile("zl_files", fileName);
    }

    /**
     * 测试账号，并且获得url和Token信息
     */
    public static Map<String, Header> genUrlAndToken(String url, String user, String password) {
        Map<String, Header> map = new HashMap<String, Header>();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet req = new HttpGet(url);
            req.addHeader("X-Storage-User", user);
            req.addHeader("X-Storage-Pass", password);
            HttpResponse rsp = httpClient.execute(req);
            Header storageUrl = rsp.getFirstHeader("X-Storage-Url");
            Header authToken = rsp.getFirstHeader("X-Auth-Token");
            Header authTokenExpires = rsp.getFirstHeader("X-Auth-Token-Expires");

            System.out.println("storageUrl:" + storageUrl.getValue());
            System.out.println("authToken:" + authToken.getValue());
            System.out.println("authTokenExpires:" + authTokenExpires.getValue());

            map.put("storageUrl", storageUrl);
            map.put("authToken", authToken);
            map.put("authTokenExpires", authTokenExpires);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 文件流上传
     *
     * @param stream 文件流
     * @throws java.io.IOException
     */
    public static boolean upload(Header storageUrl, Header authToken, InputStream stream, String containerName, String fileName) throws IOException {
        HttpPut httpPut = new HttpPut(storageUrl.getValue() + "/" + containerName + "/" + fileName);
        InputStreamEntity inputStreamEntity = new InputStreamEntity(stream);
        httpPut.setEntity(inputStreamEntity);
        httpPut.setHeader(authToken);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpPut);
        try {
            return response.getStatusLine().getStatusCode() < 300;
        } finally {
            response.close();
        }
    }

    /**
     * 下载文件
     *
     * @param containerName
     * @param fileName
     */
    public static HttpEntity downloadFile(String containerName, String fileName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(storageUrl.getValue() + "/" + containerName + "/" + fileName);
            httpget.addHeader(authToken);
            HttpResponse response = httpClient.execute(httpget);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                return response.getEntity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
