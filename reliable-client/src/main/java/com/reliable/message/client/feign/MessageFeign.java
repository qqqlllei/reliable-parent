package com.reliable.message.client.feign;

import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.wrapper.Wrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by 李雷 on 2018/5/7.
 */
@Component
@FeignClient(value = "reliable-server")
public interface MessageFeign {


    @RequestMapping(value = "/message/saveMessageWaitingConfirm",method = RequestMethod.POST)
    Wrapper saveMessageWaitingConfirm(@RequestBody ClientMessageData clientMessageData);

    @RequestMapping(value = "/message/confirmAndSendMessage",method = RequestMethod.POST)
    Wrapper confirmAndSendMessage(@RequestParam("producerMessageId") String producerMessageId);

    @RequestMapping(value = "/message/confirmFinishMessage",method = RequestMethod.POST)
    Wrapper confirmFinishMessage(@RequestParam("confirmId") String confirmId);

    @RequestMapping(value = "/message/checkServerMessageIsExist",method = RequestMethod.POST)
    Wrapper checkServerMessageIsExist(@RequestParam("producerMessageId") String producerMessageId);

    @RequestMapping(value = "/message/directSendMessage",method = RequestMethod.POST)
    Wrapper directSendMessage(ClientMessageData clientMessageData);

    @RequestMapping(value = "/message/saveAndSendMessage",method = RequestMethod.POST)
    Wrapper saveAndSendMessage(ClientMessageData clientMessageData);
}
