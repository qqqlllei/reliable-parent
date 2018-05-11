package com.reliable.message.client.feign;

import com.reliable.message.client.hystrix.MqMessageFeignHystrix;

import com.reliable.message.model.dto.TpcMqMessageDto;
import com.reliable.message.model.wrapper.Wrapper;
import org.springframework.cloud.netflix.feign.FeignClient;

/**
 * Created by 李雷 on 2018/5/7.
 */
@FeignClient(value = "reliable-message", fallback = MqMessageFeignHystrix.class)
public interface MqMessageFeign {


    Wrapper saveMessageWaitingConfirm(TpcMqMessageDto tpcMqMessageDto);

    Wrapper confirmAndSendMessage(String messageKey);
}
