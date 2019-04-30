package com.reliable.message.client.controller;

import com.reliable.message.client.netty.NettyClient;
import com.reliable.message.common.netty.RequestMessage;
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
        RequestMessage message = new RequestMessage();
        message.setId(UUID.randomUUID().toString());
        message.setMessageType(1);
        try {
            Object response = nettyClient.getNettyClientHandler().sendMessage(message);
           return response.toString();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
