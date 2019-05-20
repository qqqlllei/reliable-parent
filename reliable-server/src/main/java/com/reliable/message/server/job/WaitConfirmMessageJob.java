package com.reliable.message.server.job;

import com.alibaba.fastjson.JSONObject;
import com.job.lite.annotation.ElasticJobConfig;
import com.job.lite.job.AbstractBaseDataflowJob;
import com.reliable.message.common.domain.ReliableMessage;
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
        jobParameter = "{'fetchNum':300,'taskType':'SENDING_MESSAGE'}",description="待确认消息异常处理")
public class WaitConfirmMessageJob extends AbstractBaseDataflowJob<ReliableMessage> {


    private Logger logger = LoggerFactory.getLogger(WaitConfirmMessageJob.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private NettyServer nettyServer;


    @Override
    protected List<ReliableMessage> fetchJobData(JSONObject jobTaskParameter) {

        logger.info("WaitConfirmMessageJob.fetchJobData - jobTaskParameter={}", jobTaskParameter);

        messageService.getSendingMessageData(jobTaskParameter);

        List<ReliableMessage> reliableMessageList =  messageService.getWaitConfirmServerMessageData(jobTaskParameter);

        List<ReliableMessage> fetchServerMessageList = new ArrayList<>();
        for (ReliableMessage reliableMessage : reliableMessageList) {
            try {
                WaitConfirmCheckRequest waitConfirmCheckRequest = new WaitConfirmCheckRequest();
                waitConfirmCheckRequest.setProducerGroup(reliableMessage.getProducerGroup());
                waitConfirmCheckRequest.setId(reliableMessage.getProducerMessageId());
                ServerRpcHandler serverRpcHandler = nettyServer.getServerRpcHandler();
                Channel channel = serverRpcHandler.getClientChannel(reliableMessage.getProducerGroup());
                if(channel == null){
                    logger.warn("服务{}未启动", reliableMessage.getProducerGroup());
                    continue;
                }

                ResponseMessage object = (ResponseMessage) serverRpcHandler.sendMessage(waitConfirmCheckRequest,channel);

                if(Wrapper.SUCCESS_CODE == object.getResultCode()){
                    fetchServerMessageList.add(reliableMessage);
                }else if(Wrapper.WITHOUT_MESSAGE == object.getResultCode()){
                    logger.info("=======================WITHOUT_MESSAGE =========================="+ reliableMessage.getProducerMessageId());
                    messageService.deleteServerMessageDataById(reliableMessage.getId());
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }


        return fetchServerMessageList;
    }

    @Override
    protected void processJobData(List<ReliableMessage> reliableMessageList) {
        logger.info("WaitConfirmMessageJob.processJobData - serverMessageDataList={}", reliableMessageList);

        for (ReliableMessage reliableMessage : reliableMessageList) {

            messageService.confirmAndSendMessage(reliableMessage.getProducerMessageId());
        }
    }
}
