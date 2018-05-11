package com.reliable.message.client.server.service.impl;

import com.reliable.message.client.server.dao.MqMessageMapper;
import com.reliable.message.client.server.domain.TpcMqMessage;
import com.reliable.message.client.server.enums.MqSendStatusEnum;
import com.reliable.message.client.server.service.MqMessageService;
import com.reliable.message.model.dto.TpcMqMessageDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MqMessageServiceImpl implements MqMessageService {

    @Autowired
    private MqMessageMapper mqMessageMapper;

    @Override
    public void saveMessageWaitingConfirm(TpcMqMessageDto tpcMqMessageDto) {

        if (StringUtils.isEmpty(tpcMqMessageDto.getMessageTopic())) {
//            throw new TpcBizException(ErrorCodeEnum.TPC10050001);
        }

        Date now = new Date();
        TpcMqMessage message = new ModelMapper().map(tpcMqMessageDto, TpcMqMessage.class);
        message.setMessageStatus(MqSendStatusEnum.WAIT_SEND.sendStatus());
        message.setUpdateTime(now);
        message.setCreatedTime(now);
        mqMessageMapper.insert(message);
    }
}
