package com.lkc.yupao.once.HttpClient;

//import com.monkey.java_study.common.entity.PageEntity;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * HttpClient请求接口测试样例.
 *
 * @author xindaqi
 * @date 2022-01-12 11:34
 */
public class HttpClientTest {

    private static final Logger logger = LogManager.getLogger(HttpClientTest.class);

    /**
     * GET请求.
     *
     * @param url 请求地址
     * @return 响应数据
     */
    public static String doGet(String url) {
        try {
            // 创建客户端
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            // 建立连接
            HttpGet httpGet = new HttpGet(url);
            // 请求配置：超时时间，单位：毫秒
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            httpGet.setConfig(requestConfig);
            // 设置请求头：内容类型
            httpGet.setHeader("Content-Type", "application/json;charset=UTF-8");

            return getResponse(httpClientBuilder, httpGet);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * POST请求：携带表单数据form-data.
     *
     * @param url      请求地址
     * @param paramMap 表单数据
     * @return 响应数据
     */
    public static String doPostFormData(String url, Map<String, Object> paramMap) {
        try {
            // 创建客户端
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            // 建立连接
            HttpPost httpPost = new HttpPost(url);
            // 请求配置：超时时间，单位：毫秒
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            httpPost.setConfig(requestConfig);
            // 设置请求头：内容类型
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            if (Objects.nonNull(paramMap) && !paramMap.isEmpty()) {
                List<NameValuePair> formDataList = new ArrayList<>();
                Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();
                for (Map.Entry<String, Object> mapEntry : entrySet) {
                    formDataList.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue().toString()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(formDataList, "UTF-8"));
            }

            return postResponse(httpClientBuilder, httpPost);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * POST请求：携带JSON数据.
     *
     * @param url    请求地址
     * @param params 请求数据：JSON字符串
     * @return 响应数据
     */
    public static String doPostJson(String url, String params) {
        try {
            // 创建客户端
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            // 建立连接
            HttpPost httpPost = new HttpPost(url);
            // 请求配置：超时时间，单位：毫秒
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            httpPost.setConfig(requestConfig);
            // 设置请求头：内容类型
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            StringEntity stringEntity = new StringEntity(params);
            stringEntity.setContentType("text/json");
            httpPost.setEntity(stringEntity);
            return postResponse(httpClientBuilder, httpPost);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 获取GET请求数据.
     *
     * @param httpClientBuilder 客户端对象
     * @param httpGet           GET请求
     * @return 响应数据
     */
    public static String getResponse(HttpClientBuilder httpClientBuilder, HttpGet httpGet) {
        try (CloseableHttpResponse closeableHttpResponse = httpClientBuilder.build().execute(httpGet)) {
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return EntityUtils.toString(httpEntity, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 获取POST请求数据.
     *
     * @param httpClientBuilder 客户端对象
     * @param httpPost          POST请求
     * @return 响应数据
     */
    public static String postResponse(HttpClientBuilder httpClientBuilder, HttpPost httpPost) {
        try (CloseableHttpResponse closeableHttpResponse = httpClientBuilder.build().execute(httpPost)) {
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return EntityUtils.toString(httpEntity, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {

        // GET：请求
        String getUrl = "https://blog.csdn.net/Xin_101/article/details/122440247";
        String getResponse = doGet(getUrl);
        logger.info(">>>>>>>>>Get response:{}", getResponse);

//         POST：请求携带form-data参数
//        String postFormDataUrl = "http://localhost:9121/api/v1/parameter/annotation/form-data";
//        Map<String, Object> mapFormData = new HashMap<>();
//        mapFormData.put("name", "xiaohong");
//        String formDataResponse = doPostFormData(postFormDataUrl, mapFormData);
//        logger.info(">>>>>>>>>Post form data response:{}", formDataResponse);
//
//         POST：请求携带JSON参数
//        String postUrl = "http://localhost:9121/api/v1/mongodb/page";
//         入参实体
//        PageEntity pageEntity = new PageEntity(1, 2);
//        Gson gson = new Gson();
//         实体转JSON字符串
//        String jsonString = gson.toJson(pageEntity);
//        String postResponse = doPostJson(postUrl, jsonString);
//        logger.info(">>>>>>>>>>Post json response:{}", postResponse);
    }
}

