package com.reliable.message.client.server.dao;

import com.reliable.message.client.server.domain.TpcMqMessage;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MqMessageMapper {

    int insert(TpcMqMessage message);
}
