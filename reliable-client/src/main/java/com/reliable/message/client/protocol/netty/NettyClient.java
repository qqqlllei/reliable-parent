package com.reliable.message.client.protocol.netty;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.discovery.RegistryFactory;
import com.reliable.message.common.netty.NamedThreadFactory;
import com.reliable.message.common.netty.message.*;
import com.reliable.message.common.wrapper.Wrapper;
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
import java.util.Map;
import java.util.concurrent.*;

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
    private static final int TIMEOUT_CHECK_INTERNAL = 10000;
    private static final int MESSAGE_CHECK_PERIOD = 5000;
    private static final int KEEP_ALIVE_TIME = 500;
    private static final ThreadPoolExecutor WORKING_THREADS = new ThreadPoolExecutor(MIN_SERVER_POOL_SIZE,
            MAX_SERVER_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue(MAX_TASK_QUEUE_SIZE),
            new NamedThreadFactory("ClientHandlerThread", MAX_SERVER_POOL_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());

    protected final ScheduledExecutorService saveAndSendMessageCheck = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("messageCheck", 1, true));
    private final ConcurrentMap<String, Object> channelLocks = new ConcurrentHashMap<>();

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

        saveAndSendMessageCheck.scheduleWithFixedDelay(() -> {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fetchNum",50);
            List<String> clientMessageIds = reliableMessageService.getProducerMessage(jsonObject);
            for (String id : clientMessageIds) {
                WORKING_THREADS.execute(() -> checkServerMessageIsExist(id));
            }
        }, TIMEOUT_CHECK_INTERNAL, MESSAGE_CHECK_PERIOD, TimeUnit.MILLISECONDS);
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

        String serverLockString = serverAddress.toString();

        channelLocks.putIfAbsent(serverLockString, new Object());
        Object connectLock = channelLocks.get(serverLockString);

        synchronized (connectLock) {
            Channel channel;
            logger.info("================channels=============="+clientRpcHandler.getChannels());
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


    public void confirmAndSendMessage(String producerMessageId)  {
        ConfirmAndSendRequest confirmAndSendRequest = new ConfirmAndSendRequest();
        confirmAndSendRequest.setProducerMessageId(producerMessageId);
        confirmAndSendRequest.setSyncFlag(false);
        try {
            this.clientRpcHandler.sendMessage(confirmAndSendRequest, getExistAliveChannel());
        } catch (TimeoutException e) {
            logger.warn("confirmAndSendMessage error - 生产者 消息id={}", producerMessageId);
        }
    }


    public void saveAndSendMessage(SaveAndSendRequest saveAndSendRequest)  {
        saveAndSendRequest.setSyncFlag(false);
        try {
            this.clientRpcHandler.sendMessage(saveAndSendRequest,getExistAliveChannel());
        } catch (TimeoutException e) {
            logger.warn("saveAndSendMessage error - 生产者 消息id={}", saveAndSendRequest.getId());
        }
    }

    public void checkServerMessageIsExist(String id) {
        CheckServerMessageRequest checkServerMessageRequest = new CheckServerMessageRequest();
        checkServerMessageRequest.setId(id);
        try {
            ResponseMessage responseMessage = (ResponseMessage) this.clientRpcHandler.sendMessage(checkServerMessageRequest,getExistAliveChannel());
            if(Wrapper.SUCCESS_CODE == responseMessage.getResultCode()){
                reliableMessageService.updateMessage(checkServerMessageRequest);
            }else if(Wrapper.WITHOUT_MESSAGE == responseMessage.getResultCode()){

                Map<String, Object> requestMessage = reliableMessageService.getRequestMessageById(id);
                SaveAndSendRequest saveAndSendRequest = new ModelMapper().map(requestMessage, SaveAndSendRequest.class);
                saveAndSendRequest.setSyncFlag(false);
                clientRpcHandler.sendMessage(saveAndSendRequest,getExistAliveChannel());
            }
        } catch (TimeoutException e) {
            logger.warn("checkServerMessageIsExist error - 生产者 消息id={}", id);
        }

    }


    public void directSendMessage(DirectSendRequest directSendRequest) throws TimeoutException {
        directSendRequest.setSyncFlag(false);
        this.clientRpcHandler.sendMessage(directSendRequest,getExistAliveChannel());
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
