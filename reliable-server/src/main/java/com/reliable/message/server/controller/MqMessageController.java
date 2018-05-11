package com.reliable.message.client.server.controller;

import com.reliable.message.client.server.service.MqMessageService;
import com.reliable.message.model.dto.TpcMqMessageDto;
import com.reliable.message.model.wrapper.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
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
    Wrapper saveMessageWaitingConfirm(TpcMqMessageDto tpcMqMessageDto){
        logger.info("预存储消息. mqMessageDto={}", tpcMqMessageDto);
        messageService.saveMessageWaitingConfirm(tpcMqMessageDto);
        return null;
    }


    Wrapper confirmAndSendMessage(String messageKey){
        return null;
    }

}
