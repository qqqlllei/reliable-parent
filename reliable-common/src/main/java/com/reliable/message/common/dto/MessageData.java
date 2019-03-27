package com.reliable.message.common.dto;

import lombok.Data;

import java.util.Date;

/**
 * Created by 李雷 on 2019/3/27.
 */
@Data
public class MessageData {


    private String id;
    private Integer version;
    private String messageTopic;
    private String producerGroup;
    private String producerMessageId;
    private String confirmId;
    private String messageKey;
    private Integer status;
    private String messageBody;
    private String messageVersion;
    private int delayLevel;
    private Date sendTime;
    private Date createTime;
    private Date updateTime;
}
