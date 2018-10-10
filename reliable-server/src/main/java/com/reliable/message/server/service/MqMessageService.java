package com.reliable.message.server.service;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.server.domain.ServerMessageData;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MqMessageService {
    void saveMessageWaitingConfirm(ClientMessageData tpcMqMessageDto);

    void confirmAndSendMessage(String messageId);

    void directSendMessage(ServerMessageData messageData, String topic, String key);

    void confirmReceiveMessage(String consumerGroup, String messageKey);

    void confirmFinishMessage(String consumerGroup, String messageKey);

    ServerMessageData getServerMessageDataByProducerMessageId(String producerMessageId);

    List<ServerMessageData> getServerMessageDataByParams(JSONObject jsonObject);

    void deleteServerMessageDataById(Long id);
}
