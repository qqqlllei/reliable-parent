package com.reliable.message.server.service;

import com.reliable.message.server.domain.MessageConsumer;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MessageConsumerService {
    List<String> getConsumerGroupNameByTopic(String messageTopic);

    List<MessageConsumer> getConsumersByTopic(String topic);
}
