package com.reliable.message.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.wrapper.Wrapper;
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
    Wrapper confirmAndSendMessage(@RequestParam("messageKey") String clientMessageId){
        logger.info("确认并发送消息. messageKey={}", clientMessageId);
        messageService.confirmAndSendMessage(clientMessageId);
        return Wrapper.ok();
    }

    @RequestMapping("/confirmReceiveMessage")
    Wrapper confirmReceiveMessage(@RequestParam("cid") final String consumerGroup, @RequestParam("messageKey") final String messageKey){
        logger.info("确认收到消息. consumerGroup={}, messageKey={}", consumerGroup, messageKey);
        messageService.confirmReceiveMessage(consumerGroup, messageKey);
        return Wrapper.ok();
    }

    @RequestMapping("/confirmConsumedMessage")
    Wrapper confirmConsumedMessage(@RequestParam("consumerGroup") final String consumerGroup, @RequestParam("messageKey") final String messageKey){
        logger.info("确认完成消费消息. consumerGroup={}, messageKey={}", consumerGroup, messageKey);
        messageService.confirmConsumedMessage(consumerGroup, messageKey);
        return Wrapper.ok();
    }

}
