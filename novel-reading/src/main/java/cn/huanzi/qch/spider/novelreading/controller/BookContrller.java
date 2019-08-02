package cn.huanzi.qch.spider.novelreading.controller;

import cn.huanzi.qch.spider.novelreading.pojo.Book;
import cn.huanzi.qch.spider.novelreading.pojo.BookHandler_biquge;
import cn.huanzi.qch.spider.novelreading.pojo.BookHandler_qidian;
import cn.huanzi.qch.spider.novelreading.pojo.BookHandler_zongheng;
import cn.huanzi.qch.spider.novelreading.util.BookUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Book Controller层
 */
@RestController
@RequestMapping("book/")
public class BookContrller {

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
     * 访问首页
     */
    @GetMapping("index")
    public ModelAndView index() {
        return new ModelAndView("book_index.html");
    }

    /**
     * 搜索书名
     */
    @GetMapping("search")
    public ModelAndView search(Book book) {
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
        ModelAndView modelAndView = new ModelAndView("book_list.html", "books", books);
        try {
            modelAndView.addObject("keyWord", URLDecoder.decode(keyWord, src.get("UrlEncode")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        modelAndView.addObject("sourceKey", sourceKey);
        return modelAndView;
    }

    /**
     * 访问书本详情
     */
    @GetMapping("details")
    public ModelAndView details(String sourceKey,String bookUrl,String searchUrl) {
        //获取来源详情，复制一份
        Map<String, String> src = new HashMap<>();
        src.putAll(source.get(sourceKey));

        src.put("searchUrl",searchUrl);
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
        return new ModelAndView("book_details.html", "book", book);
    }

    /**
     * 访问书本章节
     */
    @GetMapping("read")
    public ModelAndView read(String sourceKey,String chapterUrl,String refererUrl) {
        Map<String, String> src = source.get(sourceKey);
        Book book = new Book();
        //调用不同的方法
        switch (sourceKey) {
            case "biquge":
                book = BookHandler_biquge.book_read_biquge(src, chapterUrl,refererUrl);
                break;
            case "zongheng":
                book = BookHandler_zongheng.book_read_zongheng(src, chapterUrl,refererUrl);
                break;
            case "qidian":
                book = BookHandler_qidian.book_read_qidian(src, chapterUrl,refererUrl);
                break;
            default:
                break;
        }
        return new ModelAndView("book_read.html", "book", book);
    }
}
