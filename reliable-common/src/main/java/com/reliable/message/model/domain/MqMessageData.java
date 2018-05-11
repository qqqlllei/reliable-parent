package com.reliable.message.model.domain;

import com.reliable.message.model.dto.TpcMqMessageDto;
import lombok.Data;

import java.util.Date;

@Data
public class MqMessageData {

	private String id;

	/**
	 * 版本号
	 */
	private Integer version;

	/**
	 * 消息key
	 */
	private String messageKey;

	/**
	 * topic
	 */
	private String messageTopic;

	/**
	 * 消息内容
	 */
	private String messageBody;

	/**
	 * 消息类型: 10 - 生产者 ; 20 - 消费者
	 */
	private Integer messageType;

	/**
	 * 顺序类型, 0有序 1无序
	 */
	private int orderType;

	/**
	 * 消息状态
	 */
	private Integer status;

	/**
	 * 延时级别
	 */
	private int delayLevel;

	/**
	 * 创建人
	 */
	private String creator;

	/**
	 * 创建人ID
	 */
	private Long creatorId;

	/**
	 * 创建时间
	 */
	private Date createdTime;

	/**
	 * 最近操作人
	 */
	private String lastOperator;

	/**
	 * 最后操作人ID
	 */
	private Long lastOperatorId;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 是否删除 -0 未删除 -1 已删除
	 */
	private Integer yn;

	/**
	 * producer group name
	 */
	private String producerGroup;

	public MqMessageData(){

	}
	public MqMessageData(final String msgBody, final String topic, final String key) {
		this.messageBody = msgBody;
		this.messageTopic = topic;
		this.messageKey = key;
	}

	/**
	 * Gets tpc mq message dto.
	 *
	 * @return the tpc mq message dto
	 */
	public TpcMqMessageDto getTpcMqMessageDto() {
		TpcMqMessageDto tpcMqMessageDto = new TpcMqMessageDto();
		tpcMqMessageDto.setMessageBody(this.messageBody);
		tpcMqMessageDto.setMessageKey(this.messageKey);
		tpcMqMessageDto.setMessageTopic(this.messageTopic);
		tpcMqMessageDto.setMessageType(this.messageType);
		tpcMqMessageDto.setRefNo(this.messageKey);
		tpcMqMessageDto.setDelayLevel(this.delayLevel);
		tpcMqMessageDto.setOrderType(this.orderType);
		tpcMqMessageDto.setProducerGroup(this.producerGroup);
		return tpcMqMessageDto;
	}

}