package com.reliable.message.server.job;

import com.alibaba.fastjson.JSONObject;
import com.job.lite.annotation.ElasticJobConfig;
import com.job.lite.job.AbstractBaseDataflowJob;
import com.reliable.message.common.domain.ServerMessageData;
import com.reliable.message.common.netty.message.ResponseMessage;
import com.reliable.message.common.netty.message.WaitConfirmCheckRequest;
import com.reliable.message.common.wrapper.Wrapper;
import com.reliable.message.server.netty.NettyServer;
import com.reliable.message.server.netty.ServerRpcHandler;
import com.reliable.message.server.service.MessageService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李雷 on 2018/10/11.
 */
@Component
@ElasticJobConfig(cron = "elastic.job.cron.waitConfirmMessageJobCron",
        jobParameter = "{'fetchNum':500,'taskType':'SENDING_MESSAGE'}",description="待确认消息异常处理")
public class WaitConfirmMessageJob extends AbstractBaseDataflowJob<ServerMessageData> {


    private Logger logger = LoggerFactory.getLogger(WaitConfirmMessageJob.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private NettyServer nettyServer;


    @Override
    protected List<ServerMessageData> fetchJobData(JSONObject jobTaskParameter) {

        logger.info("WaitConfirmMessageJob.fetchJobData - jobTaskParameter={}", jobTaskParameter);

        List<ServerMessageData> serverMessageDataList =  messageService.getWaitConfirmServerMessageData(jobTaskParameter);

        List<ServerMessageData> fetchServerMessageList = new ArrayList<>();
        for (ServerMessageData serverMessageData : serverMessageDataList) {
            try {
                WaitConfirmCheckRequest waitConfirmCheckRequest = new WaitConfirmCheckRequest();
                waitConfirmCheckRequest.setProducerGroup(serverMessageData.getProducerGroup());
                waitConfirmCheckRequest.setId(serverMessageData.getProducerMessageId());
                ServerRpcHandler serverRpcHandler = nettyServer.getServerRpcHandler();
                Channel channel = serverRpcHandler.getChannel(serverMessageData.getProducerGroup());
                if(channel == null){
                    logger.warn("服务{}未启动",serverMessageData.getProducerGroup());
                    continue;
                }

                ResponseMessage object = (ResponseMessage) serverRpcHandler.sendMessage(waitConfirmCheckRequest,channel);

                if(Wrapper.SUCCESS_CODE == object.getResultCode()){
                    fetchServerMessageList.add(serverMessageData);
                }else{
                    messageService.deleteServerMessageDataById(serverMessageData.getId());
                }
            } catch (Exception e) {
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
