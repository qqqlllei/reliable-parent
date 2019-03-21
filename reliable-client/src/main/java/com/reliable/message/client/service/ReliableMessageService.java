package com.reliable.message.client.service;


import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.domain.ServerMessageData;

import java.util.List;

public interface ReliableMessageService {
	/**
	 * 预发送消息，在本地事物还没有执行的时候，先在本地服务方，添加本地的一条消息记录
	 * 该消息作用是来保证本地事物执行成功，但是没有成功投递消息，根据这个数据，来恢复待确认的消息
	 * @param mqMessageData
	 */
	void saveWaitConfirmMessage(ClientMessageData mqMessageData);


	/**
	 * 保存消息到本地
	 * @param mqMessageData
	 */
	void saveProducerMessage(ClientMessageData mqMessageData);

    void confirmAndSendMessage(String producerMessageId);

    void confirmReceiveMessage(String consumerGroup, ServerMessageData dto);

	void confirmFinishMessage(String consumerGroup, String producerMessageId);

    boolean checkMessageStatus(String producerMessageId,int type);

	void deleteMessageByProducerMessageId(String producerMessageId);

	List<ClientMessageData> getProducerMessage(JSONObject jobTaskParameter);

    ClientMessageData getClientMessageDataByProducerMessageId(String producerMessageId);

    void directSendMessage(ClientMessageData domain);

	void saveAndSendMessage(ClientMessageData domain);
}
