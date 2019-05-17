package com.reliable.message.common.domain;

import com.reliable.message.common.enums.DelayLevelEnum;
import com.reliable.message.common.util.TimeUtil;
import com.reliable.message.common.util.UUIDUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class ReliableMessage implements Serializable {
	private static final long serialVersionUID = -5951754367474682967L;
	private String id;
	private Integer version;
	private String messageTopic;
	private String producerGroup;
	private String producerMessageId;
	private String messageKey;
	private Integer messageType;
	private Integer status;
	private String messageBody;
	private String messageVersion;
	private int delayLevel;
	private Date sendTime;
	private Date createTime;
	private Date updateTime;
	private List<Integer> preStatusList;




	public void initParam(int delayLevel){

		Date currentDate = new Date();

		if(StringUtils.isBlank(this.getId())){
			this.setId(UUIDUtil.getId());
		}

		this.setCreateTime(currentDate);
		this.setUpdateTime(currentDate);
		this.setSendTime(currentDate);
		this.setProducerMessageId(this.getId());

		if (delayLevel != DelayLevelEnum.ZERO.delayLevel()) {
			this.setDelayLevel(delayLevel);
			this.setSendTime(TimeUtil.getAfterByMinuteTime(delayLevel));
		}

		String messageKey = this.getMessageKey();
		if(StringUtils.isBlank(messageKey)){
			this.setMessageKey(this.getId());
		}
	}
}