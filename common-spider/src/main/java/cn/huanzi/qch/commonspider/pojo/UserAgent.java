package cn.huanzi.qch.commonspider.pojo;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 爬虫User-Agent池实体对象
 */
@Data
@Entity(name = "spider_user_agent")
public class UserAgent {
    @Id
    //User Agent
    private String userAgent;
}