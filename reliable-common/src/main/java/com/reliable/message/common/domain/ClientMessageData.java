package com.reliable.message.common.domain;

import lombok.Data;

import java.util.Date;

@Data
public class ClientMessageData {

	private String id;

	/**
	 * 版本号
	 */
	private Integer version;

	private String producerMessageId;

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
	 * producer group name
	 */
	private String producerGroup;

	public ClientMessageData(){

	}
	public ClientMessageData(final String id,final String msgBody, final String topic, final String key) {
		this.id = id;
		this.messageBody = msgBody;
		this.messageTopic = topic;
		this.messageKey = key;
	}

}