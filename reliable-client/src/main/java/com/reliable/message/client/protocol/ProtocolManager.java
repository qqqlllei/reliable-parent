package com.reliable.message.client.protocol;

/**
 * Created by 李雷 on 2019/5/5.
 */
public class ProtocolManager {

    private MessageProtocol messageProtocol;

    public ProtocolManager(MessageProtocol messageProtocol){
        this.messageProtocol = messageProtocol;
    }


    public MessageProtocol getMessageProtocol() {
        return messageProtocol;
    }

}
