package cn.huanzi.qch.novelgather.util;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具类
 */
public class GatherUtil {

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
     * 创建.txt文件
     *
     * @param fileName 文件名（小说名）
     * @return File对象
     */
    public static File createFile(String fileName) {
        //获取桌面路径
        String comPath = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
        //创建空白文件夹：networkNovel
        File file = new File(comPath + "\\networkNovel\\" + fileName + ".txt");
        try {
            //获取父目录
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            //创建文件
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            file = null;
            System.err.println("新建文件操作出错");
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 字符流写入文件
     *
     * @param file  file对象
     * @param value 要写入的数据
     */
    private static void fileWriter(File file, String value) {
        //字符流
        try {
            FileWriter resultFile = new FileWriter(file, true);//true,则追加写入
            PrintWriter myFile = new PrintWriter(resultFile);
            //写入
            myFile.println(value);
            myFile.println("\n");

            myFile.close();
            resultFile.close();
        } catch (Exception e) {
            System.err.println("写入操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 采集当前url完整response实体.toString()
     *
     * @param url url
     * @return response实体.toString()
     */
    private static String gather(String url, String refererUrl) {
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
     * 使用jsoup处理html字符串，根据规则，得到当前章节名以及完整内容跟下一章的链接地址
     * 每个站点的代码风格都不一样，所以规则要根据不同的站点去修改
     * 比如这里的文章内容直接用一个div包起来，而有些站点是每个段落用p标签包起来
     *
     * @param html html字符串
     * @return Map<String, String>
     */
    private static Map<String, String> processor(String html) {
        HashMap<String, String> map = new HashMap<>();
        String chapterName;//章节名
        String chapter = null;//完整章节（包括章节名）
        String next = null;//下一章链接地址
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //章节名称
            Elements bookname = doc.select("div.bookname > h1");
            chapterName = bookname.text().trim();
            chapter = chapterName + "\n";

            //文章内容
            Elements content = doc.select("div#content");
            String replaceText = content.text().replace(" ", "\n");
            chapter = chapter + replaceText;

            //下一章
            Elements nextText = doc.select("a:matches((?i)下一章)");
            if (nextText.size() > 0) {
                next = nextText.attr("href");
            }

            map.put("chapterName", chapterName);//章节名称
            map.put("chapter", chapter);//完整章节内容
            map.put("next", next);//下一章链接地址
        } catch (Exception e) {
            map = null;
            System.err.println("处理数据操作出错");
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 递归写入完整的一本书
     *
     * @param file       file
     * @param baseUrl    基础url
     * @param url        当前url
     * @param refererUrl refererUrl
     */
    public static void mergeBook(File file, String baseUrl, String url, String refererUrl) {
        String html = gather(baseUrl + url, baseUrl + refererUrl);
        Map<String, String> map = processor(html);
        //追加写入
        fileWriter(file, map.get("chapter"));
        System.out.println(map.get("chapterName") + " --100%");
        if (!StringUtils.isEmpty(map.get("next"))) {
            //递归
            mergeBook(file, baseUrl, map.get("next"), url);
        }
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
