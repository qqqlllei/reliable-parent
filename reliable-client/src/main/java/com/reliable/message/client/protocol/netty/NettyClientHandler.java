package com.reliable.message.client.protocol.netty;

import com.reliable.message.common.netty.ClientRegisterRequest;
import com.reliable.message.common.netty.Message;
import com.reliable.message.common.netty.RequestMessage;
import com.reliable.message.common.netty.ResponseMessage;
import com.reliable.message.common.wrapper.Wrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 李雷 on 2019/4/29.
 */
@Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<Message> {
    private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private NettyClient nettyClient;


    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, MessageFuture> futures = new ConcurrentHashMap<>();


    public NettyClientHandler(){

    }

    public NettyClientHandler(NettyClient nettyClient){
        this.nettyClient = nettyClient;
        nettyClient.setNettyClientHandler(this);
    }


    /** 循环次数 */
    private AtomicInteger fcount = new AtomicInteger(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("建立连接时：" + new Date()+"clientChannelActive==="+ctx.channel().remoteAddress());
        channels.put(ctx.channel().remoteAddress().toString(),ctx.channel());


        ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
        clientRegisterRequest.setApplicationId(nettyClient.getApplicationId());
        clientRegisterRequest.setSyncFlag(true);

        ctx.writeAndFlush(clientRegisterRequest);

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("关闭连接时：" + new Date());
        final EventLoop eventLoop = ctx.channel().eventLoop();
        nettyClient.doConnect(new Bootstrap(), eventLoop);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                Message message = new Message();
                message.setMessageType(0);
                ctx.channel().writeAndFlush(message);
                fcount.getAndIncrement();
            }
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             Message msg) throws Exception {
        if(msg instanceof ResponseMessage){
            ResponseMessage responseMessage = (ResponseMessage) msg;
            MessageFuture messageFuture = futures.remove(responseMessage.getId());
            if(messageFuture !=null){
                messageFuture.setResultMessage(responseMessage);
            }
        }

    }


    public Object sendMessage(RequestMessage message) throws TimeoutException {
        Channel channel = null;
        Iterator<String> channelIterator = channels.keySet().iterator();

        while (channelIterator.hasNext()) {
            String channelKey = channelIterator.next();
            channel = channels.get(channelKey);
            break;
        }

        if (channel != null && message.isSyncFlag()) {
            final MessageFuture messageFuture = new MessageFuture();
            messageFuture.setRequestMessage(message);
            futures.put(message.getId(), messageFuture);
            ChannelFuture future;
            future = channel.writeAndFlush(message);
            future.addListener((ChannelFutureListener) future1 -> {
                if (!future1.isSuccess()) {
                    MessageFuture messageFuture1 = futures.remove(message.getId());
                    if (messageFuture1 != null) {
                        messageFuture1.setResultMessage(future1.cause());
                    }
                }
            });
            try {
                return messageFuture.get(30 * 1000L, TimeUnit.MILLISECONDS);
            } catch (Exception exx) {
                logger.error("wait response error:" + exx.getMessage() + ",ip:" + "127" + ",request:" + message);
                if (exx instanceof TimeoutException) {
                    throw (TimeoutException) exx;
                } else {
                    throw new RuntimeException(exx);
                }
            }
        }else {
            channel.writeAndFlush(message);
            return Wrapper.ok();
        }
    }



}
