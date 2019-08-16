package cn.huanzi.qch.flowspider.cnblogs.controller;

import cn.huanzi.qch.commonspider.repository.IpProxyPoolRepository;
import cn.huanzi.qch.commonspider.repository.UserAgentPoolRepository;
import cn.huanzi.qch.commonspider.timer.IpProxyPoolScheduler;
import cn.huanzi.qch.commonspider.util.HttpClientUtil;
import cn.huanzi.qch.commonspider.util.WebClientUtil;
import cn.huanzi.qch.commonspider.vo.ResultVo;
import cn.huanzi.qch.flowspider.cnblogs.pojo.Blog;
import cn.huanzi.qch.flowspider.cnblogs.repository.BlogRepository;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/blog/")
public class BlogController {

    @Autowired
    private IpProxyPoolRepository ipProxyPoolRepository;

    @Autowired
    private UserAgentPoolRepository userAgentPoolRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private IpProxyPoolScheduler ipProxyPoolScheduler;

    /**
     * 用于计算次数
     */
    private static int pageIndex = 1, okCount = 1, failureCount = 1;

    /**
     * 开始刷、停止刷的标识
     */
    private static Boolean flag = false;

    private static List<Blog> blogList;

    /**
     * 跳转首页（实时日志页面）
     */
    @GetMapping("index")
    public ModelAndView logging() {
        return new ModelAndView("logging.html");
    }

    /**
     * 更新博客集合
     * http://localhost:10087/blog/updateBlogList?blogIndexUrl=https://www.cnblogs.com/huanzi-qch/
     */
    @GetMapping("updateBlogList")
    public void updateBlogList(String blogIndexUrl) {
        updateBlogListScheduled();
    }

    /**
     * 定时任务，一天触发一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateBlogListScheduled(){
        //重新获取
        try {
            log.info("更新博客集合任务开始 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            HttpClient httpClient = HttpClientUtil.getHttpClient();
            ResultVo<HttpResponse> resultVo = HttpClientUtil.gatherForGet(httpClient, "https://www.cnblogs.com/huanzi-qch/", "https://www.cnblogs.com", null);
            updateBlogListTask(EntityUtils.toString(resultVo.getPage().getEntity(), "UTF-8"), httpClient);
            log.info("更新博客集合任务结束 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            //获取所有博客
            blogList = blogRepository.findAll();
            log.info("博客集合初始化成功，大小：" + blogList.size());
        } catch (IOException e) {
            log.error("更新博客集合任务异常，原因：" + e.getMessage());
        }
    }

    /**
     * 手动更新IP代理池 By_xicidaili
     * http://localhost:10087/blog/updateIpProxyPool
     */
    @GetMapping("updateIpProxyPoolBy_xicidaili")
    public void updateIpProxyPoolBy_xicidaili() {
        ipProxyPoolScheduler.updateIpProxyPoolSchedulerBy_xicidaili();
    }

    /**
     * 手动更新IP代理池 By_89ip
     * http://localhost:10087/blog/updateIpProxyPool
     */
    @GetMapping("updateIpProxyPoolBy_89ip")
    public void updateIpProxyPoolBy_89ip() {
        ipProxyPoolScheduler.updateIpProxyPoolSchedulerBy_89ip();
    }

    /**
     * 手动检查IP代理池
     * http://localhost:10087/blog/checkIpProxyPool
     */
    @GetMapping("checkIpProxyPool")
    public void checkIpProxyPool() {
        ipProxyPoolScheduler.checkIpProxyPool();
    }

    /**
     * 开始
     */
    @GetMapping("start")
    public void start() {
        flag = false;
        //开启任务
        //获取WebClient对象
        WebClient webClient = WebClientUtil.getWebClient();
        //更新IP代理、User Agent池
        WebClientUtil.updateIpProxyPoolAndUserAgentPool(ipProxyPoolRepository.findAll(), userAgentPoolRepository.findAll());

        //获取所有博客
        blogList = blogRepository.findAll();
        log.info("博客集合初始化成功，大小：" + blogList.size());
        go(webClient);
    }

    /**
     * 停止
     */
    @GetMapping("stop")
    public void stop() {
        flag = true;
    }

    /**
     * 更新博客集合异步任务
     */
    @Async("asyncTaskExecutor")
    void updateBlogListTask(String html, HttpClient httpClient) throws IOException {
        //解析html格式的字符串成一个Document
        Document doc = Jsoup.parse(html);

        //遍历找到当前页所有博客
        Elements elements = doc.select("a.postTitle2");
        for (Element result : elements) {
            Blog blog = new Blog();
            blog.setBlogUrl(result.attr("href"));
            blog.setBlogName(result.text());
            blogRepository.save(blog);
        }

        //下一页
        Elements nextPage = doc.select("div.topicListFooter");
        if (nextPage.size() > 0 && nextPage.get(0).text().contains("下一页")) {
            pageIndex++;
            String nextPageUrl = "https://www.cnblogs.com/huanzi-qch/default.html?page=" + pageIndex;
            ResultVo<HttpResponse> resultVo = HttpClientUtil.gatherForGet(httpClient, nextPageUrl, "https://www.cnblogs.com", null);
            updateBlogListTask(EntityUtils.toString(resultVo.getPage().getEntity(), "UTF-8"), httpClient);
        } else {
            pageIndex = 1;
        }
    }

    /**
     * 开始刷
     */
    @Async("asyncTaskExecutor")
    void go(WebClient webClient) {
        try {
            while (true) {
                //是否结束线程
                if (flag) {
                    throw new RuntimeException("采用手动‘抛异常’的方式终止线程");
                }

                //随机获取博客
                Integer blogIndex = WebClientUtil.randomNumber(0, blogList.size() - 1);
                Blog blog = blogList.get(blogIndex);

                try {
                    //随机3到6秒数访问
                    Integer random = WebClientUtil.randomNumber(3, 6);
                    log.info(random + "秒后开始下一次访问");
                    Thread.sleep(random * 1000);

                    ResultVo<HtmlPage> resultVo = WebClientUtil.gatherForGet(webClient, blog.getBlogUrl(), "https://www.cnblogs.com/", null);
                    if (200 == resultVo.getStatusCode()) {
                        log.info(blog.getBlogName() + "，下标：" + blogIndex + " ，访问成功，当前阅读量：" + resultVo.getPage().getHtmlElementById("post_view_count").asText() + "，成功" + okCount + "次" + "，失败" + failureCount + "次");
                        okCount++;
                    } else {
                        log.error(blog.getBlogName() + "，下标：" + blogIndex + " ，访问失败，原因：" + resultVo.getStatusMessage() + "，成功" + okCount + "次" + "，失败" + failureCount + "次");
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error(blog.getBlogName() + "，下标：" + blogIndex + " ，访问失败，原因：" + e.getMessage());
                }
            }
        } catch (RuntimeException ee) {
            log.info(ee.getMessage());
        }
        log.info("线程终止!");
    }
}
