package com.reliable.message.server.service;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MessageConsumerService {
    List<String> listConsumerGroupByTopic(String messageTopic);
}
