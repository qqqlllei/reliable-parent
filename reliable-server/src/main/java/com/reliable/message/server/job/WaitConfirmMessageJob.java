package com.reliable.message.server.job;

import com.alibaba.fastjson.JSONObject;
import com.job.lite.annotation.ElasticJobConfig;
import com.job.lite.job.AbstractBaseDataflowJob;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.domain.ServerMessageData;
import com.reliable.message.server.feign.ClientMessageAdapter;
import com.reliable.message.server.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李雷 on 2018/10/11.
 */
@Component
@ElasticJobConfig(cron = "elastic.job.cron.waitConfirmMessageJobCron",
        jobParameter = "{'fetchNum':50,'taskType':'SENDING_MESSAGE'}",description="待确认消息异常处理")
public class WaitConfirmMessageJob extends AbstractBaseDataflowJob<ServerMessageData> {


    private Logger logger = LoggerFactory.getLogger(WaitConfirmMessageJob.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private ClientMessageAdapter clientMessageAdapter;


    @Override
    protected List<ServerMessageData> fetchJobData(JSONObject jobTaskParameter) {

        logger.info("WaitConfirmMessageJob.fetchJobData - jobTaskParameter={}", jobTaskParameter);

        List<ServerMessageData> serverMessageDataList =  messageService.getWaitConfirmServerMessageData(jobTaskParameter);

        List<ServerMessageData> fetchServerMessageList = new ArrayList<>();
        for (ServerMessageData serverMessageData : serverMessageDataList) {
            serverMessageData.getProducerMessageId();
            try {
                // 可查询服务确认
                ClientMessageData clientMessageData = clientMessageAdapter.getClientMessageDataByProducerMessageId(
                        serverMessageData.getProducerGroup(),serverMessageData.getProducerMessageId());
                if(clientMessageData!=null){
                    fetchServerMessageList.add(serverMessageData);
                }else{
                    messageService.deleteServerMessageDataById(serverMessageData.getId());
                }
            } catch (URISyntaxException e) {
                logger.error(e.getMessage());
            }
        }


        return fetchServerMessageList;
    }

    @Override
    protected void processJobData(List<ServerMessageData> serverMessageDataList) {
        logger.info("WaitConfirmMessageJob.processJobData - serverMessageDataList={}", serverMessageDataList);

        for (ServerMessageData serverMessageData : serverMessageDataList) {
            messageService.confirmAndSendMessage(serverMessageData.getProducerMessageId());
        }
    }
}
