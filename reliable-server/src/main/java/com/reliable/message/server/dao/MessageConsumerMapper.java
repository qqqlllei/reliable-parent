package com.reliable.message.server.dao;

import com.reliable.message.server.domain.MessageConsumer;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/14.
 */
public interface MessageConsumerMapper {
    List<String> getConsumerGroupNameByTopic(String messageTopic);

    List<MessageConsumer> getConsumersByTopic(String topic);
}
