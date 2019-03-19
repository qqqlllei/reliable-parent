package com.reliable.message.server.domain;

import lombok.Data;

import java.util.Date;


@Data
public class MessageConfirm {
	private Long id;
	private Integer version;
	private Long messageId;
	private String consumerGroup;
	private Integer consumeCount;
	private String producerGroup;
	private Long producerMessageId;
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

	public MessageConfirm(final Long id, final Long messageId,final String producerGroup,
						  final Long producerMessageId,
						  final String consumerGroup,
						  final Integer sendTimes,
						  final Integer dead) {
		this.id = id;
		this.messageId = messageId;
		this.producerGroup = producerGroup;
		this.producerMessageId = producerMessageId;
		this.consumerGroup = consumerGroup;
		this.sendTimes=sendTimes;
		this.dead = dead;
	}
}