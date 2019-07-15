package cn.huanzi.qch.spider.novelreading.pojo;

import cn.huanzi.qch.spider.novelreading.util.BookUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 笔趣阁采集规则
 */
public class BookHandler_biquge {

    /**
     * 来源信息
     */
    public static HashMap<String, String> biquge = new HashMap<>();

    static {
        //笔趣阁
        biquge.put("name", "笔趣阁");
        biquge.put("key", "biquge");
        biquge.put("baseUrl", "http://www.biquge.com.tw");
        biquge.put("baseSearchUrl", "http://www.biquge.com.tw/modules/article/soshu.php");
        biquge.put("UrlEncode", "GB2312");
        biquge.put("searchUrl", "http://www.biquge.com.tw/modules/article/soshu.php?searchkey=+#1&page=#2");
    }

    /**
     * 获取search list   笔趣阁采集规则
     *
     * @param books   结果集合
     * @param src     源目标
     * @param keyWord 关键字
     */
    public static void book_search_biquge(ArrayList<Book> books, Map<String, String> src, String keyWord) {
        //采集术
        String html = BookUtil.gather(src.get("searchUrl"), src.get("baseUrl"));
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //当前页集合
            Elements resultList = doc.select("table.grid  tr#nr");
            for (Element result : resultList) {
                Book book = new Book();
                //书本链接
                book.setBookUrl(result.child(0).select("a").attr("href"));
                //书名
                book.setBookName(result.child(0).select("a").text());
                //作者
                book.setAuthor(result.child(2).text());
                //更新时间
                book.setUpdateDate(result.child(4).text());
                //最新章节
                book.setLatestChapter(result.child(1).select("a").text());
                book.setLatestChapterUrl(result.child(1).select("a").attr("href"));
                //状态
                book.setStatus(result.child(5).text());
                //大小
                book.setMagnitude(result.child(3).text());
                //来源
                book.setSource(src);
                books.add(book);
            }

            //下一页
            Elements searchNext = doc.select("div.pages > a.ngroup");
            String href = searchNext.attr("href");
            if (!StringUtils.isEmpty(href)) {
                src.put("baseUrl", src.get("searchUrl"));
                src.put("searchUrl", href.contains("http") ? href : (src.get("baseSearchUrl") + href));
                book_search_biquge(books, src, keyWord);
            }

        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
    }

    /**
     *  获取书本详情  笔趣阁采集规则
     * @param src 源目标
     * @param bookUrl 书本链接
     * @return Book对象
     */
    public static Book book_details_biquge(Map<String, String> src, String bookUrl) {
        Book book = new Book();
        //采集术
        String html = BookUtil.gather(bookUrl, src.get("searchUrl"));
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);
            //书本链接
            book.setBookUrl(doc.select("meta[property=og:url]").attr("content"));
            //图片
            book.setImg(doc.select("meta[property=og:image]").attr("content"));
            //书名
            book.setBookName(doc.select("div#info > h1").text());
            //作者
            book.setAuthor(doc.select("meta[property=og:novel:author]").attr("content"));
            //更新时间
            book.setUpdateDate(doc.select("meta[property=og:novel:update_time]").attr("content"));
            //最新章节
            book.setLatestChapter(doc.select("meta[property=og:novel:latest_chapter_name]").attr("content"));
            book.setLatestChapterUrl(doc.select("meta[property=og:novel:latest_chapter_url]").attr("content"));
            //类型
            book.setType(doc.select("meta[property=og:novel:category]").attr("content"));
            //简介
            book.setSynopsis(doc.select("meta[property=og:description]").attr("content"));
            //状态
            book.setStatus(doc.select("meta[property=og:novel:status]").attr("content"));

            //章节目录
            ArrayList<Map<String, String>> chapters = new ArrayList<>();
            for (Element result : doc.select("div#list dd")) {
                HashMap<String, String> map = new HashMap<>();
                map.put("chapterName", result.select("a").text());
                map.put("url", result.select("a").attr("href"));
                chapters.add(map);
            }
            book.setChapters(chapters);

            //来源
            book.setSource(src);

        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
        return book;
    }

    /**
     * 得到当前章节名以及完整内容跟上、下一章的链接地址 笔趣阁采集规则
     * @param src 源目标
     * @param chapterUrl 当前章节链接
     * @param refererUrl 来源链接
     * @return Book对象
     */
    public static Book book_read_biquge(Map<String, String> src,String chapterUrl,String refererUrl) {
        Book book = new Book();

        //当前章节链接
        book.setNowChapterUrl(chapterUrl.contains("http") ? chapterUrl : (src.get("baseUrl") + chapterUrl));

        //采集术
        String html = BookUtil.gather(book.getNowChapterUrl(), refererUrl);
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //当前章节名称
            book.setNowChapter(doc.select("div.box_con > div.bookname > h1").text());

            //删除图片广告
            doc.select("div.box_con > div#content img").remove();
            //当前章节内容
            book.setNowChapterValue(doc.select("div.box_con > div#content").outerHtml());

            //上、下一章
            book.setPrevChapter(doc.select("div.bottem2 a:matches((?i)下一章)").text());
            book.setPrevChapterUrl(doc.select("div.bottem2 a:matches((?i)下一章)").attr("href"));
            book.setNextChapter(doc.select("div.bottem2 a:matches((?i)上一章)").text());
            book.setNextChapterUrl(doc.select("div.bottem2 a:matches((?i)上一章)").attr("href"));

            //来源
            book.setSource(src);

        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
        return book;
    }
}
