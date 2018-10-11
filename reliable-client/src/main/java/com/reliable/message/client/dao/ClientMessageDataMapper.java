package com.reliable.message.client.dao;


import com.alibaba.fastjson.JSONObject;
import com.reliable.message.model.domain.ClientMessageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClientMessageDataMapper {

    int insert(ClientMessageData mqMessageData);

    ClientMessageData getClientMessageByMessageIdAndType(@Param("messageId") Long messageId,@Param("messageType") int type);

    void deleteMessageByProducerMessageId(String producerMessageId);

    List<ClientMessageData> getClientMessageByParams(JSONObject jobTaskParameter);

    ClientMessageData getClientMessageDataByProducerMessageId(String producerMessageId);
}