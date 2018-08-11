package com.reliable.message.client.hystrix;

import com.reliable.message.client.feign.MqMessageFeign;

import com.reliable.message.model.dto.TpcMqMessageDto;
import com.reliable.message.model.wrapper.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by 李雷 on 2018/5/7.
 */
@Component
@Slf4j
public class MqMessageFeignHystrix implements MqMessageFeign {

    @Override
    public Wrapper saveMessageWaitingConfirm(TpcMqMessageDto tpcMqMessageDto) {

        return null;
    }

    @Override
    public Wrapper confirmAndSendMessage(String messageKey) {
        return null;
    }

    @Override
    public Wrapper confirmReceiveMessage(@RequestParam("consumerGroup") String consumerGroup, @RequestParam("messageKey") String messageKey) {
        return null;
    }
}
