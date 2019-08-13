package cn.huanzi.qch.flowspider.cnblogs.pojo;

import lombok.Data;
import javax.persistence.Id;

import javax.persistence.Entity;

/**
 * 博客园博客文章实体对象
 */
@Data
@Entity(name = "spider_blog")
public class Blog {
    @Id
    private String blogUrl;
    private String blogName;
}
