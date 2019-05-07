package com.reliable.message.client.protocol.netty;

import com.reliable.message.client.protocol.MessageProtocol;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.netty.message.ConfirmAndSendRequest;
import com.reliable.message.common.netty.message.ConfirmFinishRequest;
import com.reliable.message.common.netty.message.WaitingConfirmRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by 李雷 on 2019/4/29.
 */
public class NettyClient implements MessageProtocol{

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);
    @Value("${netty.server.ip}")
    private String host;

    @Value("${netty.server.port}")
    private Integer port;

    @Value("${spring.application.name}")
    private String applicationId;

    /**唯一标记 */
    private boolean initFalg = true;


    private NettyClientHandler nettyClientHandler;

    private EventLoopGroup group;
    private ChannelFuture f;

    private ReliableMessageService reliableMessageService;


    @PostConstruct
    public void init() {
        group = new NioEventLoopGroup();
        doConnect(new Bootstrap(), group);
    }

    public void doConnect(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {
        try {
            if (bootstrap != null) {
                bootstrap.group(eventLoopGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.handler(new NettyClientInitializer(this));
                bootstrap.remoteAddress(host, port);
                f = bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    final EventLoop eventLoop = futureListener.channel().eventLoop();
                    if (!futureListener.isSuccess()) {
                        logger.info("与服务端断开连接!在10s之后准备尝试重连!");
                        eventLoop.schedule(() -> doConnect(new Bootstrap(), eventLoop), 10, TimeUnit.SECONDS);
                    }
                });
                if(initFalg){
                    logger.info("Netty客户端启动成功!");
                    initFalg=false;
                }
            }
        } catch (Exception e) {
            logger.info("客户端连接失败!"+e.getMessage());
        }

    }

    public void setNettyClientHandler(NettyClientHandler nettyClientHandler) {
        this.nettyClientHandler = nettyClientHandler;
    }

    public NettyClientHandler getNettyClientHandler() {
        return nettyClientHandler;
    }

    @Override
    public void saveMessageWaitingConfirm(ClientMessageData clientMessageData) throws TimeoutException {
        WaitingConfirmRequest waitingConfirmRequest = new ModelMapper().map(clientMessageData, WaitingConfirmRequest.class);
        this.nettyClientHandler.sendMessage(waitingConfirmRequest,f.channel());
    }

    @Override
    public void confirmFinishMessage(String confirmId) throws TimeoutException {
        ConfirmFinishRequest confirmFinishRequest = new ConfirmFinishRequest();
        confirmFinishRequest.setConfirmId(confirmId);
        confirmFinishRequest.setSyncFlag(false);
        this.nettyClientHandler.sendMessage(confirmFinishRequest,f.channel());
    }

    @Override
    public void confirmAndSendMessage(String producerMessageId) throws TimeoutException {
        ConfirmAndSendRequest confirmAndSendRequest = new ConfirmAndSendRequest();
        confirmAndSendRequest.setProducerMessageId(producerMessageId);
        confirmAndSendRequest.setSyncFlag(false);
        this.nettyClientHandler.sendMessage(confirmAndSendRequest,f.channel());
    }

    @Override
    public void saveAndSendMessage(ClientMessageData clientMessageData) {

    }

    @Override
    public void directSendMessage(ClientMessageData clientMessageData) {

    }

    public String getApplicationId(){
        return applicationId;
    }

    public ReliableMessageService getReliableMessageService() {
        return reliableMessageService;
    }

    public void setReliableMessageService(ReliableMessageService reliableMessageService) {
        this.reliableMessageService = reliableMessageService;
    }
}
