package com.reliable.message.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class ServerMessageData implements Serializable {
	private static final long serialVersionUID = -5951754367474682967L;
	private String id;
	private Integer version;
	private String messageTopic;
	private String producerGroup;
	private String producerMessageId;
	private String messageKey;
	private Integer status;
	private String messageBody;
	private Date createTime;
	private Date updateTime;
	private List<Integer> preStatusList;
}