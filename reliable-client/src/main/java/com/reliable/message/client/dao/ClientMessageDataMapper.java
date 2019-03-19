package com.reliable.message.client.dao;


import com.alibaba.fastjson.JSONObject;
import com.reliable.message.model.domain.ClientMessageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClientMessageDataMapper {

    int insert(ClientMessageData mqMessageData);

    ClientMessageData getClientMessageByProducerMessageIdAndType(@Param("producerMessageId") Long producerMessageId,@Param("messageType") int type);

    void deleteMessageByProducerMessageId(Long producerMessageId);

    List<ClientMessageData> getClientMessageByParams(JSONObject jobTaskParameter);

    ClientMessageData getClientMessageDataByProducerMessageId(String producerMessageId);
}