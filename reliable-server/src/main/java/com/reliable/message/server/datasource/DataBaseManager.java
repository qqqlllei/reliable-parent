package com.reliable.message.server.datasource;

import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.netty.ConfirmAndSendRequest;
import com.reliable.message.common.netty.WaitingConfirmRequest;
import com.reliable.message.server.service.MessageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by 李雷 on 2019/5/5.
 */
@Component
public class DataBaseManager {

    @Autowired
    private MessageService messageService;


    public void waitingConfirmRequest(WaitingConfirmRequest waitingConfirmRequest){
        ClientMessageData clientMessageData = new ModelMapper().map(waitingConfirmRequest, ClientMessageData.class);
        messageService.saveMessageWaitingConfirm(clientMessageData);
    }

    public void confirmAndSendRequest(ConfirmAndSendRequest confirmAndSendRequest) {
        messageService.confirmAndSendMessage(confirmAndSendRequest.getProducerMessageId());
    }
}
