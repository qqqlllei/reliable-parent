//package com.reliable.message.client.feign;
//
//import com.reliable.message.client.protocol.MessageProtocol;
//import com.reliable.message.common.domain.ClientMessageData;
//import com.reliable.message.common.wrapper.Wrapper;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//
///**
// * Created by 李雷 on 2018/5/7.
// */
//@Component
//@FeignClient(value = "reliable-server")
//public interface MessageFeign extends MessageProtocol {
//
//
//    @RequestMapping(value = "/message/saveMessageWaitingConfirm",method = RequestMethod.POST)
//    void saveMessageWaitingConfirm(@RequestBody ClientMessageData clientMessageData);
//
//    @RequestMapping(value = "/message/confirmAndSendMessage",method = RequestMethod.POST)
//    void confirmAndSendMessage(@RequestParam("producerMessageId") String producerMessageId);
//
//    @RequestMapping(value = "/message/confirmFinishMessage",method = RequestMethod.POST)
//    void confirmFinishMessage(@RequestParam("confirmId") String confirmId);
//
//    @RequestMapping(value = "/message/checkServerMessageIsExist",method = RequestMethod.POST)
//    void checkServerMessageIsExist(@RequestParam("producerMessageId") String producerMessageId);
//
//    @RequestMapping(value = "/message/directSendMessage",method = RequestMethod.POST)
//    void directSendMessage(ClientMessageData clientMessageData);
//
//    @RequestMapping(value = "/message/saveAndSendMessage",method = RequestMethod.POST)
//    void saveAndSendMessage(ClientMessageData clientMessageData);
//}
