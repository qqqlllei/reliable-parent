package com.reliable.message.server.feign;

import com.reliable.message.common.domain.ClientMessageData;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.net.URI;
import java.util.List;

/**
 * Created by 李雷 on 2018/10/9.
 */
@FeignClient("clientMessageFeign")
public interface ClientMessageFeign {

    @RequestLine("GET")
    List<ClientMessageData> getClientMessageData(URI baseUri,List<String> messageIds);


    @RequestLine("POST")
    void deleteClientMessageData(URI uri);

    @RequestLine("GET")
    ClientMessageData getClientMessageDataByProducerMessageId(URI uri);
}
