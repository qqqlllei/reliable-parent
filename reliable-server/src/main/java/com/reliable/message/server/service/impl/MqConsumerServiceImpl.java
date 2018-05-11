package com.reliable.message.server.service.impl;

import com.reliable.message.server.service.MqConsumerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MqConsumerServiceImpl implements MqConsumerService {

    @Override
    public List<String> listConsumerGroupByTopic(String messageTopic) {
        return null;
    }
}
