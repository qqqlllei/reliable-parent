package com.reliable.message.server.feign;

import com.reliable.message.model.domain.ClientMessageData;
import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by 李雷 on 2018/10/9.
 */
@Component
@Import(FeignClientsConfiguration.class)
public class ClientMessageAdapter {

    private ClientMessageFeign clientMessageFeign;

    @Autowired
    public ClientMessageAdapter(Decoder decoder, Encoder encoder){
        clientMessageFeign= Feign.builder().encoder(encoder).decoder(decoder)
                .target(Target.EmptyTarget.create(ClientMessageFeign.class));
    }

    public List<ClientMessageData> getClientMessageData(String baseUri, List<String> messageIds) throws URISyntaxException {
        return clientMessageFeign.getClientMessageData(new URI(baseUri), messageIds);
    }

}
