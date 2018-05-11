package com.reliable.message.client.server.service;

import com.reliable.message.model.dto.TpcMqMessageDto;

/**
 * Created by 李雷 on 2018/5/11.
 */
public interface MqMessageService {
    void saveMessageWaitingConfirm(TpcMqMessageDto tpcMqMessageDto);
}
