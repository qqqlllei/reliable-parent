package com.reliable.message.server.feign;

import com.reliable.message.model.domain.ClientMessageData;
import feign.RequestLine;
import org.springframework.cloud.netflix.feign.FeignClient;

import java.net.URI;
import java.util.List;

/**
 * Created by 李雷 on 2018/10/9.
 */
@FeignClient("clientMessageFeign")
public interface ClientMessageFeign {

    @RequestLine("GET")
    List<ClientMessageData> getClientMessageData(URI baseUri,List<String> messageIds);

}
