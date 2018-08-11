package com.reliable.message.server.service.impl;

import com.reliable.message.model.dto.TpcMqMessageDto;
import com.reliable.message.server.dao.MqMessageMapper;
import com.reliable.message.server.domain.TpcMqConfirm;
import com.reliable.message.server.domain.TpcMqMessage;
import com.reliable.message.server.enums.MqSendStatusEnum;
import com.reliable.message.server.service.MqConfirmService;
import com.reliable.message.server.service.MqConsumerService;
import com.reliable.message.server.service.MqMessageService;
import com.reliable.message.server.util.UniqueId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MqMessageServiceImpl implements MqMessageService {

    @Autowired
    private MqMessageMapper mqMessageMapper;


    @Autowired
    private MqConsumerService mqConsumerService;

    @Autowired
    private MqConfirmService mqConfirmService;

    @Autowired
    private UniqueId uniqueId;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    private static final String MQ_CONFIRM_TABLE ="MQ_CONFIRM_TABLE";

    @Override
    public void saveMessageWaitingConfirm(TpcMqMessageDto tpcMqMessageDto) {

        if (StringUtils.isEmpty(tpcMqMessageDto.getMessageTopic())) {
//            throw new TpcBizException(ErrorCodeEnum.TPC10050001);
        }

        Date now = new Date();
        TpcMqMessage message = new ModelMapper().map(tpcMqMessageDto, TpcMqMessage.class);
        message.setStatus(MqSendStatusEnum.WAIT_SEND.sendStatus());
        message.setUpdateTime(now);
        message.setCreatedTime(now);
        mqMessageMapper.insert(message);
    }

    @Override
    public void confirmAndSendMessage(String messageKey) {
        final TpcMqMessage message = mqMessageMapper.getByMessageKey(messageKey);
        if (message == null) {
//            throw new TpcBizException(ErrorCodeEnum.TPC10050002);
        }

        TpcMqMessage update = new TpcMqMessage();
        update.setStatus(MqSendStatusEnum.SENDING.sendStatus());
        update.setId(message.getId());
        update.setUpdateTime(new Date());
        mqMessageMapper.updateById(update);


        // 创建消费待确认列表
        this.createMqConfirmListByTopic(message.getMessageTopic(), message.getId(), message.getMessageKey());
        // 直接发送消息
        this.directSendMessage(message.getMessageBody(), message.getMessageTopic(), message.getMessageKey(), message.getProducerGroup(), message.getDelayLevel());
    }

    @Override
    public void directSendMessage(String body, String topic, String key, String producerGroup, Integer delayLevel) {
        // TODO kafka 直接发送topic 消息
    }

    @Override
    public void confirmReceiveMessage(String consumerGroup, String messageKey) {
        Long confirmId = mqMessageMapper.getConfirmIdByGroupAndKey(consumerGroup, messageKey);
        // 3. 更新消费信息的状态
        mqMessageMapper.confirmReceiveMessage(confirmId);
    }

    @Override
    public void confirmConsumedMessage(String consumerGroup, String messageKey) {
        Long confirmId = mqMessageMapper.getConfirmIdByGroupAndKey(consumerGroup, messageKey);
        mqConfirmService.confirmConsumedMessage(confirmId);
    }

    private void createMqConfirmListByTopic(String messageTopic, Long messageId, String messageKey) {
        List<TpcMqConfirm> list = new ArrayList<TpcMqConfirm>();
        TpcMqConfirm tpcMqConfirm;
        List<String> consumerGroupList = mqConsumerService.listConsumerGroupByTopic(messageTopic);
        if (consumerGroupList ==null || consumerGroupList.size() == 0) {
//            throw new TpcBizException(ErrorCodeEnum.TPC100500010, topic);
        }
        for (final String consumerCode : consumerGroupList) {
            tpcMqConfirm = new TpcMqConfirm(uniqueId.getNextIdByApplicationName(MQ_CONFIRM_TABLE), messageId, messageKey, consumerCode);
            list.add(tpcMqConfirm);
        }

        mqConfirmService.batchCreateMqConfirm(list);
    }
}
