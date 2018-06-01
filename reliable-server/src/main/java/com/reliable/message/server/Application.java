package com.reliable.message.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Created by 李雷 on 2018/5/10.
 */

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.reliable.message.server.dao"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

}
