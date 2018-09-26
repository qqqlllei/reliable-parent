package com.reliable.message.server.domain;

import lombok.Data;

import java.util.Date;


@Data
public class TpcMqConfirm {
	/**
	 * ID
	 */
	private Long id;

	/**
	 * 版本号
	 */
	private Integer version;

	/**
	 * 任务ID
	 */
	private Long messageId;

	/**
	 * 消息唯一标识
	 */
	private String messageKey;

	/**
	 * 消费者组编码
	 */
	private String consumerGroup;

	/**
	 * 消费的数次
	 */
	private Integer consumeCount;

	/**
	 * 状态, 10 - 未确认 ; 20 - 已确认; 30 已消费
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private Date createdTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * Instantiates a new Tpc mq confirm.
	 *
	 * @param id           the id
	 * @param messageId    the message id
	 * @param messageKey   the message key
	 * @param consumerGroup the consumer code
	 */
	public TpcMqConfirm(final Long id, final Long messageId, final String messageKey, final String consumerGroup) {
		this.id = id;
		this.messageId = messageId;
		this.messageKey = messageKey;
		this.consumerGroup = consumerGroup;
	}
}