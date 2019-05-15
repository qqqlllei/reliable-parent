package com.reliable.message.common.netty.message;

import com.reliable.message.common.netty.rpc.AbstractRpcHandler;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

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

    public abstract void executeSql(JdbcTemplate jdbcTemplate);
}
