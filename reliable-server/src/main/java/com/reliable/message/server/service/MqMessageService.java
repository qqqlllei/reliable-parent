package com.reliable.message.server.service;

import com.reliable.message.model.domain.ClientMessageData;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MqMessageService {
    void saveMessageWaitingConfirm(ClientMessageData tpcMqMessageDto);

    void confirmAndSendMessage(String messageId);

    void directSendMessage(String body, String topic, String key, String producerGroup, Integer delayLevel);

    void confirmReceiveMessage(String consumerGroup, String messageKey);

    void confirmConsumedMessage(String consumerGroup, String messageKey);
}
