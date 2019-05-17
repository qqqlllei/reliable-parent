package com.reliable.message.server.service;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ReliableMessage;
import com.reliable.message.common.netty.message.DirectSendRequest;
import com.reliable.message.common.netty.message.SaveAndSendRequest;
import com.reliable.message.common.netty.message.WaitingConfirmRequest;
import com.reliable.message.server.domain.MessageConfirm;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MessageService {
    void saveMessageWaitingConfirm(WaitingConfirmRequest waitingConfirmRequest);

    void confirmAndSendMessage(String producerMessageId);

    void directSendMessage(String messageData, String topic, String key);

    void confirmReceiveMessage(String consumerGroup, String producerMessageId);

    void confirmFinishMessage(String confirmId);

    ReliableMessage getServerMessageDataByProducerMessageId(String producerMessageId);

    List<ReliableMessage> getServerMessageDataByParams(JSONObject jsonObject);

    void deleteServerMessageDataById(String id);

    List<ReliableMessage> getWaitConfirmServerMessageData(JSONObject jobTaskParameter);

    List<ReliableMessage> getSendingMessageData(JSONObject jobTaskParameter);

    void sendMessageToMessageQueue(List<MessageConfirm> confirmList, final ReliableMessage message );

    void directSendMessage(DirectSendRequest directSendRequest);

    void saveAndSendMessage(SaveAndSendRequest saveAndSendRequest);

    void clearFinishMessage(String messageId);

    void updateSendingMessage(ReliableMessage reliableMessage, MessageConfirm messageConfirm);

    void updateById(ReliableMessage reliableMessage);
}
