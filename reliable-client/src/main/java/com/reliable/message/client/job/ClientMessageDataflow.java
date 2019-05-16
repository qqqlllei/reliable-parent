//package com.reliable.message.client.job;
//
//import com.alibaba.fastjson.JSONObject;
//import com.job.lite.annotation.ElasticJobConfig;
//import com.job.lite.job.AbstractBaseDataflowJob;
//import com.reliable.message.client.protocol.netty.NettyClient;
//import com.reliable.message.client.service.ReliableMessageService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
///**
// * Created by 李雷
// */
//@ElasticJobConfig(cron = "elastic.job.cron.clientMessageDataflowCron", jobParameter = "{'fetchNum':200,'taskType':'SENDING_MESSAGE'}",description="生产者消息清理")
//public class ClientMessageDataflow extends AbstractBaseDataflowJob<String> {
//
//    private Logger logger = LoggerFactory.getLogger(ClientMessageDataflow.class);
//
//    @Autowired
//    private ReliableMessageService reliableMessageService;
//
//    @Autowired
//    private NettyClient nettyClient;
//
//    @Override
//    protected List<String> fetchJobData(final JSONObject jobTaskParameter) {
//        logger.info("fetchJobData - jobTaskParameter={}", jobTaskParameter);
//        List<String> clientMessageIds = reliableMessageService.getProducerMessage(jobTaskParameter);
//        return clientMessageIds;
//    }
//
//    @Override
//    protected void processJobData(final List<String> clientMessageIds) {
//        logger.info("processJobData - clientMessageIds={}", clientMessageIds);
//        //查询服务端消息状态，该消息是否已经消费成功（从消息服务段查询不到该消息）
//        for (String id : clientMessageIds) {
//            nettyClient.checkServerMessageIsExist(id);
//        }
//    }
//}