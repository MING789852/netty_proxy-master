package com.xm.netty_proxy_server.util.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

public  class HttpUtils {
    protected static Logger logger= LoggerFactory.getLogger(HttpUtils.class);

    //参数为信任的证书，信任策略
    private static HttpClient sslClient() {
        try {
            // 在调用SSL之前需要重写验证方法，取消检测SSL
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String str) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String str) {
                }
            };
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{trustManager}, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            // 创建Registry
            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(Boolean.TRUE).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", socketFactory).build();
            // 创建ConnectionManager，添加Connection配置信息
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig).build();
            return closeableHttpClient;
        } catch (KeyManagementException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * post请求返回json格式
     * @param url post请求url
     * @param headers  post请求头
     * @param dataMap post请求体
     * @return
     */
    public static String doPost(String url,JSONObject headers,String dataMap){
        try {
            HttpResponse httpResponse=doPostAsHttpResponse(url,headers,dataMap);
            if (httpResponse!=null){
                return EntityUtils.toString(httpResponse.getEntity());
            }else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get请求返回json格式
     * @param url get请求url
     * @param headers get请求头
     * @return
     */
    public static String doGet(String url,JSONObject headers){
        try {
            HttpResponse httpResponse=doGetAsHttpResponse(url,headers);
            if (httpResponse!=null){
                return EntityUtils.toString(httpResponse.getEntity());
            }else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * post请求
     * @param url post请求url
     * @param headers  post请求头
     * @param dataMap post请求体
     * @return
     */
    public static HttpResponse doPostAsHttpResponse(String url, JSONObject headers, String dataMap){
        logger.info("");
        logger.info("发送http post请求开始 \n url->{} \n head->{} \n body->{}",url,headers,dataMap);
        HttpClient httpClient=sslClient();
        HttpPost httpPost=new HttpPost(url);
        //设置连接超时
        httpPost.setConfig(setTimeOutConfig(httpPost.getConfig()));
        //设置请求头
        if (headers!=null){
            for (String header:headers.keySet()){
                httpPost.setHeader(header,headers.getString(header));
            }
        }

        //设置requetBody
        StringEntity stringEntity=new StringEntity(dataMap, Charset.defaultCharset());
        stringEntity.setContentType(ContentType.APPLICATION_JSON.toString());

        httpPost.setEntity(stringEntity);

        try {
            HttpResponse httpResponse=httpClient.execute(httpPost);
            return httpResponse;
        } catch (IOException e) {
            logger.error("Post请求错误",e);
            return null;
        }
    }

    /**
     * get请求
     * @param url get请求url
     * @param headers get请求头
     * @return
     */
    public static HttpResponse doGetAsHttpResponse(String url, JSONObject headers){
        logger.info("");
        logger.info("发送http get请求开始 \n url->{} \n head->{}",url,headers);
        HttpClient httpClient=sslClient();
        HttpGet httpGet=new HttpGet(url);
        //设置连接超时
        httpGet.setConfig(setTimeOutConfig(httpGet.getConfig()));
        //设置请求头
        if (headers!=null){
            for (String header:headers.keySet()){
                httpGet.setHeader(header,headers.getString(header));
            }
        }
        try {
            HttpResponse httpResponse =httpClient.execute(httpGet);
            return httpResponse;
        } catch (IOException e) {
            logger.error("Get请求错误 {}",e.getMessage());
            return null;
        }

    }

    /**
     * 设置 连接超时、 请求超时 、 读取超时  毫秒
     * @param requestConfig
     * @return
     */
    private static RequestConfig setTimeOutConfig(RequestConfig requestConfig) {
        if (requestConfig == null) {
            requestConfig = RequestConfig.DEFAULT;
        }
        return RequestConfig.copy(requestConfig)
                .setConnectionRequestTimeout(30000)
                .setConnectTimeout(30000)
                .setSocketTimeout(10000)
                .build();
    }


    /**
     * 构建请求url
     * @param url
     * @param queryMap url参数
     * @return
     */
    public static String buildUrl(String url, Map<String, String> queryMap) {
        StringBuffer sbUrl = new StringBuffer();
        if (!StringUtils.isBlank(url)) {
            sbUrl.append(url);
        }
        if (null != queryMap) {
            StringBuffer sbQuery = new StringBuffer();
            for (Map.Entry<String, String> query : queryMap.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append("&");
                }
                if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (!StringUtils.isBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (!StringUtils.isBlank(query.getValue())) {
                        sbQuery.append("=");
                        try {
                            sbQuery.append(URLEncoder.encode(query.getValue(), "utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append("?").append(sbQuery);
            }
        }
        return sbUrl.toString();
    }
}
