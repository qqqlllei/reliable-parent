package com.reliable.message.server.service;

import com.reliable.message.model.dto.TpcMqMessageDto;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MqMessageService {
    void saveMessageWaitingConfirm(TpcMqMessageDto tpcMqMessageDto);

    void confirmAndSendMessage(String messageKey);

    void directSendMessage(String body, String topic, String key, String producerGroup, Integer delayLevel);
}
