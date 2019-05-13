package com.reliable.message.client.protocol.netty;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.discovery.RegistryFactory;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.dto.MessageData;
import com.reliable.message.common.netty.NamedThreadFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by 李雷 on 2019/4/29.
 */
public class NettyClient {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final int MAX_CHECK_ALIVE_RETRY = 3;
    private static final int connectTimeoutMillis = 10000;
    private static final int MIN_SERVER_POOL_SIZE = 10;
    private static final int MAX_SERVER_POOL_SIZE = 50;
    private static final int MAX_TASK_QUEUE_SIZE = 2000;
    private static final int KEEP_ALIVE_TIME = 500;
    private static final ThreadPoolExecutor WORKING_THREADS = new ThreadPoolExecutor(MIN_SERVER_POOL_SIZE,
            MAX_SERVER_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue(MAX_TASK_QUEUE_SIZE),
            new NamedThreadFactory("ClientHandlerThread", MAX_SERVER_POOL_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());


    @Value("${netty.server.ip}")
    private String host;

    @Value("${netty.server.port}")
    private Integer port;

    @Value("${spring.application.name}")
    private String applicationId;


    private ClientRpcHandler clientRpcHandler;

    private EventLoopGroup group;
    private Bootstrap bootstrap;

    private ReliableMessageService reliableMessageService;



    @PostConstruct
    public void init() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        clientRpcHandler = new ClientRpcHandler(this,WORKING_THREADS);
        if (bootstrap != null) {
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ClientChannelInitializer(clientRpcHandler));
        }
        connect();
    }

    public void connect(){
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

    public Channel doConnect(InetSocketAddress serverAddress) {
        Channel channel;
        channel = clientRpcHandler.getChannels().get(serverAddress.toString());
        if(channel !=null && channel.isActive()){
            return channel;
        }

        try {
            ChannelFuture f = this.bootstrap.connect(serverAddress);
            f.await(connectTimeoutMillis, TimeUnit.MILLISECONDS);

            if (f.isCancelled()) {
                throw new RuntimeException("connect cancelled, can not connect to services-server.",f.cause());
            } else if (!f.isSuccess()) {
                throw new RuntimeException("connect failed, can not connect to services-server.",f.cause());
            } else {
                channel = f.channel();
                clientRpcHandler.getChannels().put(channel.remoteAddress().toString(),channel);
                ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
                clientRegisterRequest.setApplicationId(applicationId);
                clientRegisterRequest.setSyncFlag(true);
                channel.writeAndFlush(clientRegisterRequest);
                return channel;
            }
        } catch (Exception e) {
            if(channel !=null){
                channel.close();
            }
            clientRpcHandler.getChannels().remove(serverAddress.toString());
            throw new RuntimeException("connect failed, can not connect to services-server.",e.getCause());
        }
    }

    public void setClientRpcHandler(ClientRpcHandler clientRpcHandler) {
        this.clientRpcHandler = clientRpcHandler;
    }

    public ClientRpcHandler getClientRpcHandler() {
        return clientRpcHandler;
    }


    public void saveMessageWaitingConfirm(WaitingConfirmRequest waitingConfirmRequest) throws TimeoutException {
        this.clientRpcHandler.sendMessage(waitingConfirmRequest,getExistAliveChannel());
    }


    public void confirmFinishMessage(String confirmId) throws TimeoutException {
        ConfirmFinishRequest confirmFinishRequest = new ConfirmFinishRequest();
        confirmFinishRequest.setConfirmId(confirmId);
        confirmFinishRequest.setSyncFlag(false);
        this.clientRpcHandler.sendMessage(confirmFinishRequest, getExistAliveChannel());
    }


    public void confirmAndSendMessage(String producerMessageId) throws TimeoutException {
        ConfirmAndSendRequest confirmAndSendRequest = new ConfirmAndSendRequest();
        confirmAndSendRequest.setProducerMessageId(producerMessageId);
        confirmAndSendRequest.setSyncFlag(false);
        this.clientRpcHandler.sendMessage(confirmAndSendRequest, getExistAliveChannel());
    }


    public void saveAndSendMessage(ClientMessageData clientMessageData) {

    }


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


    private Channel getExistAliveChannel(){
        Channel channel=null;
        int i = 0;
        for (; i < MAX_CHECK_ALIVE_RETRY; i++) {
            try{
                channel =  doConnect(clientRpcHandler.loadBalance());
                return channel;
            }catch (Exception e){
               if(i == MAX_CHECK_ALIVE_RETRY){
                   logger.info("all server is down");
               }
            }
        }
        return channel;
    }
}
