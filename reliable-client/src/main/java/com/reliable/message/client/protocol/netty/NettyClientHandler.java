package com.reliable.message.client.protocol.netty;

import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.enums.MessageSendTypeEnum;
import com.reliable.message.common.netty.RoundRobinLoadBalance;
import com.reliable.message.common.netty.message.Message;
import com.reliable.message.common.netty.message.ResponseMessage;
import com.reliable.message.common.netty.message.WaitConfirmCheckRequest;
import com.reliable.message.common.netty.rpc.AbstractRpcHandler;
import com.reliable.message.common.wrapper.Wrapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by 李雷 on 2019/4/29.
 */
@Sharable
public class NettyClientHandler extends AbstractRpcHandler {
    private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private NettyClient nettyClient;
    private RoundRobinLoadBalance roundRobinLoadBalance;
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public NettyClientHandler(NettyClient nettyClient){
        this.nettyClient = nettyClient;
        nettyClient.setNettyClientHandler(this);
        this.roundRobinLoadBalance = new RoundRobinLoadBalance();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("关闭连接时：" + new Date());
//        final EventLoop eventLoop = ctx.channel().eventLoop();
//
//        nettyClient.doConnect(ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info(cause.getMessage()+"--"+ctx.channel().toString());
        ctx.disconnect();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                Message message = new Message();
                message.setMessageType(MessageSendTypeEnum.PING);
                ctx.channel().writeAndFlush(message);
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,
                             Object msg) throws Exception {

        try {

            if(msg instanceof WaitConfirmCheckRequest){
                WaitConfirmCheckRequest waitConfirmCheckRequest = (WaitConfirmCheckRequest) msg;
                ClientMessageData clientMessageData =nettyClient.getReliableMessageService().
                        getClientMessageDataByProducerMessageId(waitConfirmCheckRequest.getId());
                if(clientMessageData !=null){
                    ResponseMessage responseMessage = new ResponseMessage();
                    responseMessage.setId(clientMessageData.getId());
                    responseMessage.setResultCode(Wrapper.SUCCESS_CODE);
                    responseMessage.setMessageType(MessageSendTypeEnum.WAIT_CONFIRM);
                    ctx.writeAndFlush(responseMessage);
                }

                return;

            }
            super.channelRead(ctx,msg);


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public ArrayList<Channel> getChannels(String applicationId) {
        return new ArrayList<>(channels.values());
    }

    public ConcurrentMap<String, Channel> getChannels() {
        return channels;
    }
}
