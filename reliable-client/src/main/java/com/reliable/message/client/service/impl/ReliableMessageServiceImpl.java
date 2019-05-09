package com.reliable.message.client.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.dto.MessageData;
import com.reliable.message.common.enums.ExceptionCodeEnum;
import com.reliable.message.common.enums.MessageTypeEnum;
import com.reliable.message.common.exception.BusinessException;
import com.reliable.message.common.netty.message.RequestMessage;
import com.reliable.message.common.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by 李雷
 */
@Slf4j
@Service
public class ReliableMessageServiceImpl implements ReliableMessageService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void confirmReceiveMessage(String consumerGroup, MessageData messageData) {
		log.info("confirmReceiveMessage - 消费者={}, 确认收到messageId={}的消息", consumerGroup, messageData.getId());
		Date currentDate = new Date();
		String sql = "INSERT INTO client_message_data " +
				"(id ,version ,producer_message_id,producer_group,message_key,message_topic,message_type,message_body,message_version,delay_level,status,send_time,created_time,update_time) " +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


		Object args[] = {
				UUIDUtil.getId(),
				0,
				messageData.getProducerMessageId(),
				messageData.getProducerGroup(),
				messageData.getMessageKey(),
				messageData.getMessageTopic(),
				MessageTypeEnum.CONSUMER_MESSAGE.messageType(),
				messageData.getMessageBody(),
				messageData.getMessageVersion(),
				messageData.getDelayLevel(),
				null,
				messageData.getSendTime(),
				currentDate,
				currentDate
		};

		jdbcTemplate.update(sql,args);



	}

	@Override
	public boolean hasConsumedMessage(String producerMessageId,int type) {
		String sql = " SELECT COUNT(*) FROM client_message_data WHERE  producer_message_id= ? and message_type = ?";
		Object args[] ={producerMessageId,type};
		int count = jdbcTemplate.queryForObject(sql,args,Integer.class);
		if(count != 0) return true;
		return false;
	}

	@Override
	public boolean hasProducedMessage(String producerMessageId) {

		String sql = " SELECT COUNT(*) FROM client_message_data WHERE  id= ?";
		Object args[] ={producerMessageId};
		int count = jdbcTemplate.queryForObject(sql,args,Integer.class);
		if(count != 0) return true;
		return false;
	}

	@Override
	public void deleteMessageByProducerMessageId(String producerMessageId) {
	}

	@Override
	public List<String> getProducerMessage(JSONObject jobTaskParameter) {


		String sql = "SELECT id FROM client_message_data " +
				"WHERE message_type= ? AND send_time < DATE_SUB(CURDATE(),INTERVAL 1 DAY) AND " +
				"MOD(id, ?) = ? Limit 0, ?";
		Object args[] ={
				MessageTypeEnum.PRODUCER_MESSAGE.messageType(),
				jobTaskParameter.getString("shardingTotalCount"),
				jobTaskParameter.getString("shardingItem"),
				jobTaskParameter.getString("fetchNum")};


		return jdbcTemplate.queryForList(sql,args,String.class);
	}

	@Override
	public void saveMessage(RequestMessage requestMessage) {

		checkMessage(requestMessage);
		requestMessage.executeSql(jdbcTemplate);
	}



	private void checkMessage(RequestMessage requestMessage) {
		if (null == requestMessage) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_IS_NULL);
		}
		String messageTopic = requestMessage.getMessageTopic();
		String messageBody = requestMessage.getMessageBody();
		String producerGroup = requestMessage.getProducerGroup();
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
