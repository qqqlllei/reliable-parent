package com.reliable.message.client.job;

import com.alibaba.fastjson.JSONObject;
import com.job.lite.annotation.ElasticJobConfig;
import com.job.lite.job.AbstractBaseDataflowJob;
import com.reliable.message.client.service.MqMessageService;
import com.reliable.message.model.domain.ClientMessageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@ElasticJobConfig(cron = "0/50 * * * * ? ", jobParameter = "{'fetchNum':'200','taskType':'SENDING_MESSAGE'}",description="生产者消息清理")
public class ClientMessageDataflow extends AbstractBaseDataflowJob<ClientMessageData> {

    Logger logger = LoggerFactory.getLogger(ClientMessageDataflow.class);

    @Autowired
    private MqMessageService mqMessageService;


    @Override
    protected List<ClientMessageData> fetchJobData(final JSONObject jobTaskParameter) {
        logger.info("fetchJobData - jobTaskParameter={}", jobTaskParameter);
        List<ClientMessageData> clientMessageData = mqMessageService.getProducerMessage(jobTaskParameter);
        return clientMessageData;
    }

    @Override
    protected void processJobData(final List<ClientMessageData> taskList) {
        logger.info("processJobData - taskList={}", taskList);

        //查询服务端消息状态，该消息是否已经消费成功（从消息服务段查询不到该消息）

        //主动清清除

    }
}