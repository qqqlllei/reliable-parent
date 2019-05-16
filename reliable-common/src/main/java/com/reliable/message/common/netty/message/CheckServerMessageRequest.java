package com.reliable.message.common.netty.message;

import com.reliable.message.common.enums.MessageSendStatusEnum;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by 李雷 on 2019/5/16.
 */
public class CheckServerMessageRequest extends RequestMessage {

    @Override
    public void executeSql(JdbcTemplate jdbcTemplate) {
        String sql = "UPDATE client_message_data SET status = ? WHERE id = ?";
        Object args[] = {MessageSendStatusEnum.FINISH.sendStatus(),getId()};
        jdbcTemplate.update(sql,args);
    }
}
