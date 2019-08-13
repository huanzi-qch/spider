package cn.huanzi.qch.flowspider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j//使用lombok的@Slf4j，帮我们创建Logger对象，效果与下方获取日志对象一样
@SpringBootApplication//默认只能扫描到当前包和子包
@EnableJpaRepositories(basePackages = {"cn.huanzi.qch.commonspider.repository","cn.huanzi.qch.flowspider.cnblogs.repository"})//扫描@Repository注解；
@EntityScan(basePackages = {"cn.huanzi.qch.commonspider.pojo","cn.huanzi.qch.flowspider.cnblogs.pojo"})//扫描@Entity注解；
@ComponentScan(basePackages = {"cn.huanzi.qch.commonspider.**","cn.huanzi.qch.flowspider.**"})//扫描 带@Component的注解，如：@Controller、@Service 注解
@EnableScheduling //允许支持定时器了
public class FlowSpiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowSpiderApplication.class, args);
    }

    /**
     * 配置内部类
     */
    @Configuration
    class Config {

        /**
         * 端口
         */
        @Value("${server.port}")
        private String port;

        /**
         * 启动成功
         */
        @Bean
        public ApplicationRunner applicationRunner() {
            return applicationArguments -> {
                try {
                    InetAddress ia = InetAddress.getLocalHost();
                    //获取本机内网IP
                    log.info("启动成功：" + "http://" + ia.getHostAddress() + ":" + port + "/");
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
            };
        }
    }
}
