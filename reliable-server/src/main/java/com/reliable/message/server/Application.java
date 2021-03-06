package com.reliable.message.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Created by 李雷 on 2018/5/10.
 */

@SpringBootApplication
@MapperScan(basePackages = {"com.reliable.message.server.dao"})
@EnableKafka
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

}
