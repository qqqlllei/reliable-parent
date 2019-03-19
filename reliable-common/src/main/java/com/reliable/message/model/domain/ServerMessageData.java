package com.reliable.message.model.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class ServerMessageData implements Serializable {
	private static final long serialVersionUID = -5951754367474682967L;
	private Long id;
	private Integer version;
	private String messageTopic;
	private String producerGroup;
	private Long producerMessageId;
	private String messageKey;
	private Integer status;
	private String messageBody;
	private Integer sendTimes;
	private Date createTime;
	private Date updateTime;
	private List<Integer> preStatusList;
}