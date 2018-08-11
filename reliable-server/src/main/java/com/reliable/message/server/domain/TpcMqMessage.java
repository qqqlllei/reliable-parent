package com.reliable.message.server.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class TpcMqMessage implements Serializable {
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
	 * 是否死亡 0 - 活着; 1-死亡
	 */
	private Integer dead;

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