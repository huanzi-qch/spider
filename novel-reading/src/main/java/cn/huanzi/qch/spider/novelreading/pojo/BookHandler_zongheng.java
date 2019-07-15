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
 * 纵横中文网采集规则
 */
public class BookHandler_zongheng {

    /**
     * 来源信息
     */
    public static HashMap<String, String> zongheng = new HashMap<>();

    static {
        //纵横中文网
        zongheng.put("name", "纵横中文网");
        zongheng.put("key", "zongheng");
        zongheng.put("baseUrl", "http://www.zongheng.com");
        zongheng.put("baseSearchUrl", "http://search.zongheng.com/s");
        zongheng.put("UrlEncode", "UTF-8");
        zongheng.put("searchUrl", "http://search.zongheng.com/s?keyword=#1&pageNo=#2&sort=");
    }

    /**
     * 获取search list   纵横中文网采集规则
     *
     * @param books   结果集合
     * @param src     源目标
     * @param keyWord 关键字
     */
    public static void book_search_zongheng(ArrayList<Book> books, Map<String, String> src, String keyWord) {
        //采集术
        String html = BookUtil.gather(src.get("searchUrl"), src.get("baseUrl"));
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //当前页集合
            Elements resultList = doc.select("div.search-tab > div.search-result-list");
            for (Element result : resultList) {
                Book book = new Book();
                //书本链接
                book.setBookUrl(result.select("div.imgbox a").attr("href"));
                //图片
                book.setImg(result.select("div.imgbox img").attr("src"));
                //书名
                book.setBookName(result.select("h2.tit").text());
                //作者
                book.setAuthor(result.select("div.bookinfo > a").first().text());
                //类型
                book.setType(result.select("div.bookinfo > a").last().text());
                //简介
                book.setSynopsis(result.select("p").text());
                //状态
                book.setStatus(result.select("div.bookinfo > span").first().text());
                //大小
                book.setMagnitude(result.select("div.bookinfo > span").last().text());
                //来源
                book.setSource(src);
                books.add(book);
            }

            //下一页
            Elements searchNext = doc.select("div.search_d_pagesize > a.search_d_next");
            String href = searchNext.attr("href");
            //最多只要888本，不然太慢了
            if (books.size() < 888 && !StringUtils.isEmpty(href)) {
                src.put("baseUrl", src.get("searchUrl"));
                src.put("searchUrl", href.contains("http") ? href : (src.get("baseSearchUrl") + href));
                book_search_zongheng(books, src, keyWord);
            }

        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
    }

    /**
     *  获取书本详情  纵横中文网采集规则
     * @param src 源目标
     * @param bookUrl 书本链接
     * @return Book对象
     */
    public static Book book_details_zongheng(Map<String, String> src, String bookUrl) {
        Book book = new Book();
        //采集术
        String html = BookUtil.gather(bookUrl, src.get("searchUrl"));
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //书本链接
            book.setBookUrl(bookUrl);
            //图片
            book.setImg(doc.select("div.book-img > img").attr("src"));
            //书名
            book.setBookName(doc.select("div.book-info > div.book-name").text());
            //作者
            book.setAuthor(doc.select("div.book-author div.au-name").text());
            //更新时间
            book.setUpdateDate(doc.select("div.book-new-chapter div.time").text());
            //最新章节
            book.setLatestChapter(doc.select("div.book-new-chapter div.tit a").text());
            book.setLatestChapterUrl(doc.select("div.book-new-chapter div.tit a").attr("href"));
            //类型
            book.setType(doc.select("div.book-label > a").last().text());
            //简介
            book.setSynopsis(doc.select("div.book-dec > p").text());
            //状态
            book.setStatus(doc.select("div.book-label > a").first().text());

            //章节目录
            String chaptersUrl = doc.select("a.all-catalog").attr("href");
            ArrayList<Map<String, String>> chapters = new ArrayList<>();
            //采集术
            for (Element result : Jsoup.parse(BookUtil.gather(chaptersUrl, bookUrl)).select("ul.chapter-list li")) {
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
     * 得到当前章节名以及完整内容跟上、下一章的链接地址 纵横中文网采集规则
     * @param src 源目标
     * @param chapterUrl 当前章节链接
     * @param refererUrl 来源链接
     * @return Book对象
     */
    public static Book book_read_zongheng(Map<String, String> src,String chapterUrl,String refererUrl) {
        Book book = new Book();

        //当前章节链接
        book.setNowChapterUrl(chapterUrl.contains("http") ? chapterUrl : (src.get("baseUrl") + chapterUrl));

        //采集术
        String html = BookUtil.gather(book.getNowChapterUrl(), refererUrl);
        try {
            //解析html格式的字符串成一个Document
            Document doc = Jsoup.parse(html);

            //当前章节名称
            book.setNowChapter(doc.select("div.title_txtbox").text());

            //删除图片广告
            doc.select("div.content img").remove();
            //当前章节内容
            book.setNowChapterValue(doc.select("div.content").outerHtml());

            //上、下一章
            book.setPrevChapter(doc.select("div.chap_btnbox a:matches((?i)下一章)").text());
            book.setPrevChapterUrl(doc.select("div.chap_btnbox a:matches((?i)下一章)").attr("href"));
            book.setNextChapter(doc.select("div.chap_btnbox a:matches((?i)上一章)").text());
            book.setNextChapterUrl(doc.select("div.chap_btnbox a:matches((?i)上一章)").attr("href"));

            //来源
            book.setSource(src);

        } catch (Exception e) {
            System.err.println("采集数据操作出错");
            e.printStackTrace();
        }
        return book;
    }
}
