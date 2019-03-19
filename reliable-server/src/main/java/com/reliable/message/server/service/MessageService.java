package com.reliable.message.server.service;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.domain.ServerMessageData;
import com.reliable.message.server.domain.MessageConfirm;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MessageService {
    void saveMessageWaitingConfirm(ClientMessageData tpcMqMessageDto);

    void confirmAndSendMessage(Long producerMessageId);

    void directSendMessage(ServerMessageData messageData, String topic, String key);

    void confirmReceiveMessage(String consumerGroup, String producerMessageId);

    void confirmFinishMessage(String consumerGroup, String producerMessageId);

    ServerMessageData getServerMessageDataByProducerMessageId(Long producerMessageId);

    List<ServerMessageData> getServerMessageDataByParams(JSONObject jsonObject);

    void deleteServerMessageDataById(Long id);

    List<ServerMessageData> getWaitConfirmServerMessageData(JSONObject jobTaskParameter);

    List<ServerMessageData> getSendingMessageData(JSONObject jobTaskParameter);

    void sendMessageToMessageQueue(List<MessageConfirm> confirmList, final ServerMessageData message );

    void directSendMessage(ClientMessageData clientMessageData);

    void saveAndSendMessage(ClientMessageData clientMessageData);
}
