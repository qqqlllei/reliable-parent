package com.reliable.message.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.common.domain.ReliableMessage;
import com.reliable.message.common.enums.ExceptionCodeEnum;
import com.reliable.message.common.enums.MessageSendStatusEnum;
import com.reliable.message.common.exception.BusinessException;
import com.reliable.message.common.netty.message.DirectSendRequest;
import com.reliable.message.common.netty.message.SaveAndSendRequest;
import com.reliable.message.common.netty.message.WaitingConfirmRequest;
import com.reliable.message.common.util.TimeUtil;
import com.reliable.message.common.util.UUIDUtil;
import com.reliable.message.server.dao.ServerMessageMapper;
import com.reliable.message.server.domain.MessageConfirm;
import com.reliable.message.server.enums.MessageConfirmEnum;
import com.reliable.message.server.service.MessageConfirmService;
import com.reliable.message.server.service.MessageConsumerService;
import com.reliable.message.server.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ServerMessageMapper serverMessageMapper;

    @Autowired
    private MessageConsumerService messageConsumerService;

    @Autowired
    private MessageConfirmService messageConfirmService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    private static final Integer FIRST_SEND_TIME_COUNT =1;
    private static final Integer DEFAULT_DEAD_STATUS =0;

    @Override
    public void saveMessageWaitingConfirm(WaitingConfirmRequest waitingConfirmRequest) {

        if (StringUtils.isEmpty(waitingConfirmRequest.getMessageTopic())) {
            throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_OF_MESSAGE_TOPIC_IS_NULL);
        }

        Date now = new Date();
        waitingConfirmRequest.setUpdateTime(now);
        waitingConfirmRequest.setCreateTime(now);
        serverMessageMapper.insert(waitingConfirmRequest);
    }

    @Override
    @Transactional
    public void confirmAndSendMessage(String clientMessageId) {
        ReliableMessage message = serverMessageMapper.getByMessageId(clientMessageId);
        if (message == null) {
            throw new BusinessException(ExceptionCodeEnum.GET_SERVER_MSG_IS_NULL_BY_CLIENT_ID);
        }
        List<MessageConfirm> confirmList = confirmAndSendMessage(message);
        if(confirmList.size() == 0) return;
        sendMessageToMessageQueue(confirmList,message);

    }

    @Override
    public void directSendMessage(String messageData, String topic, String key) {
        if(StringUtils.isBlank(key)){
            kafkaTemplate.send(topic, messageData);
        }else {
            kafkaTemplate.send(topic,key,messageData);
        }
    }

    @Override
    public void confirmReceiveMessage(String consumerGroup, String producerMessageId) {
        String confirmId = serverMessageMapper.getConfirmIdByGroupAndKey(consumerGroup, producerMessageId);
        // 3. 更新消费信息的状态
        serverMessageMapper.confirmReceiveMessage(confirmId);
    }

    @Override
    public void confirmFinishMessage(String confirmId) {
        messageConfirmService.confirmFinishMessage(confirmId);
    }

    @Override
    public ReliableMessage getServerMessageDataByProducerMessageId(String producerMessageId) {
        return serverMessageMapper.getByProducerMessageId(producerMessageId);
    }

    @Override
    public List<ReliableMessage> getServerMessageDataByParams(JSONObject jsonObject) {
        jsonObject.put("status", MessageSendStatusEnum.SENDING.sendStatus());
        jsonObject.put("clearTime", TimeUtil.getBeforeByMinuteTime(10));
        return serverMessageMapper.getServerMessageDataByParams(jsonObject);
    }

    @Override
    public void deleteServerMessageDataById(String id) {
        serverMessageMapper.deleteServerMessageDataById(id);
    }

    @Override
    public List<ReliableMessage> getWaitConfirmServerMessageData(JSONObject jobTaskParameter) {

        jobTaskParameter.put("scanTime",TimeUtil.getBeforeByMinuteTime(1));
        return serverMessageMapper.getWaitConfirmServerMessageData(jobTaskParameter);
    }

    @Override
    public List<ReliableMessage> getSendingMessageData(JSONObject jobTaskParameter) {
        jobTaskParameter.put("scanTime",TimeUtil.getBeforeByMinuteTime(1));
        return serverMessageMapper.getSendingMessageData(jobTaskParameter);
    }


    private List<MessageConfirm>  confirmAndSendMessage(ReliableMessage message){


        // 创建消费待确认列表
        List<MessageConfirm> confirmList =  createMqConfirmListByTopic(message.getMessageTopic(), message.getId(),message.getProducerGroup(), message.getProducerMessageId());
        message.setStatus(MessageSendStatusEnum.SENDING.sendStatus());
        message.setUpdateTime(new Date());
        message.setSendTime(TimeUtil.getAfterByMinuteTime(message.getDelayLevel()));
        serverMessageMapper.updateById(message);
        return confirmList;
    }

    private List<MessageConfirm> createMqConfirmListByTopic(String messageTopic, String messageId,String producerGroup, String producerMessageId) {
        List<MessageConfirm> list = new ArrayList<>();
        MessageConfirm messageConfirm;

        List<String> consumerGroupList = messageConsumerService.getConsumerGroupNameByTopic(messageTopic);

        if (consumerGroupList ==null || consumerGroupList.size() == 0) {
            return new ArrayList<>();
        }

        for (final String consumerCode : consumerGroupList) {
            messageConfirm = new MessageConfirm(UUIDUtil.getId(), messageId,producerGroup, producerMessageId,
                    consumerCode,FIRST_SEND_TIME_COUNT,DEFAULT_DEAD_STATUS, MessageConfirmEnum.NOT_COMFIRM.confirmFlag());
            Date currentTime = new Date();
            messageConfirm.setCreateTime(currentTime);
            messageConfirm.setUpdateTime(currentTime);
            list.add(messageConfirm);
        }

        messageConfirmService.batchCreateMqConfirm(list);
        return list;
    }


    public void sendMessageToMessageQueue(List<MessageConfirm> confirmList, final ReliableMessage message ){

        for (MessageConfirm confirm: confirmList) {

            String topic= message.getMessageTopic()+"_"+confirm.getConsumerGroup().toUpperCase();
            String messageVersion = message.getMessageVersion();
            if(StringUtils.isNotBlank(messageVersion)){
                topic = message.getMessageTopic()+"_"+messageVersion+"_"+confirm.getConsumerGroup().toUpperCase();
            }
            JSONObject messageBody =JSONObject.parseObject(JSONObject.toJSON(message).toString());
            messageBody.put("confirmId",confirm.getId());

            this.directSendMessage(messageBody.toJSONString(), topic, message.getMessageKey());
        }
    }

    @Override
    public void directSendMessage(DirectSendRequest directSendRequest) {
        String messageTopic = directSendRequest.getMessageTopic();
        List<String> consumerGroupList= messageConsumerService.getConsumerGroupNameByTopic(messageTopic);

        for (String consumer : consumerGroupList) {

            String topic= messageTopic+"_"+consumer.toUpperCase();

            String messageVersion = directSendRequest.getMessageVersion();
            if(StringUtils.isNotBlank(messageVersion)){
                topic = directSendRequest.getMessageTopic()+"_"+messageVersion+"_"+consumer.toUpperCase();
            }

            this.directSendMessage(JSONObject.toJSON(directSendRequest).toString(),topic,directSendRequest.getMessageKey());
        }
    }

    @Override
    @Transactional
    public void saveAndSendMessage(SaveAndSendRequest saveAndSendRequest) {

        Date now = new Date();
        saveAndSendRequest.setUpdateTime(now);
        saveAndSendRequest.setCreateTime(now);
        serverMessageMapper.insert(saveAndSendRequest);
        List<MessageConfirm> confirmList = createMqConfirmListByTopic(saveAndSendRequest.getMessageTopic(),saveAndSendRequest.getId(),saveAndSendRequest.getProducerGroup(),saveAndSendRequest.getProducerMessageId());
        sendMessageToMessageQueue(confirmList,saveAndSendRequest.requestToReliableMessage());
    }

    @Override
    @Transactional
    public void clearFinishMessage(String messageId) {
        deleteServerMessageDataById(messageId);

        messageConfirmService.deleteMessageConfirmByMessageId(messageId);
    }

    @Override
    @Transactional
    public void updateSendingMessage(ReliableMessage reliableMessage, MessageConfirm messageConfirm) {
        reliableMessage.setUpdateTime(new Date());
        updateById(reliableMessage);
        messageConfirmService.updateById(messageConfirm);
    }

    public void updateById(ReliableMessage reliableMessage) {
        this.serverMessageMapper.updateById(reliableMessage);
    }
}
