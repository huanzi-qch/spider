package cn.huanzi.qch.spider.novelreading.controller;

import cn.huanzi.qch.spider.novelreading.pojo.Book;
import cn.huanzi.qch.spider.novelreading.pojo.BookHandler_biquge;
import cn.huanzi.qch.spider.novelreading.pojo.BookHandler_qidian;
import cn.huanzi.qch.spider.novelreading.pojo.BookHandler_zongheng;
import cn.huanzi.qch.spider.novelreading.util.BookUtil;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//开启跨域
@CrossOrigin(
        origins = "http://localhost:8080",
        allowedHeaders = "*",
        methods = {RequestMethod.POST},
        allowCredentials = "true",
        maxAge = 3600
)
@RestController
@RequestMapping("book/api/")
public class ApiContrller {
    /**
     * 来源集合
     */
    private static Map<String, Map<String, String>> source = new HashMap<>();

    static {
        //笔趣阁
        source.put("biquge", BookHandler_biquge.biquge);

        //纵横中文网
        source.put("zongheng", BookHandler_zongheng.zongheng);

        //起点中文网
        source.put("qidian", BookHandler_qidian.qidian);
    }

    /**
     * 搜索书名
     */
    @PostMapping("search")
    public ArrayList<Book> search(@RequestBody Book book) {
        //结果集
        ArrayList<Book> books = new ArrayList<>();
        //关键字
        String keyWord = book.getBookName();
        //来源
        String sourceKey = book.getSourceKey();

        //获取来源详情，复制一份
        Map<String, String> src = new HashMap<>();
        src.putAll(source.get(sourceKey));

        // 编码
        try {
            keyWord = URLEncoder.encode(keyWord, src.get("UrlEncode"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //searchUrl
        src.put("searchUrl", BookUtil.insertParams(src.get("searchUrl"), keyWord, "1"));

        //调用不同的方法
        switch (sourceKey) {
            case "biquge":
                BookHandler_biquge.book_search_biquge(books, src, keyWord);
                break;
            case "zongheng":
                BookHandler_zongheng.book_search_zongheng(books, src, keyWord);
                break;
            case "qidian":
                BookHandler_qidian.book_search_qidian(books, src, keyWord);
                break;
            default:
                //默认所有都查
                BookHandler_biquge.book_search_biquge(books, src, keyWord);
                BookHandler_zongheng.book_search_zongheng(books, src, keyWord);
                BookHandler_qidian.book_search_qidian(books, src, keyWord);
                break;
        }

        System.out.println(books.size());
        return books;
    }

    /**
     * 访问书本详情
     */
    @PostMapping("details")
    public Book details(@RequestBody Map<String, String> map) {
        String sourceKey = map.get("sourceKey");
        String bookUrl = map.get("bookUrl");
        String searchUrl = map.get("searchUrl");

        //获取来源详情，复制一份
        Map<String, String> src = new HashMap<>();
        src.putAll(source.get(sourceKey));

        src.put("searchUrl", searchUrl);
        Book book = new Book();
        //调用不同的方法
        switch (sourceKey) {
            case "biquge":
                book = BookHandler_biquge.book_details_biquge(src, bookUrl);
                break;
            case "zongheng":
                book = BookHandler_zongheng.book_details_zongheng(src, bookUrl);
                break;
            case "qidian":
                book = BookHandler_qidian.book_details_qidian(src, bookUrl);
                break;
            default:
                break;
        }
        return book;
    }

    /**
     * 访问书本章节
     */
    @PostMapping("read")
    public Book read(@RequestBody Map<String, String> map) {
        String sourceKey = map.get("sourceKey");
        String chapterUrl = map.get("chapterUrl");
        String refererUrl = map.get("refererUrl");

        //获取来源详情，复制一份
        Map<String, String> src = new HashMap<>();
        src.putAll(source.get(sourceKey));

        Book book = new Book();
        //调用不同的方法
        switch (sourceKey) {
            case "biquge":
                book = BookHandler_biquge.book_read_biquge(src, chapterUrl, refererUrl);
                break;
            case "zongheng":
                book = BookHandler_zongheng.book_read_zongheng(src, chapterUrl, refererUrl);
                break;
            case "qidian":
                book = BookHandler_qidian.book_read_qidian(src, chapterUrl, refererUrl);
                break;
            default:
                break;
        }
        return book;
    }
}
