package cn.huanzi.qch.sportslifesense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync//开启异步调用
@EnableScheduling //允许支持定时器了
public class SportsLifesenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportsLifesenseApplication.class, args);
    }

}
