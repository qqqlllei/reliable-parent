package com.reliable.message.server.netty;

import com.reliable.message.common.netty.RoundRobinLoadBalance;
import com.reliable.message.common.netty.message.*;
import com.reliable.message.common.netty.rpc.AbstractRpcHandler;
import com.reliable.message.server.datasource.DataBaseManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
@Sharable
public class TCPServerHandler extends AbstractRpcHandler {

    private static Logger logger = LoggerFactory.getLogger(TCPServerHandler.class);


    private DataBaseManager dataBaseManager;

    private RoundRobinLoadBalance roundRobinLoadBalance;

    private NettyServer nettyServer;

    public TCPServerHandler(NettyServer nettyServer){
        this.nettyServer = nettyServer;
        this.dataBaseManager = nettyServer.getDataBaseManager();
        nettyServer.setTcpServerHandler(this);
        this.roundRobinLoadBalance = new RoundRobinLoadBalance();
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
                ConcurrentMap<String, ConcurrentMap<String,Channel>> channels = nettyServer.getChannels();
                ConcurrentMap<String,Channel>  channelGroup = channels.get(clientRegisterRequest.getApplicationId());
                Channel channel = ctx.channel();

                if(channelGroup == null){
                    channelGroup= new ConcurrentHashMap<>();
                    channelGroup.put(channel.remoteAddress().toString(),channel);
                    channels.put(clientRegisterRequest.getApplicationId(),channelGroup);
                    return;
                }


                if(!channelGroup.containsKey(channel.remoteAddress().toString())){
                    channelGroup.put(channel.remoteAddress().toString(),channel);
                }
                return;
            }


            if(msg instanceof RequestMessage){

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

               if(msg instanceof ConfirmFinishRequest){
                   ConfirmFinishRequest confirmFinishRequest =  (ConfirmFinishRequest) msg;
                   dataBaseManager.confirmFinishRequest(confirmFinishRequest.getConfirmId());
                   return;
               }
            }


            super.channelRead(ctx,msg);




        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果读通道处于空闲状态，说明没有接收到心跳命令
            if (IdleState.READER_IDLE.equals(event.state())) {
                logger.warn("已经5秒没有接收到客户端的信息了");
                ctx.disconnect();
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info(cause.getMessage()+"--"+ctx.channel().toString());
        ctx.disconnect();
        ctx.close();
    }




    public Channel getChannel(String applicationId){
        Channel channel;
        ConcurrentMap<String,Channel>  channelGroup = this.nettyServer.getChannels().get(applicationId);

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
}