package com.reliable.message.server.job;

import com.alibaba.fastjson.JSONObject;
import com.job.lite.annotation.ElasticJobConfig;
import com.job.lite.job.AbstractBaseDataflowJob;
import com.reliable.message.model.domain.ServerMessageData;
import com.reliable.message.server.domain.MessageConfirm;
import com.reliable.message.server.service.MessageConfirmService;
import com.reliable.message.server.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by 李雷 on 2018/10/11.
 */
@ElasticJobConfig(cron = "elastic.job.cron.sendingMessageCron",
        jobParameter = "{'fetchNum':200,'taskType':'SENDING_MESSAGE'}",description="待发送消息异常处理")
public class SendingMessageJob extends AbstractBaseDataflowJob<ServerMessageData> {


    private Logger logger = LoggerFactory.getLogger(SendingMessageJob.class);

    private Integer MAX_RESEND_COUNT=5;

    private Integer SERVER_MESSAGE_DEAD_STATUS= 1;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageConfirmService messageConfirmService;

    @Override
    protected List<ServerMessageData> fetchJobData(JSONObject jobTaskParameter) {

        logger.info("SendingMessageJob.fetchJobData - jobTaskParameter={}", jobTaskParameter);

        List<ServerMessageData> serverMessageDataList = messageService.getSendingMessageData(jobTaskParameter);

        return serverMessageDataList;
    }

    @Override
    protected void processJobData(List<ServerMessageData> serverMessageDataList) {

        logger.info("SendingMessageJob.processJobData - serverMessageDataList={}", serverMessageDataList);

        for (ServerMessageData serverMessageData : serverMessageDataList) {
            List<MessageConfirm> messageConfirms = messageConfirmService.getMessageConfirmsByProducerMessageId(serverMessageData.getProducerMessageId());
            for (MessageConfirm messageConfirm : messageConfirms) {
                int sendTimes = messageConfirm.getSendTimes();
                if(sendTimes >= MAX_RESEND_COUNT ){
                    messageConfirm.setDead(SERVER_MESSAGE_DEAD_STATUS);
                }
                messageConfirm.setSendTimes(sendTimes+1);
                messageConfirm.setUpdateTime(new Date());
                messageConfirmService.updateById(messageConfirm);
                messageService.directSendMessage(serverMessageData,serverMessageData.getMessageTopic(),serverMessageData.getMessageKey());
            }
        }
    }
}
