package cn.huanzi.qch.sportslifesense.timer;


import cn.huanzi.qch.commonspider.util.WebClientUtil;
import com.gargoylesoftware.htmlunit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 定时更新运动步数任务
 */
@Component
@Slf4j
public class SportsScheduler {

    //抓取到的userId
    private final String userId = "";

    //抓取到的id
    private final String id = "";

    //日期格式
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");

    private WebClient webClient;
    private  WebRequest request;

    public SportsScheduler(){
        log.info("SportsScheduler定时任务类初始化成功！");

        webClient = getWebClient();

        //设置请求头Header
        webClient.removeRequestHeader("Content-Type");
        webClient.addRequestHeader("Content-Type","application/json; charset=utf-8");
        webClient.removeRequestHeader("User-Agent");
        webClient.addRequestHeader("User-Agent","Dalvik/2.1.0 (Linux; U; Android 10; MI 9 MIUI/V11.0.5.0.QFACNXM)");
        webClient.removeRequestHeader("Host");
        webClient.addRequestHeader("Host","sports.lifesense.com");
        webClient.removeRequestHeader("Connection");
        webClient.addRequestHeader("Connection","Keep-Alive");
        webClient.removeRequestHeader("Accept-Encoding");
        webClient.addRequestHeader("Accept-Encoding","gzip");

        //设置cookie
        WebClientUtil.setCookie(webClient,"sports.lifesense.com","session=%7B%22accessToken%22%3A%22d4f2b49f4ee64f66b067f34fff8ba033%22%2C%22appType%22%3A6%2C%22expireAt%22%3A1601781691345%2C%22loginId%22%3A%2227125870%22%2C%22userType%22%3A99%2C%22gray%22%3Afalse%7D; appType2=6; accessToken2=d4f2b49f4ee64f66b067f34fff8ba033; expireAt2=1601781691345; loginId2=27125870; userType2=99; gray2=false; session=%7B%22accessToken%22%3A%22d4f2b49f4ee64f66b067f34fff8ba033%22%2C%22appType%22%3A6%2C%22expireAt%22%3A1601781691345%2C%22loginId%22%3A%2227125870%22%2C%22userType%22%3A99%2C%22gray%22%3Afalse%7D; userType2=99; expireAt2=1601781691345; accessToken=D2A6AFB93531605DBE56DC2EEE74C4C909C43A335079CB661214D00DB8B225199DAE586666F19751CDAC8A142256DD3359C3A7AAAE2EAC2A827CFC4A4568C60CF31E3097ACEF1EB4AE49BF522869EFCC64CC89D6FD301A447148A3C79CE0ECC949554FBB05547258AAD122D8E99AD8A24668699AC4F5A5C4B7C28A537C8603EE.EBDF843E9902A27D076B2A4CB17EE3BBB45887E721D0F2F15337B4EA04665975; accessToken2=d4f2b49f4ee64f66b067f34fff8ba033; loginId2=27125870; gray2=false; appType2=6");

        //post
        try {
            request = new WebRequest(new URL("https://sports.lifesense.com/sport_service/sport/sport/uploadMobileStepV2?country=%E4%B8%AD%E5%9B%BD&city=%E5%B9%BF%E5%B7%9E&cityCode=440100&timezone=Asia%2FShanghai&latitude=23.118415&os_country=CN&channel=huawei&language=zh&openudid=&platform=android&province=%E5%B9%BF%E4%B8%9C%E7%9C%81&appType=6&requestId=ab885c6925ac4c35b49422da9b5b4b1f&countryCode=&systemType=2&longitude=113.316503&devicemodel=COL-AL10&area=CN&screenwidth=1080&os_langs=zh&provinceCode=440000&promotion_channel=huawei&rnd=902df954&version=4.6.7&areaCode=440106&requestToken=137c4e133ea3361bfd7c25870341e099&network_type=wifi&osversion=10&screenheight=2190"), HttpMethod.POST);
            request.setProxyHost(webClient.getOptions().getProxyConfig().getProxyHost());
            request.setProxyPort(webClient.getOptions().getProxyConfig().getProxyPort());
            request.removeAdditionalHeader("Accept");
            request.removeAdditionalHeader("Accept-Encoding");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            log.error("创建URL异常，原因：" + e.getMessage());
        }
    }

    /**
     * 获取一个WebClient
     */
    private WebClient getWebClient() {
        //创建一个WebClient，并随机初始化一个浏览器模型
        WebClient webClient = new WebClient();

        //几个重要配置
        webClient.getCookieManager().setCookiesEnabled(true);//启用cookie
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//抛出失败的状态码
        webClient.getOptions().setThrowExceptionOnScriptError(false);//抛出js异常
        webClient.getOptions().setUseInsecureSSL(true);//忽略ssl认证
        webClient.getOptions().setJavaScriptEnabled(false);//启用js
        webClient.getOptions().setRedirectEnabled(false);//启用重定向
        webClient.getOptions().setCssEnabled(false);//启用css
        webClient.getOptions().setTimeout(5000); //设置连接超时时间
        webClient.waitForBackgroundJavaScript(5000);//设置等待js响应时间

        return webClient;
    }

    /**
     * 定时更新运动步数
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void updateSportsScheduler() {
        asyncTask("10000");
    }
    @Scheduled(cron = "0 0 12 * * ?")
    public void updateSportsScheduler2() {
        asyncTask("15000");
    }
    @Scheduled(cron = "0 0 15 * * ?")
    public void updateSportsScheduler3() {
        asyncTask("20520");
    }

    /**
     * 异步任务，构造请求，更新步数
     * @param buShu 步数
     */
    @Async
    public void asyncTask(String buShu) {
        try {
            Date dateTime = new Date();
            log.info("定时更新运动步数开始 ---" + simpleDateFormat.format(dateTime));
            String date = simpleDateFormat2.format(dateTime);
            String time = simpleDateFormat.format(dateTime);

            int distance = Integer.valueOf(buShu) / 15;
            double calories = Integer.valueOf(buShu) / 2000;

            log.info("本次更新步数：" + buShu);
            log.info("本次更新距离：" + distance);
            log.info("本次更新卡路里：" + calories);

            //设置参数
            request.setRequestBody("{\"list\":[{\"active\":1,\"calories\":"+calories+",\"created\":\""+time+"\",\"dataSource\":2,\"dayMeasurementTime\":\""+date+"\",\"deviceId\":\"M_NULL\",\"distance\":"+distance+",\"id\":\""+id+"\",\"isUpload\":0,\"measurementTime\":\""+time+"\",\"priority\":0,\"step\":"+buShu+",\"type\":2,\"updated\":"+dateTime.getTime()+",\"userId\":\""+userId+"\",\"DataSource\":2,\"exerciseTime\":0}]}");

            //发送请求
            Page page = webClient.getPage(request);

            log.info("响应结果：");
            WebResponse webResponse = page.getWebResponse();
            log.info(webResponse.getStatusCode()+"");
            log.info(webResponse.getStatusMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("定时更新运动步数定时任务创建失败，原因：" + e.getMessage());
        }
        log.info("定时更新运动步数任务结束");
    }
}
