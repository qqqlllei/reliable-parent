package com.reliable.message.server.netty;

import com.reliable.message.server.feign.ClientMessageAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by 李雷 on 2019/5/5.
 */
@Component
public class ProtocolManager {


    @Autowired
    private ClientMessageAdapter clientMessageAdapter;


    @Autowired
    private NettyServer nettyServer;


    public MessageProtocol getMessageProtocolByType(String type) {

        if("http".equals(type)){
            return clientMessageAdapter;
        }
        return nettyServer;
    }

}
