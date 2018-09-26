package com.reliable.message.client.feign;

import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.wrapper.Wrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
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
public interface MqMessageFeign {


    @RequestMapping(value = "/message/saveMessageWaitingConfirm",method = RequestMethod.POST)
    Wrapper saveMessageWaitingConfirm(@RequestBody ClientMessageData clientMessageData);

    @RequestMapping(value = "/message/confirmAndSendMessage",method = RequestMethod.POST)
    Wrapper confirmAndSendMessage(@RequestParam("clientMessageId") String clientMessageId);

    @RequestMapping(value = "/message/confirmReceiveMessage",method = RequestMethod.POST)
    Wrapper confirmReceiveMessage(@RequestParam("consumerGroup") final String consumerGroup, @RequestParam("messageKey") final String messageKey);

    @RequestMapping(value = "/message/confirmConsumedMessage",method = RequestMethod.POST)
    Wrapper confirmConsumedMessage(@RequestParam("consumerGroup") String consumerGroup,@RequestParam("messageKey") String messageKey);
}
