package com.reliable.message.server.feign;

import com.reliable.message.model.domain.ClientMessageData;
import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

/**
 * Created by 李雷 on 2018/10/9.
 * Feign 通过eureka 动态获取url调用
 */
@Component
@Import(FeignClientsConfiguration.class)
public class ClientMessageAdapter {



    private static final String DELETE_CLIENT_MESSAGE_URL="/deleteMessage/";
    private static final String GET_CLIENT_MESSAGE_URL="/getClientMessage/";


    @Autowired
    private DiscoveryClient discoveryClient;

    private ClientMessageFeign clientMessageFeign;

    @Autowired
    public ClientMessageAdapter(Decoder decoder, Encoder encoder){
        clientMessageFeign= Feign.builder().encoder(encoder).decoder(decoder)
                .target(Target.EmptyTarget.create(ClientMessageFeign.class));
    }

    public List<ClientMessageData> getClientMessageData(String baseUri, List<String> messageIds) throws URISyntaxException {
        return clientMessageFeign.getClientMessageData(new URI(baseUri), messageIds);
    }

    public ClientMessageData getClientMessageDataByProducerMessageId(String consumerGroup,String producerMessageId) throws URISyntaxException {
        String baseUrl = getBaseUrl(consumerGroup);
        String url = baseUrl+GET_CLIENT_MESSAGE_URL+producerMessageId;
        return clientMessageFeign.getClientMessageDataByProducerMessageId(new URI(url));
    }

    public void deleteClientMessageData(String consumerGroup,String producerMessageId) throws URISyntaxException {
        String baseUrl = getBaseUrl(consumerGroup);
        String deleteUrl = baseUrl+DELETE_CLIENT_MESSAGE_URL+producerMessageId;
        clientMessageFeign.deleteClientMessageData(new URI(deleteUrl));
    }

    private String getBaseUrl(String consumerGroup){
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(consumerGroup);
        int index = new Random().nextInt(serviceInstances.size());
        ServiceInstance serviceInstance = serviceInstances.get(index);

        return serviceInstance.getUri().toString();
    }

}
