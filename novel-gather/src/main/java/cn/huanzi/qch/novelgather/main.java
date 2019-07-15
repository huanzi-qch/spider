package cn.huanzi.qch.novelgather;

import java.io.File;

import static cn.huanzi.qch.novelgather.util.GatherUtil.createFile;
import static cn.huanzi.qch.novelgather.util.GatherUtil.mergeBook;

/**
 * main函数
 */
public class main {
    public static void main(String[] args) {
        //需要提供的条件：站点；小说名；第一章的链接；refererUrl
        String baseUrl = "http://www.biquge.com.tw";
        File file = createFile("斗破苍穹");
        mergeBook(file, baseUrl, "/1_1999/1179371.html","/1_1999/");
    }
}
