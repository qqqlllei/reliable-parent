//package com.reliable.message.client.hystrix;
//
//import com.reliable.message.client.feign.MessageFeign;
//import com.reliable.message.common.domain.ClientMessageData;
//import com.reliable.message.common.wrapper.Wrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestParam;
//
///**
// * Created by 李雷 on 2018/5/7.
// */
//@Component
//@Slf4j
//public class MessageFeignHystrix implements MessageFeign {
//
//    @Override
//    public Wrapper saveMessageWaitingConfirm(ClientMessageData clientMessageData) {
//
//        return null;
//    }
//
//    @Override
//    public Wrapper confirmAndSendMessage(String messageKey) {
//        return null;
//    }
//
//    @Override
//    public Wrapper confirmFinishMessage(@RequestParam("consumerGroup") String consumerGroup, @RequestParam("messageKey") String messageKey) {
//        return null;
//    }
//
//    @Override
//    public Wrapper checkServerMessageIsExist(@RequestParam("producerMessageId") String producerMessageId) {
//        return null;
//    }
//
//    @Override
//    public Wrapper directSendMessage(ClientMessageData clientMessageData) {
//        return null;
//    }
//
//    @Override
//    public Wrapper saveAndSendMessage(ClientMessageData clientMessageData) {
//        return null;
//    }
//}
