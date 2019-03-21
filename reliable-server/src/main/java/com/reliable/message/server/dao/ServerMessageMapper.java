package com.reliable.message.server.dao;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ServerMessageData;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface ServerMessageMapper {

    int insert(ServerMessageData message);

    ServerMessageData getByProducerMessageId(String clientMessageId);

    int updateById(ServerMessageData update);

    String getConfirmIdByGroupAndKey(String consumerGroup, String messageKey);

    void confirmReceiveMessage(String confirmId);

    List<ServerMessageData> getServerMessageDataByParams(JSONObject jsonObject);

    void deleteServerMessageDataById(String id);

    List<ServerMessageData> getWaitConfirmServerMessageData(JSONObject jobTaskParameter);

    List<ServerMessageData> getSendingMessageData(JSONObject jobTaskParameter);
}
