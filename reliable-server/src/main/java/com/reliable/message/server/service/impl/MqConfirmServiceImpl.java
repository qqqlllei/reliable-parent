package com.reliable.message.server.service.impl;

import com.reliable.message.server.dao.MqConfirmMapper;
import com.reliable.message.server.domain.TpcMqConfirm;
import com.reliable.message.server.service.MqConfirmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MqConfirmServiceImpl implements MqConfirmService{

    @Autowired
    private MqConfirmMapper mqConfirmMapper;

    @Override
    public void batchCreateMqConfirm(List<TpcMqConfirm> list) {
        mqConfirmMapper.batchCreateMqConfirm(list);
    }

    @Override
    public void confirmFinishMessage(String consumerGroup ,String producerMessageId) {
        mqConfirmMapper.confirmFinishMessage(consumerGroup,producerMessageId);
    }

    @Override
    public int getMessageConfirmCountByProducerMessageId(String producerMessageId) {
        return mqConfirmMapper.getMessageConfirmCountByProducerMessageId(producerMessageId);
    }
}
