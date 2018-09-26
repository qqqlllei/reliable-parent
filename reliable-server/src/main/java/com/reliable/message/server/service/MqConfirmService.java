package com.reliable.message.server.service;

import com.reliable.message.server.domain.TpcMqConfirm;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MqConfirmService {
    void batchCreateMqConfirm(List<TpcMqConfirm> list);

    void confirmFinishMessage(String consumerGroup ,String producerMessageId);
}
