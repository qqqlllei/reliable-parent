package com.reliable.message.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by 李雷 on 2018/5/11.
 */

@Service
public class UniqueId {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${spring.application.name}")
    private String SPRING_APPLICATION_NAME;


    public long getNextIdByApplicationName(String tableName){
        return stringRedisTemplate.opsForValue().increment(SPRING_APPLICATION_NAME+tableName,1);
    }
}
