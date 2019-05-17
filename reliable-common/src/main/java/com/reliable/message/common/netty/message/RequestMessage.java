package com.reliable.message.common.netty.message;

import com.reliable.message.common.domain.ReliableMessage;
import lombok.Data;
import org.modelmapper.ModelMapper;

import java.util.Date;

/**
 * Created by 李雷 on 2019/4/30.
 */
@Data
public abstract class RequestMessage extends Message {

    private String id;
    private Integer version;
    private String messageTopic;
    private String producerGroup;
    private String producerMessageId;
    private String confirmId;
    private String messageKey;
    private Integer status;
    private String messageBody;
    private Integer messageType;
    private String messageVersion;
    private int delayLevel;
    private Date sendTime;
    private Date createTime;
    private Date updateTime;


    public ReliableMessage requestToReliableMessage(){
        return new ModelMapper().map(this, ReliableMessage.class);
    }
}
