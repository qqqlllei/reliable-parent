package com.reliable.message.server.netty;

import com.reliable.message.common.netty.RoundRobinLoadBalance;
import com.reliable.message.common.netty.message.*;
import com.reliable.message.common.netty.rpc.AbstractRpcHandler;
import com.reliable.message.common.wrapper.Wrapper;
import com.reliable.message.server.datasource.DataBaseManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

@Sharable
@Slf4j
public class ServerRpcHandler extends AbstractRpcHandler {

    private final ConcurrentMap<String, ConcurrentMap<String,Channel>> channels = new ConcurrentHashMap<>();

    private DataBaseManager dataBaseManager;

    private ThreadPoolExecutor messageExecutor;

    public ServerRpcHandler(NettyServer nettyServer,ThreadPoolExecutor messageExecutor){
        this.dataBaseManager = nettyServer.getDataBaseManager();
        this.messageExecutor = messageExecutor;
        nettyServer.setServerRpcHandler(this);
        super.roundRobinLoadBalance = new RoundRobinLoadBalance();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,
                             Object msg) throws Exception {

        try {
            if(msg instanceof ClientRegisterRequest){
                ClientRegisterRequest clientRegisterRequest = (ClientRegisterRequest) msg;
                ConcurrentMap<String,Channel>  channelGroup = this.channels.get(clientRegisterRequest.getApplicationId());
                Channel channel = ctx.channel();
                if(channelGroup == null){
                    channelGroup= new ConcurrentHashMap<>();
                    channelGroup.put(channel.remoteAddress().toString(),channel);
                    this.channels.put(clientRegisterRequest.getApplicationId(),channelGroup);
                    return;
                }

                if(!channelGroup.containsKey(channel.remoteAddress().toString())){
                    channelGroup.put(channel.remoteAddress().toString(),channel);
                }
                return;
            }

            if(msg instanceof RequestMessage){
                messageExecutor.execute(() -> {
                    if(msg instanceof WaitingConfirmRequest){
                        WaitingConfirmRequest waitingConfirmRequest = (WaitingConfirmRequest) msg;
                        dataBaseManager.waitingConfirmRequest(waitingConfirmRequest);
                        ResponseMessage responseMessage = new ResponseMessage();
                        responseMessage.setResultCode(200);
                        responseMessage.setId(waitingConfirmRequest.getId());
                        ctx.writeAndFlush(responseMessage);

                        return;
                    }

                    if(msg instanceof ConfirmAndSendRequest){
                        ConfirmAndSendRequest confirmAndSendRequest = (ConfirmAndSendRequest) msg;
                        dataBaseManager.confirmAndSendRequest(confirmAndSendRequest);

                        return;
                    }

                    if(msg instanceof DirectSendRequest){
                        DirectSendRequest directSendRequest =  (DirectSendRequest) msg;
                        dataBaseManager.directSendMessage(directSendRequest);
                        return;
                    }

                    if(msg instanceof SaveAndSendRequest){
                        SaveAndSendRequest saveAndSendRequest = (SaveAndSendRequest) msg;
                        dataBaseManager.saveAndSendMessage(saveAndSendRequest);

                        ReceiveSaveAndSendRequest receiveSaveAndSendRequest = new ReceiveSaveAndSendRequest();
                        receiveSaveAndSendRequest.setId(saveAndSendRequest.getId());
                        ctx.writeAndFlush(receiveSaveAndSendRequest);
                        return;
                    }

                    if(msg instanceof CheckServerMessageRequest){
                        CheckServerMessageRequest checkServerMessageRequest = (CheckServerMessageRequest) msg;
                        boolean isExist = dataBaseManager.checkMessageIsExist(checkServerMessageRequest.getId());

                        ResponseMessage responseMessage = new ResponseMessage();
                        responseMessage.setId(checkServerMessageRequest.getId());
                        if(isExist){
                            responseMessage.setResultCode(Wrapper.SUCCESS_CODE);
                        }else{
                            responseMessage.setResultCode(Wrapper.WITHOUT_MESSAGE);
                        }
                        ctx.writeAndFlush(responseMessage);
                        return;

                    }

                    if(msg instanceof ConfirmFinishRequest){
                        ConfirmFinishRequest confirmFinishRequest =  (ConfirmFinishRequest) msg;
                        dataBaseManager.confirmFinishRequest(confirmFinishRequest.getConfirmId());
                        return;
                    }
                });
            }
            super.channelRead(ctx,msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


    public Channel getClientChannel(String applicationId){
        Channel channel;
        ConcurrentMap<String,Channel>  channelGroup = this.channels.get(applicationId);
        Collection<Channel> channelList = channelGroup.values();
        while (channelList.size()>0){
            channel = roundRobinLoadBalance.doSelect(new ArrayList<>(channelList));
            if(channel.isActive()){
                return channel;
            }
            channelList.remove(channel);
        }
        return null;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.READER_IDLE.equals(event.state())) {
                log.warn("已经5秒没有接收到客户端的信息了");
                ctx.disconnect();
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info(cause.getMessage()+"--"+ctx.channel().toString());
        ctx.disconnect();
        ctx.close();
    }
}