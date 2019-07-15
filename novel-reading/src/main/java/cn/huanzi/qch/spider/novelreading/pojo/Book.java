package cn.huanzi.qch.spider.novelreading.pojo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 书对象
 */
@Data
public class Book {

    /**
     * 链接
     */
    private String bookUrl;

    /**
     * 书名
     */
    private String bookName;

    /**
     * 作者
     */
    private String author;

    /**
     * 简介
     */
    private String synopsis;

    /**
     * 图片
     */
    private String img;

    /**
     * 章节目录 chapterName、url
     */
    private List<Map<String,String>> chapters;

    /**
     * 状态
     */
    private String status;

    /**
     * 类型
     */
    private String type;

    /**
     * 更新时间
     */
    private String updateDate;

    /**
     * 第一章
     */
    private String firstChapter;

    /**
     * 第一章链接
     */
    private String firstChapterUrl;

    /**
     * 上一章节
     */
    private String prevChapter;

    /**
     * 上一章节链接
     */
    private String prevChapterUrl;

    /**
     * 当前章节名称
     */
    private String nowChapter;

    /**
     * 当前章节内容
     */
    private String nowChapterValue;

    /**
     * 当前章节链接
     */
    private String nowChapterUrl;

    /**
     * 下一章节
     */
    private String nextChapter;

    /**
     * 下一章节链接
     */
    private String nextChapterUrl;

    /**
     * 最新章节
     */
    private String latestChapter;

    /**
     * 最新章节链接
     */
    private String latestChapterUrl;

    /**
     * 大小
     */
    private String magnitude;

    /**
     * 来源
     */
    private Map<String,String> source;
    private String sourceKey;
}
