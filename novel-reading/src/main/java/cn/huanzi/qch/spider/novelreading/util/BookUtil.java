package cn.huanzi.qch.spider.novelreading.util;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * 小工具类
 */
public class BookUtil {

    //创建httpclient对象 (这里设置成全局变量，相对于同一个请求session、cookie会跟着携带过去)
    private static CloseableHttpClient httpClient;

    static{
        try {
            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySSL();

            // 设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(new PoolingHttpClientConnectionManager(socketFactoryRegistry));

            //创建自定义的httpclient对象
            httpClient = httpClientBuilder.build();

            //获取默认对象
//            CloseableHttpClient httpClient = HttpClients.createDefault();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动注入参数
     * 例如：
     *
     * @param src    http://search.zongheng.com/s?keyword=#1&pageNo=#2&sort=
     * @param params "斗破苍穹","1"
     * @return http://search.zongheng.com/s?keyword=斗破苍穹&pageNo=1&sort=
     */
    public static String insertParams(String src, String... params) {
        int i = 1;
        for (String param : params) {
            src = src.replaceAll("#" + i, param);
            i++;
        }
        return src;
    }

    /**
     * 采集当前url完整response实体.toString()
     *
     * @param url url
     * @return response实体.toString()
     */
    public static String gather(String url, String refererUrl) {
        String result = null;
        try {
            //创建get方式请求对象
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Content-type", "application/json");
            //包装一下
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
            httpGet.addHeader("Referer", refererUrl);
            httpGet.addHeader("Connection", "keep-alive");

            //通过请求对象获取响应对象
            CloseableHttpResponse response = httpClient.execute(httpGet);
            //获取结果实体
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity(), "GBK");
            }

            //释放链接
            response.close();
        }
        //这里还可以捕获超时异常，重新连接抓取
        catch (Exception e) {
            result = null;
            System.err.println("采集操作出错");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 绕过SSL验证
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");

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

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }
}
