package com.ydles.canal;

import com.xpand.starter.canal.annotation.EnableCanalClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@EnableCanalClient //这个程序是用来启动canal的

public class CanalApplication<exclude> {

    public static void main(String[] args) {
        SpringApplication.run(CanalApplication.class, args);
    }
}
