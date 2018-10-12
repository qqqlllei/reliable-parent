package com.reliable.message.server.job;

import com.alibaba.fastjson.JSONObject;
import com.job.lite.annotation.ElasticJobConfig;
import com.job.lite.job.AbstractBaseDataflowJob;
import com.netflix.discovery.converters.Auto;
import com.reliable.message.server.domain.ServerMessageData;
import com.reliable.message.server.domain.TpcMqConfirm;
import com.reliable.message.server.service.MqConfirmService;
import com.reliable.message.server.service.MqMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by 李雷 on 2018/10/11.
 */
@ElasticJobConfig(cron = "elastic.job.cron.sendingMessageCron",
        jobParameter = "{'fetchNum':200,'taskType':'SENDING_MESSAGE'}",description="待发送消息异常处理")
public class SendingMessageJob extends AbstractBaseDataflowJob<ServerMessageData> {


    private Logger logger = LoggerFactory.getLogger(SendingMessageJob.class);

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MqConfirmService mqConfirmService;

    @Override
    protected List<ServerMessageData> fetchJobData(JSONObject jobTaskParameter) {

        logger.info("SendingMessageJob.fetchJobData - jobTaskParameter={}", jobTaskParameter);

        List<ServerMessageData> serverMessageDataList = mqMessageService.getSendingMessageData(jobTaskParameter);

        return serverMessageDataList;
    }

    @Override
    protected void processJobData(List<ServerMessageData> serverMessageDataList) {

        logger.info("SendingMessageJob.processJobData - serverMessageDataList={}", serverMessageDataList);

        for (ServerMessageData serverMessageData : serverMessageDataList) {
            List<TpcMqConfirm> messageConfirms = mqConfirmService.getMessageConfirmsByProducerMessageId(serverMessageData.getProducerMessageId());

            for (TpcMqConfirm messageConfirm:messageConfirms ) {
                
            }
        }

    }
}
