package com.reliable.message.client.protocol.netty;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.reliable.message.client.protocol.MessageProtocol;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.discovery.RegistryFactory;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.netty.message.ClientRegisterRequest;
import com.reliable.message.common.netty.message.ConfirmAndSendRequest;
import com.reliable.message.common.netty.message.ConfirmFinishRequest;
import com.reliable.message.common.netty.message.WaitingConfirmRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by 李雷 on 2019/4/29.
 */
public class NettyClient implements MessageProtocol{

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final int connectTimeoutMillis = 10000;


    @Value("${netty.server.ip}")
    private String host;

    @Value("${netty.server.port}")
    private Integer port;

    @Value("${spring.application.name}")
    private String applicationId;


    private NettyClientHandler nettyClientHandler;

    private EventLoopGroup group;
    private Bootstrap bootstrap;

    private ReliableMessageService reliableMessageService;



    @PostConstruct
    public void init() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        nettyClientHandler = new NettyClientHandler(this);
        if (bootstrap != null) {
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new NettyClientInitializer(nettyClientHandler));
        }

        try {
            List<InetSocketAddress> inetSocketAddresses =  RegistryFactory.getInstance("nacos_register").lookup("");
            if(CollectionUtils.isEmpty(inetSocketAddresses)){
                logger.error("no available server to connect.");
                return;
            }

            for (InetSocketAddress serverAddress : inetSocketAddresses) {
                try {
                    doConnect(serverAddress);
                } catch (Exception e) {
                    logger.error("can not connect to " + serverAddress + " cause:" + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doConnect(InetSocketAddress serverAddress) {
        try {
            ChannelFuture f = this.bootstrap.connect(serverAddress);
            f.await(connectTimeoutMillis, TimeUnit.MILLISECONDS);

            if (f.isCancelled()) {
                throw new RuntimeException("connect cancelled, can not connect to services-server.",f.cause());
            } else if (!f.isSuccess()) {
                throw new RuntimeException("connect failed, can not connect to services-server.",f.cause());
            } else {
                Channel channel = f.channel();

                nettyClientHandler.getChannels().put(channel.remoteAddress().toString(),channel);
                ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
                clientRegisterRequest.setApplicationId(applicationId);
                clientRegisterRequest.setSyncFlag(true);
                channel.writeAndFlush(clientRegisterRequest);
            }
        } catch (Exception e) {
            throw new RuntimeException("connect failed, can not connect to services-server.",e.getCause());
        }
    }

    public void setNettyClientHandler(NettyClientHandler nettyClientHandler) {
        this.nettyClientHandler = nettyClientHandler;
    }

    public NettyClientHandler getNettyClientHandler() {
        return nettyClientHandler;
    }

    @Override
    public void saveMessageWaitingConfirm(ClientMessageData clientMessageData) throws Exception {
        WaitingConfirmRequest waitingConfirmRequest = new ModelMapper().map(clientMessageData, WaitingConfirmRequest.class);

        this.nettyClientHandler.sendMessage(waitingConfirmRequest,nettyClientHandler.getChannel(null));
    }

    @Override
    public void confirmFinishMessage(String confirmId) throws TimeoutException {
        ConfirmFinishRequest confirmFinishRequest = new ConfirmFinishRequest();
        confirmFinishRequest.setConfirmId(confirmId);
        confirmFinishRequest.setSyncFlag(false);
        this.nettyClientHandler.sendMessage(confirmFinishRequest,nettyClientHandler.getChannel(null));
    }

    @Override
    public void confirmAndSendMessage(String producerMessageId) throws TimeoutException {
        ConfirmAndSendRequest confirmAndSendRequest = new ConfirmAndSendRequest();
        confirmAndSendRequest.setProducerMessageId(producerMessageId);
        confirmAndSendRequest.setSyncFlag(false);
        this.nettyClientHandler.sendMessage(confirmAndSendRequest,nettyClientHandler.getChannel(null));
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
