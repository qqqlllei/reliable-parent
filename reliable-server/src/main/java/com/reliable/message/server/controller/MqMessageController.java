package com.reliable.message.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.wrapper.Wrapper;
import com.reliable.message.server.feign.ClientMessageAdapter;
import com.reliable.message.server.feign.ClientMessageFeign;
import com.reliable.message.server.service.MqMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by 李雷 on 2018/5/10.
 */
@RestController
@RequestMapping("/message")
public class MqMessageController {

    @Autowired
    private MqMessageService messageService;



    @Autowired
    private DiscoveryClient discoveryClient;


    @Autowired
    private ClientMessageAdapter clientMessageAdapter;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/saveMessageWaitingConfirm")
    Wrapper saveMessageWaitingConfirm(@RequestBody ClientMessageData clientMessageData){
        logger.info("预存储消息. mqMessageDto={}", JSONObject.toJSONString(clientMessageData));
        messageService.saveMessageWaitingConfirm(clientMessageData);
        return null;
    }


    @RequestMapping("/confirmAndSendMessage")
    Wrapper confirmAndSendMessage(@RequestParam("producerMessageId") String producerMessageId){
        logger.info("确认并发送消息. producerMessageId={}", producerMessageId);
        messageService.confirmAndSendMessage(producerMessageId);
        return Wrapper.ok();
    }

    @RequestMapping("/confirmFinishMessage")
    Wrapper confirmFinishMessage(@RequestParam("consumerGroup") final String consumerGroup, @RequestParam("producerMessageId") final String producerMessageId){
        logger.info("确认完成消费消息. consumerGroup={}, producerMessageId={}", consumerGroup, producerMessageId);
        messageService.confirmFinishMessage(consumerGroup, producerMessageId);
        // 主动清除生产者消息

        return Wrapper.ok();
    }

    @RequestMapping("/aaa")
    public List<ServiceInstance> aaa(String name) throws URISyntaxException {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(name);


        int index = new Random().nextInt(serviceInstances.size());
        ServiceInstance serviceInstance = serviceInstances.get(index);
        String url = serviceInstance.getUri()+"/deleteMessage/123";
        clientMessageAdapter.getClientMessageData(url,new ArrayList<String>());
        return serviceInstances;
    }
}
