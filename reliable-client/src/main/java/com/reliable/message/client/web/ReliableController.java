package com.reliable.message.client.web;

import com.reliable.message.client.service.MqMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 李雷 on 2018/10/8.
 */
@RestController
public class ReliableController {


    @Autowired
    private MqMessageService mqMessageService;


    @RequestMapping("/deleteMessage/{producerMessageId}")
    public void deleteMessageByProducerMessageId(@PathVariable("producerMessageId") String producerMessageId){

        mqMessageService.deleteMessageByProducerMessageId(producerMessageId);

    }

}
