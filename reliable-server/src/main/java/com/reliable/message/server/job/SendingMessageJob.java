package com.reliable.message.server.job;

import com.alibaba.fastjson.JSONObject;
import com.job.lite.annotation.ElasticJobConfig;
import com.job.lite.job.AbstractBaseDataflowJob;
import com.reliable.message.common.domain.ReliableMessage;
import com.reliable.message.server.domain.MessageConfirm;
import com.reliable.message.server.service.MessageConfirmService;
import com.reliable.message.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by 李雷 on 2018/10/11.
 */
@Slf4j
@ElasticJobConfig(cron = "elastic.job.cron.sendingMessageCron",
        jobParameter = "{'fetchNum':300,'taskType':'SENDING_MESSAGE'}",description="待发送消息异常处理")
public class SendingMessageJob extends AbstractBaseDataflowJob<MessageConfirm> {



    private Integer MAX_RESEND_COUNT = 4;

    private Integer SERVER_MESSAGE_DEAD_STATUS = 1;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageConfirmService messageConfirmService;

    @Override
    protected List<MessageConfirm> fetchJobData(JSONObject jobTaskParameter) {

        log.info("SendingMessageJob.fetchJobData - jobTaskParameter={}", jobTaskParameter);

        List<MessageConfirm> unConfirmMessages =  messageConfirmService.getUnConfirmMessage(jobTaskParameter);

        return unConfirmMessages;
    }

    @Override
    protected void processJobData(List<MessageConfirm> messageConfirms) {

        log.info("SendingMessageJob.processJobData - messageConfirms={}", messageConfirms);



        for (MessageConfirm messageConfirm : messageConfirms) {
            int sendTimes = messageConfirm.getSendTimes();
            if(sendTimes >= MAX_RESEND_COUNT ){
                messageConfirm.setDead(SERVER_MESSAGE_DEAD_STATUS);
            }
            messageConfirm.setSendTimes(sendTimes+1);
            messageConfirm.setUpdateTime(new Date());
            ReliableMessage reliableMessage = messageService.getServerMessageDataByProducerMessageId(messageConfirm.getProducerMessageId());

            messageConfirmService.updateById(messageConfirm);

            String topic= reliableMessage.getMessageTopic()+"_"+messageConfirm.getConsumerGroup().toUpperCase();

            String messageVersion = reliableMessage.getMessageVersion();
            if(StringUtils.isNotBlank(messageVersion)){
                topic = reliableMessage.getMessageTopic()+"_"+messageVersion+"_"+messageConfirm.getConsumerGroup().toUpperCase();
            }
            JSONObject messageBody =JSONObject.parseObject(JSONObject.toJSON(reliableMessage).toString());
            messageBody.put("confirmId",messageConfirm.getId());
            messageService.directSendMessage(messageBody.toJSONString(),topic, reliableMessage.getMessageKey());
        }
    }
}
