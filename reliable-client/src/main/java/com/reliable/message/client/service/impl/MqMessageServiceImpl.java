package com.reliable.message.client.service.impl;

import com.reliable.message.client.feign.MqMessageFeign;
import com.reliable.message.client.mapper.MqMessageDataMapper;

import com.reliable.message.client.service.MqMessageService;
import com.reliable.message.model.domain.MqMessageData;
import com.reliable.message.model.dto.TpcMqMessageDto;
import com.reliable.message.model.enums.ExceptionCodeEnum;
import com.reliable.message.model.enums.MqMessageTypeEnum;
import com.reliable.message.model.exception.BusinessException;
import com.reliable.message.model.wrapper.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;


@Slf4j
@Service
public class MqMessageServiceImpl implements MqMessageService {
	@Autowired
	private MqMessageDataMapper mqMessageDataMapper;
	@Autowired
	private MqMessageFeign mqMessageFeign;
//	@Resource
//	private TaskExecutor taskExecutor;

//	@Value("${spring.profiles.active}")
//	String profile;
//	@Value("${spring.application.name}")
//	String applicationName;

	@Override
	public void saveWaitConfirmMessage(final MqMessageData mqMessageData) {
		//当前应用的本地消息存储
		this.saveMqProducerMessage(mqMessageData);
		// 发送预发送状态的消息给消息中心
		TpcMqMessageDto tpcMqMessageDto = mqMessageData.getTpcMqMessageDto();
		//可靠消息服务远程接口
		mqMessageFeign.saveMessageWaitingConfirm(tpcMqMessageDto);
		log.info("<== saveWaitConfirmMessage - 存储预发送消息成功. messageKey={}", mqMessageData.getMessageKey());
	}

	@Override
	public void saveMqProducerMessage(MqMessageData mqMessageData) {
		// 校验消息数据
		this.checkMessage(mqMessageData);
		// 先保存消息
		mqMessageData.setMessageType(MqMessageTypeEnum.PRODUCER_MESSAGE.messageType());
		mqMessageData.setId(UUID.randomUUID().toString());
		mqMessageDataMapper.insert(mqMessageData);
	}

	@Async
	@Override
	public void confirmAndSendMessage(String messageKey) {
		Wrapper wrapper = mqMessageFeign.confirmAndSendMessage(messageKey);
		if (wrapper == null) {
//			throw new TpcBizException(ErrorCodeEnum.GL99990002);
		}
		if (wrapper.error()) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050004, wrapper.getMessage(), messageKey);
		}
		log.info("<== saveMqProducerMessage - 存储并发送消息给消息中心成功. messageKey={}", messageKey);
	}

	@Override
	public void confirmReceiveMessage(String consumerGroup, MqMessageData messageData) {
		final String messageKey = messageData.getMessageKey();
		log.info("confirmReceiveMessage - 消费者={}, 确认收到key={}的消息", consumerGroup, messageKey);
		// 先保存消息
		messageData.setMessageType(MqMessageTypeEnum.CONSUMER_MESSAGE.messageType());
		messageData.setId(UUID.randomUUID().toString());
		mqMessageDataMapper.insert(messageData);

		Wrapper wrapper = mqMessageFeign.confirmReceiveMessage(consumerGroup, messageKey);
		log.info("tpcMqMessageFeignApi.confirmReceiveMessage result={}", wrapper);
		if (wrapper == null) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_CONVERT_EXCEPTION);
		}
		if (wrapper.error()) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_CONVERT_EXCEPTION);
		}
	}

	@Override
	public void saveAndConfirmFinishMessage(String consumerGroup, String messageKey) {
		Wrapper wrapper = mqMessageFeign.confirmConsumedMessage(consumerGroup, messageKey);
		log.info("tpcMqMessageFeignApi.confirmReceiveMessage result={}", wrapper);
		if (wrapper == null) {
//			throw new TpcBizException(ErrorCodeEnum.GL99990002);
		}
		if (wrapper.error()) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050004, wrapper.getMessage(), messageKey);
		}
	}


	private void checkMessage(MqMessageData mqMessageData) {
		if (null == mqMessageData) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050007);
		}
		String messageTopic = mqMessageData.getMessageTopic();
		String messageBody = mqMessageData.getMessageBody();
		String messageKey = mqMessageData.getMessageKey();
		String producerGroup = mqMessageData.getProducerGroup();
		if (StringUtils.isEmpty(messageKey)) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050009);
		}
		if (StringUtils.isEmpty(messageTopic)) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050001);
		}
		if (StringUtils.isEmpty(messageBody)) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050008, mqMessageData.getMessageKey());
		}

		if (StringUtils.isEmpty(producerGroup)) {
//			throw new TpcBizException(ErrorCodeEnum.TPC100500015, mqMessageData.getMessageKey());
		}
	}


}
