package cn.huanzi.qch.commonspider.timer;

import cn.huanzi.qch.commonspider.pojo.IpProxy;
import cn.huanzi.qch.commonspider.repository.IpProxyPoolRepository;
import cn.huanzi.qch.commonspider.repository.UserAgentPoolRepository;
import cn.huanzi.qch.commonspider.util.IpProxyPoolUtil;
import cn.huanzi.qch.commonspider.util.WebClientUtil;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * IP代理池维护定时任务
 */
@Component
@Slf4j
public class IpProxyPoolScheduler {

    @Autowired
    private IpProxyPoolRepository ipProxyPoolRepository;

    @Autowired
    private UserAgentPoolRepository userAgentPoolRepository;

    /**
     * 定时更新IP代理池，一个小时触发一次
     * https://www.xicidaili.com/nt/ 西刺免费代理IP
     */
//    @Scheduled(cron = "0 0 * * * ?")
    public void updateIpProxyPoolSchedulerBy_xicidaili() {
        try {
            log.info("定时更新IP代理池任务开始 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            WebClient webClient = WebClientUtil.getWebClient();
            //关闭部分功能
            webClient.getOptions().setJavaScriptEnabled(false);//关闭js
            webClient.getOptions().setRedirectEnabled(false);//关闭重定向
            webClient.getOptions().setCssEnabled(false);//关闭css
            //调整时间
            webClient.getOptions().setTimeout(3000); //设置连接超时时间
            updateIpProxyPoolTaskBy_xicidaili(webClient.getPage("https://www.xicidaili.com/nt/"), webClient);
        } catch (IOException e) {
            log.error("更新IP代理池定时任务创建失败，原因：" + e.getMessage());
        }
        log.info("定时更新IP代理池任务结束 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    /**
     * 定时更新IP代理池，一个小时触发一次
     * http://www.89ip.cn/index_1.html 89ip
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateIpProxyPoolSchedulerBy_89ip() {
        try {
            log.info("定时更新IP代理池任务开始 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            WebClient webClient = WebClientUtil.getWebClient();
            //关闭部分功能
            webClient.getOptions().setJavaScriptEnabled(false);//关闭js
            webClient.getOptions().setRedirectEnabled(false);//关闭重定向
            webClient.getOptions().setCssEnabled(false);//关闭css
            //调整时间
            webClient.getOptions().setTimeout(3000); //设置连接超时时间
            updateIpProxyPoolTaskBy_89ip(webClient.getPage("http://www.89ip.cn/index_1.html"), webClient);
        } catch (IOException e) {
            log.error("更新IP代理池定时任务创建失败，原因：" + e.getMessage());
        }
        log.info("定时更新IP代理池任务结束 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    /**
     * 定时检查IP代理池，30分钟触发一次
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void checkIpProxyPool() {
        log.info("定时检查IP代理池任务开始 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        checkIpProxyPoolTask();
        log.info("定时检查IP代理池任务结束 ---" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        //更新IP代理、User Agent池
        IpProxyPoolUtil.updateIpProxyPoolAndUserAgentPool(ipProxyPoolRepository.findAll(), userAgentPoolRepository.findAll());
        IpProxyPoolUtil.updateIpProxyPoolAndUserAgentPool(ipProxyPoolRepository.findAll(), userAgentPoolRepository.findAll());
    }

    /**
     * 更新IP代理池异步任务
     */
    @Async("asyncTaskExecutor")
    void updateIpProxyPoolTaskBy_xicidaili(HtmlPage page, WebClient webClient) {
        //得到所有的 tr
        DomNodeList<DomElement> tr = page.getHtmlElementById("ip_list").getPage().getElementsByTagName("tr");
        tr.stream().forEach((domElement) -> {
            //得到所有的 td，按照目前规则，第二个是IP，第三个是端口
            DomNodeList<HtmlElement> td = domElement.getElementsByTagName("td");
            //筛选规则：存活时间大于1天
            if (td.size() > 0 /*&& td.get(8).asText().contains("天")*/) {
                IpProxy ipProxyPool = new IpProxy();
                ipProxyPool.setIp(td.get(1).asText());
                ipProxyPool.setPort(td.get(2).asText());
                ipProxyPool.setCity(td.get(3).asText());
                //主键是ip，如果已经存在则更新记录、不存在则存储新记录
                ipProxyPoolRepository.save(ipProxyPool);
                log.info(ipProxyPool.toString() + "IP代理成功，已更新到IP代理池");
            }
        });
        try {
            //下一页
            HtmlElement nextPage = page.getDocumentElement().getOneHtmlElementByAttribute("a", "rel", "next");
            //前10页
            if (Integer.valueOf(nextPage.asText()) <= 5) {
                HtmlPage page1 = nextPage.click();
                //递归调用
                updateIpProxyPoolTaskBy_xicidaili(page1, webClient);
            }
        } catch (IOException e) {
            log.error("更新IP代理池异步任务，获取下一页异常：" + e.getMessage());
        }
    }

    /**
     * 更新IP代理池异步任务
     */
    @Async("asyncTaskExecutor")
    void updateIpProxyPoolTaskBy_89ip(HtmlPage page, WebClient webClient) {
        //得到所有的 tr
        DomNodeList<HtmlElement> tr = page.getElementsByTagName("table").get(0).getElementsByTagName("tr");
        tr.stream().forEach((domElement) -> {
            //得到所有的 td，按照目前规则，第二个是IP，第三个是端口
            DomNodeList<HtmlElement> td = domElement.getElementsByTagName("td");
            //筛选规则：
            if (td.size() > 0) {
                IpProxy ipProxyPool = new IpProxy();
                ipProxyPool.setIp(td.get(0).asText());
                ipProxyPool.setPort(td.get(1).asText());
                ipProxyPool.setCity(td.get(2).asText());
                ipProxyPool.setOperator(td.get(3).asText());
                //主键是ip，如果已经存在则更新记录、不存在则存储新记录
                ipProxyPoolRepository.save(ipProxyPool);
                log.info(ipProxyPool.toString() + "IP代理成功，已更新到IP代理池");
            }
        });
        try {
            //下一页
            HtmlElement nextPage = page.getDocumentElement().getOneHtmlElementByAttribute("a", "class", "layui-laypage-next");
            String nextPagehref = nextPage.getAttribute("href");
            //前10页
            if (!nextPagehref.contains("10")) {
                HtmlPage page1 = nextPage.click();
                //递归调用
                updateIpProxyPoolTaskBy_89ip(page1, webClient);
            }
        } catch (IOException e) {
            log.error("更新IP代理池异步任务，获取下一页异常：" + e.getMessage());
        }
    }

    /**
     * 检查IP代理池异步任务
     */
    @Async("asyncTaskExecutor")
    void checkIpProxyPoolTask() {
        //查出当前所有IP代理
        List<IpProxy> list = ipProxyPoolRepository.findAll();
        WebClient webClient = WebClientUtil.getWebClient();
        //关闭部分功能
        webClient.getOptions().setJavaScriptEnabled(false);//关闭js
        webClient.getOptions().setRedirectEnabled(false);//关闭重定向
        webClient.getOptions().setCssEnabled(false);//关闭css
        //调整时间
        webClient.getOptions().setTimeout(3000); //设置连接超时时间
        list.stream().forEach((ipProxyPool) -> {
            try {
                //设置IP代理
                ProxyConfig proxyConfig = webClient.getOptions().getProxyConfig();
                proxyConfig.setProxyHost(ipProxyPool.getIp());//ip地址
                proxyConfig.setProxyPort(Integer.parseInt(ipProxyPool.getPort()));//端口
                //访问查询外网地址的网站，能请求成功，且返回的外网ip是一样说明代理成功
                /*
                    https://cmyip.com/
                    http://pv.sohu.com/cityjson
                    http://www.ip168.com/json.do?view=myipaddress
                 */
                Page checkIp = webClient.getPage("http://pv.sohu.com/cityjson");
                if (!(checkIp.getWebResponse().getStatusCode() == 200 && ((TextPage) checkIp).getContent().contains(ipProxyPool.getIp()))) {
                    ipProxyPoolRepository.delete(ipProxyPool);
                    log.info(ipProxyPool.toString() + "检查IP代理失败，已从IP代理池移除");
                }
            } catch (Exception e) {
                ipProxyPoolRepository.delete(ipProxyPool);
                log.error(ipProxyPool.toString() + "检查IP代理池异步任务异常，已从IP代理池移除" + e.getMessage());
            }
        });
    }
}
