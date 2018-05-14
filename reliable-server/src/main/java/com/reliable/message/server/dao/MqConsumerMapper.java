package com.reliable.message.server.dao;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/14.
 */
public interface MqConsumerMapper {
    List<String> listConsumerGroupByTopic(String messageTopic);
}
