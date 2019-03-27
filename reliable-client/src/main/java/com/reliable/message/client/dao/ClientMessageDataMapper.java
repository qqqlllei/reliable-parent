package com.reliable.message.client.dao;


import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ClientMessageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * Created by 李雷
 */
public interface ClientMessageDataMapper {

    int insert(ClientMessageData mqMessageData);

    ClientMessageData getClientMessageByProducerMessageIdAndType(@Param("producerMessageId") String producerMessageId,@Param("messageType") int type);

    void deleteMessageByProducerMessageId(String producerMessageId);

    List<ClientMessageData> getClientMessageByParams(JSONObject jobTaskParameter);

    ClientMessageData getClientMessageDataByProducerMessageId(String producerMessageId);
}