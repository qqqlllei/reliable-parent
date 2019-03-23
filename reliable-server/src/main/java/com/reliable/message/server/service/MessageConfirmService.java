package com.reliable.message.server.service;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.server.domain.MessageConfirm;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MessageConfirmService {
    void batchCreateMqConfirm(List<MessageConfirm> list);

    void confirmFinishMessage(String consumerGroup ,String producerMessageId);

    int getMessageConfirmCountByProducerMessageId(String producerMessageId);

    List<MessageConfirm> getMessageConfirmsByProducerMessageId(String producerMessageId);

    void updateById(MessageConfirm messageConfirm);

    void deleteMessageConfirmByMessageId(String messageId);

    List<MessageConfirm> getUnConfirmMessage(JSONObject jobTaskParameter);
}
