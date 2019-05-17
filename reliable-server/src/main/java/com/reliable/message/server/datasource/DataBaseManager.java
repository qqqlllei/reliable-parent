package com.reliable.message.server.datasource;

import com.reliable.message.common.domain.ReliableMessage;
import com.reliable.message.common.netty.message.ConfirmAndSendRequest;
import com.reliable.message.common.netty.message.DirectSendRequest;
import com.reliable.message.common.netty.message.SaveAndSendRequest;
import com.reliable.message.common.netty.message.WaitingConfirmRequest;
import com.reliable.message.server.service.MessageService;
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
        messageService.saveMessageWaitingConfirm(waitingConfirmRequest);
    }

    public void confirmAndSendRequest(ConfirmAndSendRequest confirmAndSendRequest) {
        messageService.confirmAndSendMessage(confirmAndSendRequest.getId());
    }

    public void confirmFinishRequest(String confirmId) {
        messageService.confirmFinishMessage(confirmId);
    }

    public void saveAndSendMessage(SaveAndSendRequest saveAndSendRequest){
        messageService.saveAndSendMessage(saveAndSendRequest);
    }

    public void directSendMessage(DirectSendRequest directSendRequest){
        messageService.directSendMessage(directSendRequest);
    }

    public boolean checkMessageIsExist(String producerMessageId){
        ReliableMessage reliableMessage =  messageService.getServerMessageDataByProducerMessageId(producerMessageId);
        if(reliableMessage !=null){
            return true;
        }
        return  false;

    }
}
