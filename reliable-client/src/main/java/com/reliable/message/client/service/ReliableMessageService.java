package com.reliable.message.client.service;


import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.domain.ServerMessageData;
import com.reliable.message.common.dto.MessageData;

import java.util.List;
/**
 * Created by 李雷
 */
public interface ReliableMessageService {
//	void saveWaitConfirmMessage(ClientMessageData mqMessageData);
	void saveProducerMessage(ClientMessageData mqMessageData);
//    void confirmAndSendMessage(String producerMessageId);
    void confirmReceiveMessage(String consumerGroup, MessageData dto);
//	void confirmFinishMessage(String confirmId);
    boolean checkMessageStatus(String producerMessageId,int type);
	void deleteMessageByProducerMessageId(String producerMessageId);
	List<ClientMessageData> getProducerMessage(JSONObject jobTaskParameter);
    ClientMessageData getClientMessageDataByProducerMessageId(String producerMessageId);
//    void directSendMessage(ClientMessageData domain);
//	void saveAndSendMessage(ClientMessageData domain);
}
