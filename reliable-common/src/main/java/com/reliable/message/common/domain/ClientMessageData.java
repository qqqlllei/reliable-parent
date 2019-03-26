package com.reliable.message.common.domain;

import com.reliable.message.common.enums.DelayLevelEnum;
import com.reliable.message.common.util.TimeUtil;
import com.reliable.message.common.util.UUIDUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.Date;
import java.util.List;

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
	 * 消息版本
	 */

	private String messageVersion;

	/**
	 * 消息状态
	 */
	private Integer status;

	private Date sendTime;

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

	private List<String> consumerList;

	public ClientMessageData(){

	}
	public ClientMessageData(final String id,final String msgBody, final String topic, final String key) {
		this.id = id;
		this.messageBody = msgBody;
		this.messageTopic = topic;
		this.messageKey = key;
	}

	public void initParam(int delayLevel){

		Date currentDate = new Date();
		if(StringUtils.isBlank(this.getId())){
			this.setId(UUIDUtil.getId());
		}
		this.setCreatedTime(currentDate);
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