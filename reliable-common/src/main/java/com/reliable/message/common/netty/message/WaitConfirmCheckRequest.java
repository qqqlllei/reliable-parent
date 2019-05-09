package com.reliable.message.common.netty.message;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by 李雷 on 2019/5/6.
 */
@Data
public class WaitConfirmCheckRequest extends RequestMessage{


    @Override
    public void executeSql(JdbcTemplate jdbcTemplate) {

    }
}
