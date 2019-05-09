package com.reliable.message.client.service;


import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.dto.MessageData;
import com.reliable.message.common.netty.message.RequestMessage;

import java.util.List;
/**
 * Created by 李雷
 */
public interface ReliableMessageService {
    void confirmReceiveMessage(String consumerGroup, MessageData dto);
    boolean hasConsumedMessage(String producerMessageId,int type);
    boolean hasProducedMessage(String producerMessageId);
	void deleteMessageByProducerMessageId(String producerMessageId);
	List<String> getProducerMessage(JSONObject jobTaskParameter);
    void saveMessage(RequestMessage waitingConfirmRequest);

}
