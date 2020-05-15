package cn.huanzi.qch.flowspider.wechatvote.controller;

import cn.huanzi.qch.commonspider.repository.IpProxyPoolRepository;
import cn.huanzi.qch.commonspider.util.HttpClientUtil;
import cn.huanzi.qch.commonspider.util.WebClientUtil;
import cn.huanzi.qch.commonspider.vo.ResultVo;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/weChatVote/")
public class WeChatVoteController {

    @Autowired
    private IpProxyPoolRepository ipProxyPoolRepository;

    /**
     * 用于计算次数
     */
    private static int okCount = 1, failureCount = 1;

    /**
     * 开始刷、停止刷的标识
     */
    private static Boolean flag = false;

    /**
     * 开始
     */
    @GetMapping("start")
    public void start() {
        flag = false;
        //开启任务

        //更新IP代理，User Agent池赋一个空对象即可
        WebClientUtil.updateIpProxyPoolAndUserAgentPool(ipProxyPoolRepository.findAll(), new ArrayList<>());
        go(WebClientUtil.getWebClient());
    }

    /**
     * 停止
     */
    @GetMapping("stop")
    public void stop() {
        flag = true;
    }

    /**
     * 开始刷
     */
    @Async("asyncTaskExecutor")
    void go(WebClient webClient) {
        //微信UserAgent标识
        String[] webKitUserAgent = {
                "Mozilla/5.0 (Linux; Android 7.1.1; MI 6 Build/NMF26X; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/6.2 TBS/043807 Mobile Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 7.1.1; OD103 Build/NMF26F; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/4G Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 6.0.1; SM919 Build/MXB48T; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 5.1.1; vivo X6S A Build/LMY47V; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 5.1; HUAWEI TAG-AL00 Build/HUAWEITAG-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043622 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/4G Language/zh_CN",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13F69 MicroMessenger/6.6.1 NetType/4G Language/zh_CN",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 11_2_2 like Mac OS X) AppleWebKit/604.4.7 (KHTML, like Gecko) Mobile/15C202 MicroMessenger/6.6.1 NetType/4G Language/zh_CN",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 11_1_1 like Mac OS X) AppleWebKit/604.3.5 (KHTML, like Gecko) Mobile/15B150 MicroMessenger/6.6.1 NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (iphone x Build/MXB48T; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
        };
        try {
            while (true) {
                //是否结束线程
                if (flag) {
                    throw new RuntimeException("采用手动‘抛异常’的方式终止线程");
                }

                //随机设置微信UserAgent标识
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", webKitUserAgent[HttpClientUtil.randomNumber(0, webKitUserAgent.length - 1)]);

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("json","{\"super_vote_item\":[{\"vote_id\":452951133,\"item_idx_list\":{\"item_idx\":[\"20\"]}}],\"super_vote_id\":452950995}");
                paramMap.put("action","vote");
//                paramMap.put("__biz","MzI1MzAyMjIzMA==");
//                paramMap.put("uin","MTE5MjIyMjE3OA==");
                paramMap.put("key","d2b64b7a8020292ce617a561e59ca839e6206268eb70753f145af6fb199102e70f5f8cc14f6d66e37005664507517f078fe72c42cd2a8481fb22305fb3d8caf65b85a14730b84d614c80fe0fe080124b");
//                paramMap.put("pass_ticket","2oivn2WIng7YFtpCNVV0jnjKCwI7EWcUveLaZx9TpEhEYtnbO%2FqXmKtKcUWWcHir");
                paramMap.put("appmsg_token","1035_LwVeT3eQpujaat2EJlQKEUN8k__S5HlfEjdI02v1JNagXo29ZWVdcKAhF6wNXt46-PG_4_GzVRDiJ8xf");
//                paramMap.put("f","json");
//                paramMap.put("idx","1");
//                paramMap.put("mid","2649139206");
                paramMap.put("wxtoken","777");
                try {
                    //随机3到6秒数访问
                    Integer random = HttpClientUtil.randomNumber(3, 6);
                    log.info(random + "秒后开始下一次访问");
                    Thread.sleep(random * 1000);

                    ResultVo<HtmlPage> resultVo = WebClientUtil.gatherForPost(webClient, "https://mp.weixin.qq.com/mp/newappmsgvote", "https://mp.weixin.qq.com/mp/newappmsgvote?action=show&__biz=MzI1MzAyMjIzMA==&supervoteid=452950995&uin=MTE5MjIyMjE3OA%3D%3D&key=d2b64b7a8020292ce617a561e59ca839e6206268eb70753f145af6fb199102e70f5f8cc14f6d66e37005664507517f078fe72c42cd2a8481fb22305fb3d8caf65b85a14730b84d614c80fe0fe080124b&pass_ticket=2oivn2WIng7YFtpCNVV0jnjKCwI7EWcUveLaZx9TpEhEYtnbO%252FqXmKtKcUWWcHir&wxtoken=777&mid=2649139206&idx=1&appmsg_token=1035_LwVeT3eQpujaat2EJlQKEUN8k__S5HlfEjdI02v1JNagXo29ZWVdcKAhF6wNXt46-PG_4_GzVRDiJ8xf", headers, paramMap);
                    //获取页面源代码
                    String result = resultVo.getPage().asText();
                    log.info(result);
                    if (result.contains("errmsg:\"ok\"")) {
                        log.info("投票成功，第" + okCount + "次");
                        okCount++;
                    } else {
                        log.info("投票失败，第" + failureCount + "次");
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error("投票异常：" + e.getMessage());
                }
            }
        } catch (RuntimeException ee) {
            log.info(ee.getMessage());
        }
        log.info("线程终止!");
    }
}
