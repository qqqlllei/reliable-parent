package com.reliable.message.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.wrapper.Wrapper;
import com.reliable.message.server.domain.ServerMessageData;
import com.reliable.message.server.service.MqMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 李雷 on 2018/5/10.
 */
@RestController
@RequestMapping("/message")
public class MqMessageController {

    @Autowired
    private MqMessageService messageService;

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
        return Wrapper.ok();
    }

    @RequestMapping("/checkServerMessageIsExist")
    Wrapper checkServerMessageIsExist(@RequestParam("producerMessageId") final String producerMessageId){
        ServerMessageData serverMessageData = messageService.getServerMessageDataByProducerMessageId(producerMessageId);

        if(serverMessageData!=null) return Wrapper.ok().result(true);

        return Wrapper.ok().result(false);
    }
}
