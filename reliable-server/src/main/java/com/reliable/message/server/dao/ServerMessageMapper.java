package com.reliable.message.server.dao;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ReliableMessage;
import com.reliable.message.common.netty.message.RequestMessage;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface ServerMessageMapper {

    int insert(ReliableMessage message);

    int insert(RequestMessage requestMessage);

    ReliableMessage getByProducerMessageId(String clientMessageId);

    ReliableMessage getByMessageId(String id);

    int updateById(ReliableMessage update);

    String getConfirmIdByGroupAndKey(String consumerGroup, String messageKey);

    void confirmReceiveMessage(String confirmId);

    List<ReliableMessage> getServerMessageDataByParams(JSONObject jsonObject);

    void deleteServerMessageDataById(String id);

    List<ReliableMessage> getWaitConfirmServerMessageData(JSONObject jobTaskParameter);

    List<ReliableMessage> getSendingMessageData(JSONObject jobTaskParameter);


}
