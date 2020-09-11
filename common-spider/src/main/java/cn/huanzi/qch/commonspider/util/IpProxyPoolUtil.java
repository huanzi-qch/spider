package cn.huanzi.qch.commonspider.util;

import cn.huanzi.qch.commonspider.pojo.IpProxy;
import cn.huanzi.qch.commonspider.pojo.UserAgent;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.List;

/**
 * IP代理池工具类
 */
@Slf4j//使用lombok的@Slf4j，帮我们创建Logger对象
public class IpProxyPoolUtil {
    //IP代理池
    private static List<IpProxy> ipProxyPool;

    //User-Agent池
    private static List<UserAgent> userAgentPool;

    /**
     * 更新IP代理、User Agent池
     */
    public static void updateIpProxyPoolAndUserAgentPool(List<IpProxy> ipProxyPools, List<UserAgent> userAgentPools) {
        ipProxyPool = ipProxyPools;
        log.info("HttpClientUtil的IP代理池更新成功，大小：" + ipProxyPool.size());
        userAgentPool = userAgentPools;
        log.info("HttpClientUtil的User Agent池更新成功，大小：" + userAgentPool.size());
    }

    /**
     * 为HttpGetOrHttpPost更新IP代理、User Agent
     */
    public static void updateIpProxyAndUserAgentForHttpGetOrHttpPost(HttpRequestBase httpGetOrHttpPost) {
        //设置IP代理，每次都取不同的
        if (ipProxyPool != null && ipProxyPool.size() > 0) {
            IpProxy ipProxy = ipProxyPool.get(RandomUtil.randomNumber(0, ipProxyPool.size() - 1));
            log.info("更新代理IP，当前使用：" + ipProxy.toString());
            httpGetOrHttpPost.setConfig(RequestConfig.custom().setProxy(new HttpHost(ipProxy.getIp(), Integer.parseInt(ipProxy.getPort()))).build());
        }
        //更新User-Agent
        if (userAgentPool != null && userAgentPool.size() > 0) {
            UserAgent userAgent = userAgentPool.get(RandomUtil.randomNumber(0, userAgentPool.size() - 1));
            log.info("更新User-Agent，当前使用：" + userAgent.getUserAgent());
            httpGetOrHttpPost.addHeader("User-Agent", userAgent.getUserAgent());
        } else {
            //需要设置默认User-Agent，否则自带的值是：User-Agent: Apache-HttpClient/4.5.4 (Java/1.8.0_131)
            BrowserVersion[] versions = {BrowserVersion.FIREFOX_60, BrowserVersion.FIREFOX_52, BrowserVersion.INTERNET_EXPLORER, BrowserVersion.CHROME, BrowserVersion.EDGE};
            httpGetOrHttpPost.addHeader("User-Agent", versions[(int) (versions.length * Math.random())].getUserAgent());
        }
    }

    /**
     * 为WebClient更新IP代理、User Agent
     */
    public static void updateIpProxyAndUserAgentForWebClient(WebClient webClient) {
        //设置IP代理，每次都取不同的
        if (ipProxyPool != null && ipProxyPool.size() > 0) {
            Integer index = RandomUtil.randomNumber(0, ipProxyPool.size() - 1);
            IpProxy ipProxy = ipProxyPool.get(index);
            log.info("更新代理IP，当前使用：" + ipProxy.toString() + ",下标：" + index);
            ProxyConfig proxyConfig = webClient.getOptions().getProxyConfig();
            proxyConfig.setProxyHost(ipProxy.getIp());//ip地址
            proxyConfig.setProxyPort(Integer.parseInt(ipProxy.getPort()));//端口
        }

        //更新User-Agent
        if (userAgentPool != null && userAgentPool.size() > 0) {
            Integer index = RandomUtil.randomNumber(0, userAgentPool.size() - 1);
            UserAgent userAgent = userAgentPool.get(index);
            log.info("更新User Agent，当前使用：" + userAgent.getUserAgent() + ",下标：" + index);
            webClient.removeRequestHeader("User-Agent");
            webClient.addRequestHeader("User-Agent", userAgent.getUserAgent());
        }
    }
}
