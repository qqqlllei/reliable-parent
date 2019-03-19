package com.reliable.message.client.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.client.dao.ClientMessageDataMapper;
import com.reliable.message.client.feign.MessageFeign;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.enums.ExceptionCodeEnum;
import com.reliable.message.model.enums.MessageTypeEnum;
import com.reliable.message.model.exception.BusinessException;
import com.reliable.message.model.wrapper.Wrapper;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Slf4j
@Service
public class ReliableMessageServiceImpl implements ReliableMessageService {
	@Autowired
	private ClientMessageDataMapper mqMessageDataMapper;
	@Autowired
	private MessageFeign messageFeign;

	@Override
	public void saveWaitConfirmMessage(final ClientMessageData mqMessageData) {
		//当前应用的本地消息存储
		this.saveProducerMessage(mqMessageData);
		//可靠消息服务远程接口
		Wrapper wrapper = messageFeign.saveMessageWaitingConfirm(mqMessageData);
		log.info("<== saveWaitConfirmMessage - 存储预发送消息成功. messageKey={}, wrapper={}", mqMessageData.getMessageKey(),wrapper.getCode());
	}

	@Override
	public void saveProducerMessage(ClientMessageData mqMessageData) {
		// 校验消息数据
		this.checkMessage(mqMessageData);
		// 先保存消息
		mqMessageData.setMessageType(MessageTypeEnum.PRODUCER_MESSAGE.messageType());
		Date currentDate = new Date();
		mqMessageData.setCreatedTime(currentDate);
		mqMessageData.setUpdateTime(currentDate);
		mqMessageData.setProducerMessageId(mqMessageData.getProducerGroup()+"-"+mqMessageData.getId());
		mqMessageDataMapper.insert(mqMessageData);
	}

	@Async
	@Override
	public void confirmAndSendMessage(String producerMessageId) {
		Wrapper wrapper = messageFeign.confirmAndSendMessage(producerMessageId);
		if (wrapper == null) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_CONFIRM_AND_SEND_MESSAGE_ERROR);
		}
		log.info("<== saveMqProducerMessage - 存储并发送消息给消息中心成功. producerMessageId={}", producerMessageId);
	}

	@Override
	public void confirmReceiveMessage(String consumerGroup, ClientMessageData messageData) {
		final Long messageId = messageData.getId();
		log.info("confirmReceiveMessage - 消费者={}, 确认收到messageId={}的消息", consumerGroup, messageId);
		// 先保存消息
		messageData.setMessageType(MessageTypeEnum.CONSUMER_MESSAGE.messageType());
		Date currentTime = new Date();
		messageData.setCreatedTime(currentTime);
		messageData.setUpdateTime(currentTime);
		mqMessageDataMapper.insert(messageData);
	}

	@Async
	@Override
	public void confirmFinishMessage(String consumerGroup, String messageKey) {
		Wrapper wrapper = messageFeign.confirmFinishMessage(consumerGroup, messageKey);
		log.info("tpcMqMessageFeignApi.confirmReceiveMessage result={}", wrapper);
		if (wrapper == null) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_CONFIRM_FINISH_MESSAGE_ERROR);
		}
	}

	@Override
	public boolean checkMessageStatus(Long messageId,int type) {
		ClientMessageData clientMessageData = mqMessageDataMapper.getClientMessageByMessageIdAndType(messageId,type);
		if(clientMessageData !=null ) return true;
		return false;
	}

	@Override
	public void deleteMessageByProducerMessageId(String producerMessageId) {
		mqMessageDataMapper.deleteMessageByProducerMessageId(producerMessageId);
	}

	@Override
	public List<ClientMessageData> getProducerMessage(JSONObject jobTaskParameter) {
		//设置消息类型
		jobTaskParameter.put("messageType", MessageTypeEnum.PRODUCER_MESSAGE.messageType());
		//检查清除时间
		return mqMessageDataMapper.getClientMessageByParams(jobTaskParameter);
	}

	@Override
	public ClientMessageData getClientMessageDataByProducerMessageId(String producerMessageId) {
		return mqMessageDataMapper.getClientMessageDataByProducerMessageId(producerMessageId);
	}

	@Override
	public void directSendMessage(ClientMessageData clientMessageData) {
		messageFeign.directSendMessage(clientMessageData);
	}

	@Override
	public void saveAndSendMessage(ClientMessageData clientMessageData) {
		messageFeign.saveAndSendMessage(clientMessageData);
	}


	private void checkMessage(ClientMessageData mqMessageData) {
		if (null == mqMessageData) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_IS_NULL);
		}
		String messageTopic = mqMessageData.getMessageTopic();
		String messageBody = mqMessageData.getMessageBody();
		String producerGroup = mqMessageData.getProducerGroup();
		if (StringUtils.isEmpty(messageTopic)) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_OF_MESSAGE_TOPIC_IS_NULL);
		}
		if (StringUtils.isEmpty(messageBody)) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_OF_MESSAGE_BODY_IS_NULL);
		}

		if (StringUtils.isEmpty(producerGroup)) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_OF_MESSAGE_GROUP_IS_NULL);
		}
	}



}
