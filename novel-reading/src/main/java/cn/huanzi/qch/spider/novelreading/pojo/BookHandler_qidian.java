package cn.huanzi.qch.spider.novelreading.pojo;

import cn.huanzi.qch.spider.novelreading.util.BookUtil;
import net.sf.json.JSONObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  起点中文网采集规则
 */
public class BookHandler_qidian {

    /**
     * 来源信息
     */
    public static HashMap<String, String> qidian = new HashMap<>();

    static {
        //起点中文网
        qidian.put("name", "起点中文网");
        qidian.put("key", "qidian");
        qidian.put("baseUrl", "http://www.qidian.com");
        qidian.put("baseSearchUrl", "https://www.qidian.com/search");
        qidian.put("UrlEncode", "UTF-8");
        qidian.put("searchUrl", "https://www.qidian.com/search?kw=#1&page=#2");
    }

    /**
     * 获取search list   起点中文网采集规则
     *
     * @param books   结果集合
     * @param src     源目标
     * @param keyWord 关键字
     */
    public static void book_search_qidian(ArrayList<Book> books, Map<String, String> src, String keyWord) {
        //采集术
        String html = BookUtil.gather(src.get("searchUrl"), src.get("baseUrl"));
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //当前页集合
            Elements resultList = doc.select("li.res-book-item");
            for (Element result : resultList) {
                Book book = new Book();
                /*
                       如果大家打断点在这里的话就会发现，起点的链接是这样的
                       //book.qidian.com/info/1012786368

                       以两个斜杠开头，不过无所谓，httpClient照样可以请求
                 */
                //书本链接
                book.setBookUrl(result.select("div.book-img-box a").attr("href"));
                //图片
                book.setImg(result.select("div.book-img-box img").attr("src"));
                //书名
                book.setBookName(result.select("div.book-mid-info > h4").text());
                //作者
                book.setAuthor(result.select("div.book-mid-info > p.author > a").first().text());
                //类型
                book.setType(result.select("div.book-mid-info > p.author > a").last().text());
                //简介
                book.setSynopsis(result.select("div.book-mid-info > p.intro").text());
                //状态
                book.setStatus(result.select("div.book-mid-info > p.author > span").first().text());
                //更新时间
                book.setUpdateDate(result.select("div.book-mid-info > p.update > span").text());
                //最新章节
                book.setLatestChapter(result.select("div.book-mid-info > p.update > a").text());
                book.setLatestChapterUrl(result.select("div.book-mid-info > p.update > a").attr("href"));
                //来源
                book.setSource(src);
                books.add(book);
            }

            //当前页
            String page = doc.select("div#page-container").attr("data-page");

            //最大页数
            String pageMax = doc.select("div#page-container").attr("data-pageMax");

            //当前页 < 最大页数
            if (Integer.valueOf(page) < Integer.valueOf(pageMax)) {
                src.put("baseUrl", src.get("searchUrl"));
                //自己拼接下一页链接
                src.put("searchUrl", src.get("searchUrl").replaceAll("page=" + Integer.valueOf(page), "page=" + (Integer.valueOf(page) + 1)));
                book_search_qidian(books, src, keyWord);
            }

        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
    }

    /**
     *  获取书本详情  起点中文网采集规则
     * @param src 源目标
     * @param bookUrl 书本链接
     * @return Book对象
     */
    public static Book book_details_qidian(Map<String, String> src, String bookUrl) {
        Book book = new Book();

        //https
        bookUrl = "https:" + bookUrl;

        //采集术
        String html = BookUtil.gather(bookUrl, src.get("searchUrl"));
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //书本链接
            book.setBookUrl(bookUrl);
            //图片
            String img = doc.select("div.book-img > a#bookImg > img").attr("src");
            img = "https:" + img;
            book.setImg(img);
            //书名
            book.setBookName(doc.select("div.book-info > h1 > em").text());
            //作者
            book.setAuthor(doc.select("div.book-info > h1 a.writer").text());
            //更新时间
            book.setUpdateDate(doc.select("li.update em.time").text());
            //最新章节
            book.setLatestChapter(doc.select("li.update a").text());
            book.setLatestChapterUrl(doc.select("li.update a").attr("href"));
            //类型
            book.setType(doc.select("p.tag > span").first().text());
            //简介
            book.setSynopsis(doc.select("div.book-intro > p").text());
            //状态
            book.setStatus(doc.select("p.tag > a").first().text());

            //章节目录

            //创建httpclient对象 (这里设置成全局变量，相对于同一个请求session、cookie会跟着携带过去)
            BasicCookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            //创建get方式请求对象
            HttpGet httpGet = new HttpGet("https://book.qidian.com/");
            httpGet.addHeader("Content-type", "application/json");
            //包装一下
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
            httpGet.addHeader("Connection", "keep-alive");
            //通过请求对象获取响应对象
            CloseableHttpResponse response = httpClient.execute(httpGet);
            //获得Cookies
            String _csrfToken = "";
            List<Cookie> cookies = cookieStore.getCookies();
            for (int i = 0; i < cookies.size(); i++) {
                if("_csrfToken".equals(cookies.get(i).getName())){
                    _csrfToken = cookies.get(i).getValue();
                }
            }

            //构造post
            String bookId = doc.select("div.book-img a#bookImg").attr("data-bid");
            HttpPost httpPost = new HttpPost(BookUtil.insertParams("https://book.qidian.com/ajax/book/category?_csrfToken=#1&bookId=#2",_csrfToken,bookId));
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
            httpPost.addHeader("Connection", "keep-alive");
            //通过请求对象获取响应对象
            CloseableHttpResponse response1 = httpClient.execute(httpPost);
            //获取结果实体(json格式字符串)
            String chaptersJson = "";
            if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                chaptersJson = EntityUtils.toString(response1.getEntity(), "UTF-8");
            }

            //java处理json
            ArrayList<Map<String, String>> chapters = new ArrayList<>();

            JSONObject jsonArray = JSONObject.fromObject(chaptersJson);
            Map<String,Object> objectMap = (Map<String, Object>) jsonArray;

            Map<String, Object> objectMap_data = (Map<String, Object>) objectMap.get("data");
            List<Map<String, Object>> objectMap_data_vs = (List<Map<String, Object>>) objectMap_data.get("vs");
            for(Map<String, Object> vs : objectMap_data_vs){
                List<Map<String, Object>> cs = (List<Map<String, Object>>) vs.get("cs");
                for(Map<String, Object> chapter : cs){
                    Map<String, String> map = new HashMap<>();
                    map.put("chapterName", (String) chapter.get("cN"));
                    map.put("url", "https://read.qidian.com/chapter/"+(String) chapter.get("cU"));
                    chapters.add(map);
                }
            }

            book.setChapters(chapters);


            //来源
            book.setSource(src);

            //释放链接
            response.close();
        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
        return book;
    }

    /**
     * 得到当前章节名以及完整内容跟上、下一章的链接地址 起点中文网采集规则
     * @param src 源目标
     * @param chapterUrl 当前章节链接
     * @param refererUrl 来源链接
     * @return Book对象
     */
    public static Book book_read_qidian(Map<String, String> src,String chapterUrl,String refererUrl) {
        Book book = new Book();

        //当前章节链接
        book.setNowChapterUrl(chapterUrl.contains("http") ? chapterUrl : (src.get("baseUrl") + chapterUrl));

        //采集术
        String html = BookUtil.gather(book.getNowChapterUrl(), refererUrl);
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            System.out.println(html);

            //当前章节名称
            book.setNowChapter(doc.select("h3.j_chapterName").text());

            //删除图片广告
            doc.select("div.read-content img").remove();
            //当前章节内容
            book.setNowChapterValue(doc.select("div.read-content").outerHtml());

            //上、下一章
            book.setPrevChapter(doc.select("div.chapter-control a:matches((?i)下一章)").text());
            String prev = doc.select("div.chapter-control a:matches((?i)下一章)").attr("href");
            prev = "https:"+prev;
            book.setPrevChapterUrl(prev);
            book.setNextChapter(doc.select("div.chapter-control a:matches((?i)上一章)").text());
            String next = doc.select("div.chapter-control a:matches((?i)上一章)").attr("href");
            next = "https:"+next;
            book.setNextChapterUrl(next);

            //来源
            book.setSource(src);

        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
        return book;
    }
}
