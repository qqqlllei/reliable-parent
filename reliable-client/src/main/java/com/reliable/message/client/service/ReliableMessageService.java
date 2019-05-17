package com.reliable.message.client.service;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ReliableMessage;
import com.reliable.message.common.dto.MessageData;
import com.reliable.message.common.enums.ExceptionCodeEnum;
import com.reliable.message.common.enums.MessageSendStatusEnum;
import com.reliable.message.common.enums.MessageTypeEnum;
import com.reliable.message.common.exception.BusinessException;
import com.reliable.message.common.util.TimeUtil;
import com.reliable.message.common.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by 李雷
 */
@Slf4j
public class ReliableMessageService {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	public void confirmReceiveMessage(String consumerGroup, MessageData messageData) {
		log.info("confirmReceiveMessage - 消费者={}, 确认收到messageId={}的消息", consumerGroup, messageData.getId());
		Date currentDate = new Date();
		String sql = "INSERT INTO reliable_message " +
				"(id ,version ,producer_message_id,producer_group,message_key,message_topic,message_type,message_body,message_version,delay_level,status,send_time,create_time,update_time) " +
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


	public boolean hasConsumedMessage(String producerMessageId,int type) {
		String sql = " SELECT COUNT(*) FROM client_message_data WHERE  producer_message_id= ? and message_type = ?";
		Object args[] ={producerMessageId,type};
		int count = jdbcTemplate.queryForObject(sql,args,Integer.class);
		if(count != 0) return true;
		return false;
	}


	public boolean hasProducedMessage(String producerMessageId) {

		String sql = " SELECT COUNT(*) FROM client_message_data WHERE  id= ?";
		Object args[] ={producerMessageId};
		int count = jdbcTemplate.queryForObject(sql,args,Integer.class);
		if(count != 0) return true;
		return false;
	}


	public void deleteMessageByProducerMessageId(String producerMessageId) {
	}


	public List<String> getProducerMessage(JSONObject jobTaskParameter) {

		String scanTime = TimeUtil.getBeforeByMinuteTime(1);
		String sql = "SELECT id FROM reliable_message " +
				"WHERE message_type= ? and status = ? and send_time < ? ORDER BY send_time ASC Limit 0, ?";
		Object args[] ={
				MessageTypeEnum.PRODUCER_MESSAGE.messageType(),
				MessageSendStatusEnum.SENDING.sendStatus(),
				scanTime,
				jobTaskParameter.getIntValue("fetchNum")};


		return jdbcTemplate.queryForList(sql,args,String.class);
	}



	public Map<String, Object> getRequestMessageById(String id) {

		String getSql = "SELECT id,version,producer_group AS producerGroup,producer_message_id AS producerMessageId," +
				"message_key AS messageKey,message_topic AS messageTopic,message_type AS messageType," +
				"message_body AS messageBody,message_version AS messageVersion,delay_level AS delayLevel," +
				"status,send_time AS sendTime  FROM reliable_message WHERE id = ?";
		Object getArgs[] ={id};
		return jdbcTemplate.queryForMap(getSql,getArgs);
	}


	public void updateMessageStatusToFinish(String id) {
		String sql = "UPDATE reliable_message SET status = ? WHERE id = ?";
		Object args[] = {MessageSendStatusEnum.FINISH.sendStatus(),id};
		jdbcTemplate.update(sql,args);
	}


	public void saveMessage(ReliableMessage requestMessage) {

		checkMessage(requestMessage);

		String sql = "INSERT INTO reliable_message " +
				"(id ,version ,producer_message_id,producer_group,message_key,message_topic,message_type,message_body,message_version,delay_level,status,send_time,create_time,update_time) VALUES " +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		Object args[] = {
				requestMessage.getId(),
				0,
				requestMessage.getProducerMessageId(),
				requestMessage.getProducerGroup(),
				requestMessage.getMessageKey(),
				requestMessage.getMessageTopic(),
				requestMessage.getMessageType(),
				requestMessage.getMessageBody(),
				requestMessage.getMessageVersion(),
				requestMessage.getDelayLevel(),
				requestMessage.getStatus(),
				requestMessage.getSendTime(),
				requestMessage.getCreateTime(),
				requestMessage.getUpdateTime()
		};

		jdbcTemplate.update(sql,args);
	}

	private void checkMessage(ReliableMessage requestMessage) {
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
