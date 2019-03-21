package com.reliable.message.server.domain;

import lombok.Data;

import java.util.Date;


@Data
public class MessageConfirm {
	private String id;
	private Integer version;
	private String messageId;
	private String consumerGroup;
	private Integer consumeCount;
	private String producerGroup;
	private String producerMessageId;
	/**
	 * 状态, 10 - 未确认 ; 20 - 已确认; 30 已消费
	 */
	private Integer status;
	private Integer sendTimes;
	/**
	 * 是否死亡 0 - 活着; 1-死亡
	 */
	private Integer dead;
	private Date createTime;
	private Date updateTime;
	private Integer confirmFlag; // 0 未消费，1.已消费

	public MessageConfirm(){

	}

	public MessageConfirm(final String id, final String messageId,final String producerGroup,
						  final String producerMessageId,
						  final String consumerGroup,
						  final Integer sendTimes,
						  final Integer dead,Integer confirmFlag) {
		this.id = id;
		this.messageId = messageId;
		this.producerGroup = producerGroup;
		this.producerMessageId = producerMessageId;
		this.consumerGroup = consumerGroup;
		this.sendTimes=sendTimes;
		this.dead = dead;
		this.confirmFlag = confirmFlag;
	}
}