package com.reliable.message.client.service;


import com.reliable.message.model.domain.MqMessageData;

public interface MqMessageService {
	/**
	 * 预发送消息，在本地事物还没有执行的时候，先在本地服务方，添加本地的一条消息记录
	 * 该消息作用是来保证本地事物执行成功，但是没有成功投递消息，根据这个数据，来恢复待确认的消息
	 * @param mqMessageData
	 */
	void saveWaitConfirmMessage(MqMessageData mqMessageData);


	/**
	 * 保存消息到本地
	 * @param mqMessageData
	 */
	void saveMqProducerMessage(MqMessageData mqMessageData);

    void confirmAndSendMessage(String messageId);

    void confirmReceiveMessage(String consumerGroup, MqMessageData dto);

	void saveAndConfirmFinishMessage(String consumerGroup, String messageKey);

    boolean checkMessageStatus(MqMessageData dto);
}
