package com.reliable.message.server.dao;

import com.reliable.message.server.domain.ServerMessageData;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface ServerMessageMapper {

    int insert(ServerMessageData message);

    ServerMessageData getByClientMessageId(String clientMessageId);

    int updateByMessageKey(ServerMessageData update);


    int updateById(ServerMessageData update);

    Long getConfirmIdByGroupAndKey(String consumerGroup, String messageKey);

    void confirmReceiveMessage(Long confirmId);
}
