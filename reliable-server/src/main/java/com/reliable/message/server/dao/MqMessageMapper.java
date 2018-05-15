package com.reliable.message.server.dao;

import com.reliable.message.server.domain.TpcMqMessage;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MqMessageMapper {

    int insert(TpcMqMessage message);

    TpcMqMessage getByMessageKey(String messageKey);

    int updateByMessageKey(TpcMqMessage update);


    int updateById(TpcMqMessage update);
}
