package com.reliable.message.client.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.client.dao.ClientMessageDataMapper;
import com.reliable.message.client.feign.MessageFeign;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.dto.MessageData;
import com.reliable.message.common.util.UUIDUtil;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.domain.ServerMessageData;
import com.reliable.message.common.enums.ExceptionCodeEnum;
import com.reliable.message.common.enums.MessageTypeEnum;
import com.reliable.message.common.exception.BusinessException;
import com.reliable.message.common.wrapper.Wrapper;
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
	public void saveWaitConfirmMessage(final ClientMessageData clientMessageData) {
		//当前应用的本地消息存储
		this.saveProducerMessage(clientMessageData);
		//可靠消息服务远程接口
		Wrapper wrapper = messageFeign.saveMessageWaitingConfirm(clientMessageData);
		log.info("<== saveWaitConfirmMessage - 存储预发送消息成功. messageKey={}, wrapper={}", clientMessageData.getMessageKey(),wrapper.getCode());
	}

	@Override
	public void saveProducerMessage(ClientMessageData mqMessageData) {
		// 校验消息数据
		this.checkMessage(mqMessageData);
		// 先保存消息
		mqMessageData.setMessageType(MessageTypeEnum.PRODUCER_MESSAGE.messageType());
		mqMessageDataMapper.insert(mqMessageData);
	}

	@Async
	@Override
	public void confirmAndSendMessage(String producerMessageId) {
		Wrapper wrapper = messageFeign.confirmAndSendMessage(producerMessageId);
		if (wrapper == null) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_CONFIRM_AND_SEND_MESSAGE_ERROR);
		}
		log.info("<== saveMqProducerMessage - 存储并发送消息给消息中心成功. producerMessageId={} thread={}", producerMessageId,Thread.currentThread().getName());
	}

	@Override
	public void confirmReceiveMessage(String consumerGroup, MessageData messageData) {
		log.info("confirmReceiveMessage - 消费者={}, 确认收到messageId={}的消息", consumerGroup, messageData.getId());
		ClientMessageData clientMessageData = new ClientMessageData(UUIDUtil.getId()
				,messageData.getMessageBody(),messageData.getMessageTopic(),messageData.getMessageKey());
		clientMessageData.setMessageType(MessageTypeEnum.CONSUMER_MESSAGE.messageType());
		clientMessageData.setProducerMessageId(messageData.getProducerMessageId());
		clientMessageData.setMessageVersion(messageData.getMessageVersion());
		clientMessageData.setDelayLevel(messageData.getDelayLevel());
		clientMessageData.setSendTime(messageData.getSendTime());
		Date currentDate = new Date();
		clientMessageData.setCreatedTime(currentDate);
		clientMessageData.setUpdateTime(currentDate);
		mqMessageDataMapper.insert(clientMessageData);
	}

	@Async
	@Override
	public void confirmFinishMessage(String confirmId) {
		Wrapper wrapper = messageFeign.confirmFinishMessage(confirmId);
		log.info("tpcMqMessageFeignApi.confirmReceiveMessage result={} Thread={}", wrapper,Thread.currentThread().getName());
		if (wrapper == null) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_CONFIRM_FINISH_MESSAGE_ERROR);
		}
	}

	@Override
	public boolean checkMessageStatus(String producerMessageId,int type) {
		ClientMessageData clientMessageData = mqMessageDataMapper.getClientMessageByProducerMessageIdAndType(producerMessageId,type);
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
