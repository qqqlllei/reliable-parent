package com.reliable.message.server.dao;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.server.domain.ServerMessageData;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface ServerMessageMapper {

    int insert(ServerMessageData message);

    ServerMessageData getByProducerMessageId(String clientMessageId);

    int updateById(ServerMessageData update);

    Long getConfirmIdByGroupAndKey(String consumerGroup, String messageKey);

    void confirmReceiveMessage(Long confirmId);

    List<ServerMessageData> getServerMessageDataByParams(JSONObject jsonObject);

    void deleteServerMessageDataById(Long id);

    List<ServerMessageData> getWaitConfirmServerMessageData(JSONObject jobTaskParameter);

    List<ServerMessageData> getSendingMessageData(JSONObject jobTaskParameter);
}
