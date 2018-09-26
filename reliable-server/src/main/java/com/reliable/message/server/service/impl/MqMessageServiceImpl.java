package com.reliable.message.server.service.impl;

import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.server.dao.ServerMessageMapper;
import com.reliable.message.server.domain.ServerMessageData;
import com.reliable.message.server.domain.TpcMqConfirm;
import com.reliable.message.server.enums.MqSendStatusEnum;
import com.reliable.message.server.service.MqConfirmService;
import com.reliable.message.server.service.MqConsumerService;
import com.reliable.message.server.service.MqMessageService;
import com.reliable.message.server.util.UniqueId;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MqMessageServiceImpl implements MqMessageService {

    @Autowired
    private ServerMessageMapper serverMessageMapper;

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
    public void saveMessageWaitingConfirm(ClientMessageData clientMessageData) {

        if (StringUtils.isEmpty(clientMessageData.getMessageTopic())) {
//            throw new TpcBizException(ErrorCodeEnum.TPC10050001);
        }

        Date now = new Date();
        ServerMessageData message = new ModelMapper().map(clientMessageData, ServerMessageData.class);
        message.setStatus(MqSendStatusEnum.WAIT_SEND.sendStatus());
        message.setProducerMessageId(clientMessageData.getProducerGroup()+"-"+clientMessageData.getId());
        message.setId(uniqueId.getNextIdByApplicationName(ServerMessageData.class.getSimpleName()));
        message.setUpdateTime(now);
        message.setCreateTime(now);
        serverMessageMapper.insert(message);
    }

    @Override
    public void confirmAndSendMessage(String clientMessageId) {
        final ServerMessageData message = serverMessageMapper.getByClientMessageId(clientMessageId);
        if (message == null) {
//            throw new TpcBizException(ErrorCodeEnum.TPC10050002);
        }

        ServerMessageData update = new ServerMessageData();
        update.setStatus(MqSendStatusEnum.SENDING.sendStatus());
        update.setId(message.getId());
        update.setUpdateTime(new Date());
        serverMessageMapper.updateById(update);


        // 创建消费待确认列表
        List<TpcMqConfirm> confirmList =  this.createMqConfirmListByTopic(message.getMessageTopic(), message.getId(), clientMessageId);

        for (TpcMqConfirm confirm: confirmList) {
            this.directSendMessage(message, message.getMessageTopic()+"-"+confirm.getConsumerGroup(), message.getMessageKey());
        }

    }

    @Override
    public void directSendMessage(ServerMessageData messageData, String topic, String key) {
        if(StringUtils.isBlank(key)){
            kafkaTemplate.send(topic,messageData);
        }else {
            kafkaTemplate.send(topic,key,messageData);
        }
    }

    @Override
    public void confirmReceiveMessage(String consumerGroup, String producerMessageId) {
        Long confirmId = serverMessageMapper.getConfirmIdByGroupAndKey(consumerGroup, producerMessageId);
        // 3. 更新消费信息的状态
        serverMessageMapper.confirmReceiveMessage(confirmId);
    }

    @Override
    public void confirmFinishMessage(String consumerGroup, String producerMessageId) {
        mqConfirmService.confirmFinishMessage(consumerGroup,producerMessageId);
    }

    private List<TpcMqConfirm> createMqConfirmListByTopic(String messageTopic, Long messageId, String messageKey) {
        List<TpcMqConfirm> list = new ArrayList<TpcMqConfirm>();
        TpcMqConfirm tpcMqConfirm;
        List<String> consumerGroupList = mqConsumerService.listConsumerGroupByTopic(messageTopic);
        if (consumerGroupList ==null || consumerGroupList.size() == 0) {
//            throw new TpcBizException(ErrorCodeEnum.TPC100500010, topic);
        }
        for (final String consumerCode : consumerGroupList) {
            tpcMqConfirm = new TpcMqConfirm(uniqueId.getNextIdByApplicationName(TpcMqConfirm.class.getSimpleName()), messageId, messageKey, consumerCode);
            list.add(tpcMqConfirm);
        }

        mqConfirmService.batchCreateMqConfirm(list);
        return list;
    }
}
