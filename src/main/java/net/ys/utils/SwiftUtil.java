package net.ys.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;

/**
 * User: LiWenC
 * Date: 18-1-11
 */
public class SwiftUtil {

    public static void main(String[] args) throws IOException {
        Map<String, Header> map = genUrlAndToken("http://10.30.30.101/auth/v1.0", "test:tester", "testing");
        Header storageUrl = map.get("storageUrl");
        Header authToken = map.get("authToken");
        Header authTokenExpires = map.get("authTokenExpires");//authToken有过期时间，实际开发应该实时获取或者判断token是否过期，若过期需要重新获取

        System.out.println("storageUrl:" + storageUrl.getValue());
        System.out.println("authToken:" + authToken.getValue());
        System.out.println("authTokenExpires:" + authTokenExpires.getValue());

        long start = System.currentTimeMillis();

//        createContainer(storageUrl, authToken, "files");
//        uploadFile(storageUrl, authToken, "cjsTest", "e:/tt/", "1.zip");

        FileInputStream fis = new FileInputStream("f:/maven.zip");
        upload(storageUrl, authToken, fis, "new_files", System.currentTimeMillis() + ".zip");

//        downloadFile(storageUrl, authToken, "files", "e:", "1516681784028.png");
//        deleteFile(storageUrl, authToken, "files", "1517291790631.zip");
//        deleteContainer(storageUrl, authToken, "files");

//        System.out.println(getContainers(storageUrl, authToken));
//        System.out.println(getObjects(storageUrl, authToken, "files"));

//        new SwiftUtil().threadExec(storageUrl, authToken);
        System.out.println("use time:" + (System.currentTimeMillis() - start));
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
            map.put("storageUrl", storageUrl);
            map.put("authToken", authToken);
            map.put("authTokenExpires", authTokenExpires);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 创建Container
     *
     * @param storageUrl
     * @param authToken
     * @param containerName
     */
    public static void createContainer(Header storageUrl, Header authToken, String containerName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPut hpp = new HttpPut(storageUrl.getValue() + "/" + containerName);
            hpp.addHeader(authToken);
            CloseableHttpResponse rsp = httpClient.execute(hpp);
            Header[] headers = rsp.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                System.out.println(headers[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件
     *
     * @param storageUrl
     * @param authToken
     * @param containerName
     * @param filePath
     * @param fileName
     */
    public static void uploadFile(Header storageUrl, Header authToken, String containerName, String filePath, String fileName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            File file = new File(filePath + "/" + fileName);
            FileEntity fileEntity = new FileEntity(file);
            fileEntity.setContentType("text/plain; charset=\"UTF-8\"");
            HttpPut httpPost = new HttpPut(storageUrl.getValue() + "/" + containerName + "/" + System.currentTimeMillis() + ".zip");
            httpPost.setHeader(authToken);
            httpPost.setEntity(fileEntity);
            CloseableHttpResponse rsp = httpClient.execute(httpPost);
            Header[] headers = rsp.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
//                System.out.println(headers[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件流上传
     *
     * @param stream 文件流
     * @throws java.io.IOException
     */
    public static void upload(Header storageUrl, Header authToken, InputStream stream, String containerName, String fileName) throws IOException {
        HttpPut httpPut = new HttpPut(storageUrl.getValue() + "/" + containerName + "/" + fileName);
        InputStreamEntity inputStreamEntity = new InputStreamEntity(stream);
        httpPut.setEntity(inputStreamEntity);
        httpPut.setHeader(authToken);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpPut);
        try {
            int code = response.getStatusLine().getStatusCode();
//            if (code == 200) {
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            EntityUtils.consume(entity);
            String dd = result.toString().replaceAll("'", "\"");
            System.out.println(dd);
//            }
        } finally {
            response.close();
        }
    }

    /**
     * 下载文件
     *
     * @param storageUrl
     * @param authToken
     * @param containerName
     * @param storePath
     * @param fileName
     */
    public static void downloadFile(Header storageUrl, Header authToken, String containerName, String storePath, String fileName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(storageUrl.getValue() + "/" + containerName + "/" + fileName);
            httpget.addHeader(authToken);
            HttpResponse response = httpClient.execute(httpget);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println(entity.getContentType());
                    System.out.println(entity.isStreaming());
                    File storeFile = new File(storePath + "/" + System.currentTimeMillis() + ".png");
                    FileOutputStream output = new FileOutputStream(storeFile);
                    InputStream input = entity.getContent();
                    byte b[] = new byte[1024];
                    int j;
                    while ((j = input.read(b)) != -1) {
                        output.write(b, 0, j);
                    }
                    output.flush();
                    output.close();
                }
                if (entity != null) {
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除容器(容器内不能有对象)
     *
     * @param storageUrl
     * @param authToken
     * @param containerName
     */
    public static void deleteContainer(Header storageUrl, Header authToken, String containerName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpDelete httpDelete = new HttpDelete(storageUrl.getValue() + "/" + containerName);
            httpDelete.addHeader(authToken);
            HttpResponse response = httpClient.execute(httpDelete);
            int code = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == code) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream input = entity.getContent();
                    ByteArrayOutputStream aos = new ByteArrayOutputStream();
                    byte b[] = new byte[1024];
                    int j;
                    while ((j = input.read(b)) != -1) {
                        aos.write(b, 0, j);
                    }
                    String result = new String(aos.toByteArray());
                    System.out.println(result);
                }
                if (entity != null) {
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param storageUrl
     * @param authToken
     * @param containerName
     * @param fileName
     */
    public static void deleteFile(Header storageUrl, Header authToken, String containerName, String fileName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpDelete httpDelete = new HttpDelete(storageUrl.getValue() + "/" + containerName + "/" + fileName);
            httpDelete.addHeader(authToken);
            HttpResponse response = httpClient.execute(httpDelete);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream input = entity.getContent();
                    ByteArrayOutputStream aos = new ByteArrayOutputStream();
                    byte b[] = new byte[1024];
                    int j;
                    while ((j = input.read(b)) != -1) {
                        aos.write(b, 0, j);
                    }
                    String result = new String(aos.toByteArray());
                    System.out.println(result);
                }
                if (entity != null) {
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取容器列表
     *
     * @param storageUrl
     * @param authToken
     */
    public static List<String> getContainers(Header storageUrl, Header authToken) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(storageUrl.getValue());
            httpGet.addHeader(authToken);
            HttpResponse response = httpClient.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == code) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream input = entity.getContent();
                    ByteArrayOutputStream aos = new ByteArrayOutputStream();
                    byte b[] = new byte[1024];
                    int j;
                    while ((j = input.read(b)) != -1) {
                        aos.write(b, 0, j);
                    }
                    String result = new String(aos.toByteArray());
                    return Arrays.asList(result.split("\n"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }

    /**
     * 获取容器内对象列表
     *
     * @param storageUrl
     * @param authToken
     * @param containerName 容器名称
     */
    public static List<String> getObjects(Header storageUrl, Header authToken, String containerName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(storageUrl.getValue() + "/" + containerName);
            httpGet.addHeader(authToken);
            HttpResponse response = httpClient.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == code) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream input = entity.getContent();
                    ByteArrayOutputStream aos = new ByteArrayOutputStream();
                    byte b[] = new byte[1024];
                    int j;
                    while ((j = input.read(b)) != -1) {
                        aos.write(b, 0, j);
                    }
                    String result = new String(aos.toByteArray());
                    return Arrays.asList(result.split("\n"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }
}
