package com.reliable.message.client.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by 李雷 on 2018/5/11.
 */

public class MessageClientUniqueId {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public long getNextIdByApplicationName(String groupName,String tableName){
        return stringRedisTemplate.opsForValue().increment(groupName+tableName,1);
    }
}
