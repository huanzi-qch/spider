package cn.huanzi.qch.commonspider.util;

import cn.huanzi.qch.commonspider.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Map;

/**
 * HttpClient工具类
 * <p>
 * HttpClient是http包下面的东西，可以简单发起请求获取数据，但不会去解析DOM、执行js、css等
 * 因此需要借助Jsoup来解析Html文档
 */
@Slf4j//使用lombok的@Slf4j，帮我们创建Logger对象
public class HttpClientUtil {

    /**
     * 获取一个HttpClient
     */
    public static HttpClient getHttpClient() {
        HttpClient httpclient = null;
        try {

            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = SSLContext.getInstance("SSLv3");
            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslcontext.init(null, new TrustManager[]{trustManager}, null);

            //获取一个构造器
            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    // 设置协议http和https对应的处理socket链接工厂的对象
                    .setConnectionManager(new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.INSTANCE)
                            .register("https", new SSLConnectionSocketFactory(sslcontext))
                            .build()))
                    //设置超时时间
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                            .setSocketTimeout(5000).build());

            //创建自定义的httpclient对象
            httpclient = httpClientBuilder.build();
        } catch (Exception e) {
            log.info("获取HttpClient失败，原因：" + e.getMessage());
        }
        return httpclient;
    }

    /**
     * 根据一个url发起get请求
     */
    public static ResultVo<HttpResponse> gatherForGet(HttpClient httpClient, String url, String refererUrl, Map<String, String> headers) throws IOException {
        //创建get方式请求对象
        HttpGet httpGet = new HttpGet(url);

        //更新代理IP、UA
        IpProxyPoolUtil.updateIpProxyAndUserAgentForHttpGetOrHttpPost(httpGet);

        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("Content-type", "text/html; charset=utf-8");
        httpGet.addHeader("Connection", "keep-alive");
        //Referer，默认百度 https://www.baidu.com
        httpGet.addHeader("Referer", StringUtils.isEmpty(refererUrl) ? "https://www.baidu.com" : refererUrl);

        //是否还要其他的Header，可以直接在http请求的head里面携带cookie
        if (!StringUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> {
                httpGet.removeHeaders(key);
                httpGet.addHeader(key, value);
            });
        }

        //通过请求对象获取响应对象
        HttpResponse response = httpClient.execute(httpGet);

        //直接响应HttpResponse
        return ResultVo.of(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), response);
    }

    /**
     * 根据一个url发起post请求
     *
     * EntityUtils.toString(httpResponse.getEntity()) ：通过InputStream，将entity转成字符串
     */
    public static ResultVo<HttpResponse> gatherForPost(HttpClient httpClient, String url, String refererUrl, Map<String, String> headers, Map<String, Object> paramMap) throws IOException {
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        /*
            服务端有@RequestBody，请求头需要设置Content-type=application/json; charset=UTF-8，同时请求参数要放在body里
         */
//        String paramJson = JSONObject.fromObject(paramMap).toString();
//        StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/json", "UTF-8"));
//        httpPost.setEntity(stringEntity);
//        httpPost.addHeader("Content-type", "application/json; charset=UTF-8");

        /*
           服务端没有@RequestBody，请求头需要设置Content-type=application/x-www-form-urlencoded; charset=UTF-8，同时请求参数要放在URL参数里
        */
        ArrayList<NameValuePair> list = new ArrayList<>();
        for (int i = 0; i < paramMap.size(); i++) {
            String key = (String) paramMap.keySet().toArray()[i];
            list.add(new BasicNameValuePair(key, (String) paramMap.get(key)));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");

        //更新代理IP、UA
        IpProxyPoolUtil.updateIpProxyAndUserAgentForHttpGetOrHttpPost(httpPost);

        httpPost.addHeader("Accept", "*/*");
        httpPost.addHeader("Connection", "keep-alive");
        //Referer，默认百度 https://www.baidu.com
        httpPost.addHeader("Referer", StringUtils.isEmpty(refererUrl) ? "https://www.baidu.com" : refererUrl);

        //是否还要其他的Header，可以直接在http请求的head里面携带cookie
        if (!StringUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> {
                httpPost.removeHeaders(key);
                httpPost.addHeader(key, value);
            });
        }

        //通过请求对象获取响应对象
        HttpResponse response = httpClient.execute(httpPost);

        //直接响应HttpResponse
        return ResultVo.of(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), response);
    }

    /**
     * main测试
     */
//    public static void main(String[] args) {
//        HttpClient httpClient = HttpClientUtil.getHttpClient();
//        try {
//            ResultVo<String> resultVo = HttpClientUtil.gatherForGet(httpClient, "https://book.qidian.com/info/1004608738", null,null);
//
//            //获取页面源代码
//            log.info(resultVo.getPage());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
