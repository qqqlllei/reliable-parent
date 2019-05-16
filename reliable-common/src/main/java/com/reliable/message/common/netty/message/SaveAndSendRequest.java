package com.reliable.message.common.netty.message;
import com.reliable.message.common.enums.MessageSendStatusEnum;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by 李雷 on 2019/5/15.
 */
public class SaveAndSendRequest extends RequestMessage{
    @Override
    public void executeSql(JdbcTemplate jdbcTemplate) {
        String sql = "INSERT INTO client_message_data " +
                "(id ,version ,producer_message_id,producer_group,message_key,message_topic,message_type,message_body,message_version,delay_level,status,send_time,created_time,update_time) VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        Object args[] = {
                getId(),
                0,
                getProducerMessageId(),
                getProducerGroup(),
                getMessageKey(),
                getMessageTopic(),
                getMessageType(),
                getMessageBody(),
                getMessageVersion(),
                getDelayLevel(),
                getStatus(),
                getSendTime(),
                getCreateTime(),
                getUpdateTime()
        };
        jdbcTemplate.update(sql,args);
    }


    @Override
    public Integer getStatus() {
        return MessageSendStatusEnum.SENDING.sendStatus();
    }
}
