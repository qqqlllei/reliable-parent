package com.reliable.message.client.feign;

import com.reliable.message.client.hystrix.MqMessageFeignHystrix;

import com.reliable.message.model.dto.TpcMqMessageDto;
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
    Wrapper saveMessageWaitingConfirm(@RequestBody TpcMqMessageDto tpcMqMessageDto);

    @RequestMapping(value = "/message/confirmAndSendMessage",method = RequestMethod.POST)
    Wrapper confirmAndSendMessage(@RequestParam("messageKey") String messageKey);
}
