package cn.huanzi.qch.commonspider.util;

import cn.huanzi.qch.commonspider.pojo.IpProxy;
import cn.huanzi.qch.commonspider.pojo.UserAgent;
import cn.huanzi.qch.commonspider.vo.ResultVo;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

/**
 * HttpClient工具类
 * <p>
 * HttpClient是http包下面的东西，可以简单发起请求获取数据，但不会去解析DOM、执行js、css等
 * 因此需要借助Jsoup来解析Html文档
 */
@Slf4j//使用lombok的@Slf4j，帮我们创建Logger对象
public class HttpClientUtil {
    //IP代理池
    private static List<IpProxy> ipProxyPool;

    //User-Agent池
    private static List<UserAgent> userAgentPool;

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
            HttpClientBuilder httpClientBuilder = HttpClients.custom().
                    // 设置协议http和https对应的处理socket链接工厂的对象
                            setConnectionManager(new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.INSTANCE)
                            .register("https", new SSLConnectionSocketFactory(sslcontext))
                            .build()));

            //创建自定义的httpclient对象
            httpclient = httpClientBuilder.build();
        } catch (Exception e) {
            log.info("获取HttpClient失败，原因：" + e.getMessage());
        }
        return httpclient;
    }

    /**
     * 更新IP代理、User Agent池
     */
    public static void updateIpProxyPoolAndUserAgentPool(List<IpProxy> ipProxyPools, List<UserAgent> userAgentPools) {
        ipProxyPool = ipProxyPools;
        log.info("HttpClientUtil的IP代理池更新成功，大小：" + ipProxyPool.size());
        userAgentPool = userAgentPools;
        log.info("HttpClientUtil的User Agent池更新成功，大小：" + userAgentPool.size());
    }

    /**
     * 为HttpGet更新IP代理、User Agent
     */
    private static void updateIpProxyAndUserAgentForHttpGet(HttpGet httpGet) {
        //设置IP代理，每次都取不同的
        if (ipProxyPool != null && ipProxyPool.size() > 0) {
            IpProxy ipProxy = ipProxyPool.get(randomNumber(0, ipProxyPool.size() - 1));
            log.info("更新代理IP，当前使用：" + ipProxy.toString());
            httpGet.setConfig(RequestConfig.custom().setProxy(new HttpHost(ipProxy.getIp(), Integer.parseInt(ipProxy.getPort()))).build());
        }
        //更新User-Agent
        if (userAgentPool != null && userAgentPool.size() > 0) {
            UserAgent userAgent = userAgentPool.get(randomNumber(0, userAgentPool.size() - 1));
            log.info("更新User-Agent，当前使用：" + userAgent.getUserAgent());
            httpGet.addHeader("User-Agent", userAgent.getUserAgent());
        } else {
            //需要设置默认User-Agent，否则自带的值是：User-Agent: Apache-HttpClient/4.5.4 (Java/1.8.0_131)
            BrowserVersion[] versions = {BrowserVersion.FIREFOX_60, BrowserVersion.FIREFOX_52, BrowserVersion.INTERNET_EXPLORER, BrowserVersion.CHROME, BrowserVersion.EDGE};
            httpGet.addHeader("User-Agent", versions[(int) (versions.length * Math.random())].getUserAgent());
        }
    }

    /**
     * 根据一个url发起get请求
     */
    public static ResultVo<String> gather(HttpClient httpClient, String url, String refererUrl, List<Map<String, String>> headers) throws IOException {
        String result = null;
        //创建get方式请求对象
        HttpGet httpGet = new HttpGet(url);

        //更新代理IP、UA
        updateIpProxyAndUserAgentForHttpGet(httpGet);

        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("Content-type", "application/json;charset=UTF-8");
        httpGet.addHeader("Connection", "keep-alive");
        //Referer，默认百度 https://www.baidu.com
        httpGet.addHeader("Referer", StringUtils.isEmpty(refererUrl) ? "https://www.baidu.com" : refererUrl);

        //是否还要其他的Header，可以直接在http请求的head里面携带cookie
        if (!StringUtils.isEmpty(headers)) {
            headers.forEach((header) -> {
                httpGet.addHeader(header.get("name"), header.get("value"));
            });
        }

        //通过请求对象获取响应对象
        HttpResponse response = httpClient.execute(httpGet);

        //获取结果实体
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            result = EntityUtils.toString(response.getEntity(), "UTF-8");
        }
        return ResultVo.of(statusCode, response.getStatusLine().getReasonPhrase(), result);
    }

    /**
     * 返回一个最小值-最大值的随机数
     */
    public static Integer randomNumber(Integer min, Integer max) {
        //(最小值+Math.randomNumber()*(最大值-最小值+1))
        return (int) (min + Math.random() * (max - min + 1));
    }

    /**
     * 返回一个随机指定长度的字符串
     */
    public static String randomString(Integer length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * main测试
     */
    public static void main(String[] args) {
//        HttpClient httpClient = HttpClientUtil.getHttpClient();
//        try {
//            ResultVo<String> resultVo = HttpClientUtil.gather(httpClient, "https://book.qidian.com/info/1004608738", null,null);
//
//            //获取页面源代码
//            log.info(resultVo.getPage());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
