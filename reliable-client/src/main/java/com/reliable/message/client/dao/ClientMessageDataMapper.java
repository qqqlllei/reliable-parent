package com.reliable.message.client.dao;


import com.reliable.message.model.domain.ClientMessageData;
import org.apache.ibatis.annotations.Param;

public interface ClientMessageDataMapper {

    int insert(ClientMessageData mqMessageData);

    ClientMessageData getClientMessageByMessageIdAndType(@Param("messageId") Long messageId,@Param("messageType") int type);

    void deleteMessageByProducerMessageId(String producerMessageId);
}