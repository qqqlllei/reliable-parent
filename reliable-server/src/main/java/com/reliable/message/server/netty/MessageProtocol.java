package com.reliable.message.server.netty;

/**
 * Created by 李雷 on 2019/5/5.
 */
public interface MessageProtocol {


    String getClientMessageDataByProducerMessageId(String consumerGroup,String producerMessageId) throws Exception;
}
