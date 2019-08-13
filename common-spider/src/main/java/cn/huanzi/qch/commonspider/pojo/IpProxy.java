package cn.huanzi.qch.commonspider.pojo;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 爬虫IP代理池实体对象
 */
@Data
@Entity(name = "spider_ip_proxy")
public class IpProxy {
    @Id
    //ip地址
    private String ip;
    //端口
    private String port;
    //城市
    private String city;
    //运营商
    private String operator;
}
