package com.reliable.message.server.service.impl;

import com.reliable.message.server.dao.MessageConfirmMapper;
import com.reliable.message.server.domain.MessageConfirm;
import com.reliable.message.server.service.MessageConfirmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MessageConfirmServiceImpl implements MessageConfirmService {

    @Autowired
    private MessageConfirmMapper messageConfirmMapper;

    @Override
    public void batchCreateMqConfirm(List<MessageConfirm> list) {
        messageConfirmMapper.batchCreateMqConfirm(list);
    }

    @Override
    public void confirmFinishMessage(String consumerGroup ,String producerMessageId) {
        messageConfirmMapper.confirmFinishMessage(consumerGroup,producerMessageId);
    }

    @Override
    public int getMessageConfirmCountByProducerMessageId(String producerMessageId) {
        return messageConfirmMapper.getMessageConfirmCountByProducerMessageId(producerMessageId);
    }

    @Override
    public List<MessageConfirm> getMessageConfirmsByProducerMessageId(String producerMessageId) {
        return messageConfirmMapper.getMessageConfirmsByProducerMessageId(producerMessageId);
    }
}
