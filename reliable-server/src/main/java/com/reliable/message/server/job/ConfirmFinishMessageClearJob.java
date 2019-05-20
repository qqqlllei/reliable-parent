//package com.reliable.message.server.job;
//
//import com.alibaba.fastjson.JSONObject;
//import com.job.lite.annotation.ElasticJobConfig;
//import com.job.lite.job.AbstractBaseDataflowJob;
//
//import com.reliable.message.common.domain.ReliableMessage;
//import com.reliable.message.server.service.MessageConfirmService;
//import com.reliable.message.server.service.MessageService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * Created by 李雷 on 2018/10/10.
// */
//@Component
//@ElasticJobConfig(cron = "elastic.job.cron.confirmFinishMessageClearCron",
//        jobParameter = "{'fetchNum':200,'taskType':'SENDING_MESSAGE'}",description="消息服务成功消费记录清除")
//public class ConfirmFinishMessageClearJob extends AbstractBaseDataflowJob<ReliableMessage> {
//    private Logger logger = LoggerFactory.getLogger(ConfirmFinishMessageClearJob.class);
//
//    @Autowired
//    private MessageService messageService;
//
//    @Autowired
//    private MessageConfirmService messageConfirmService;
//
//    @Override
//    protected List<ReliableMessage> fetchJobData(JSONObject jobTaskParameter) {
//        logger.info("fetchJobData - jobTaskParameter={}", jobTaskParameter);
//        List<ReliableMessage> reliableMessageList =  messageService.getServerMessageDataByParams(jobTaskParameter);
//        return reliableMessageList;
//    }
//
//    @Override
//    protected void processJobData(List<ReliableMessage> reliableMessageList) {
//        for (ReliableMessage reliableMessage : reliableMessageList) {
//            int count = messageConfirmService.getMessageConfirmCountByProducerMessageId(reliableMessage.getProducerMessageId());
//            if(count == 0){
//                messageService.clearFinishMessage(reliableMessage.getId());
//            }
//        }
//    }
//}
