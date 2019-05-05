package com.reliable.message.client.protocol;

import com.reliable.message.common.domain.ClientMessageData;

import java.util.concurrent.TimeoutException;

/**
 * Created by 李雷 on 2019/5/5.
 */
public interface MessageProtocol {


    void saveMessageWaitingConfirm(ClientMessageData clientMessageData) throws TimeoutException;

    void confirmFinishMessage(String confirmId) throws TimeoutException;

    void confirmAndSendMessage(String producerMessageId) throws TimeoutException;

    void saveAndSendMessage(ClientMessageData clientMessageData);

    void directSendMessage(ClientMessageData clientMessageData);
}
