package com.reliable.message.client.controller;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.client.netty.NettyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by 李雷 on 2019/4/29.
 */
@RestController
public class IndexController {


    @Autowired
    private NettyClient nettyClient;


    @RequestMapping("/")
    public String sendMessage(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","request");
        jsonObject.put("body","123");
        jsonObject.put("id", UUID.randomUUID());
        try {
            Object response = nettyClient.getNettyClientHandler().sendMessage(jsonObject);
           return response.toString();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
