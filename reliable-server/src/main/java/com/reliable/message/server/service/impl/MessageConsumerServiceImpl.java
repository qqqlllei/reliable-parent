package com.reliable.message.server.service.impl;

import com.reliable.message.server.dao.MqConsumerMapper;
import com.reliable.message.server.service.MessageConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MessageConsumerServiceImpl implements MessageConsumerService {

    @Autowired
    private MqConsumerMapper mqConsumerMapper;


    @Override
    public List<String> listConsumerGroupByTopic(String messageTopic) {
        return mqConsumerMapper.listConsumerGroupByTopic(messageTopic);
    }
}