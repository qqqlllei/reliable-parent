package com.reliable.message.server.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class ServerMessageData implements Serializable {
	private static final long serialVersionUID = -5951754367474682967L;
	/**
	 * ID
	 */
	private Long id;

	/**
	 * 版本号
	 */
	private Integer version;

	/**
	 * topic
	 */
	private String messageTopic;

	/**
	 * 生产者PID
	 */
	private String producerGroup;

	private String producerMessageId;


	/**
	 *  目前是生产者messageId
     */
	private String messageKey;

	/**
	 * 消息状态
	 */
	private Integer status;

	/**
	 * 消息内容
	 */
	private String messageBody;

	/**
	 * 执行次数
	 */
	private Integer sendTimes;



	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;


	private List<Integer> preStatusList;
}