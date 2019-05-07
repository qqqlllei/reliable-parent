package com.reliable.message.common.netty.rpc;

import com.reliable.message.common.netty.MessageFuture;
import com.reliable.message.common.netty.message.RequestMessage;
import com.reliable.message.common.netty.message.ResponseMessage;
import com.reliable.message.common.wrapper.Wrapper;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by 李雷 on 2019/5/6.
 */
public abstract class AbstractRpcHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AbstractRpcHandler.class);
    protected final ConcurrentHashMap<String, MessageFuture> futures = new ConcurrentHashMap<>();


    public Object sendMessage(RequestMessage requestMessage, Channel channel) throws TimeoutException {

        if(requestMessage.isSyncFlag()){
            final MessageFuture messageFuture = new MessageFuture();
            messageFuture.setRequestMessage(requestMessage);
            futures.put(requestMessage.getId(), messageFuture);
            ChannelFuture future;
            future = channel.writeAndFlush(requestMessage);
            future.addListener((ChannelFutureListener) future1 -> {
                if (!future1.isSuccess()) {
                    MessageFuture messageFuture1 = futures.remove(requestMessage.getId());
                    if (messageFuture1 != null) {
                        messageFuture1.setResultMessage(future1.cause());
                    }
                }
            });
            try {
                return messageFuture.get(30 * 1000L, TimeUnit.MILLISECONDS);
            } catch (Exception exx) {
                logger.error("wait response error:" + exx.getMessage() + ",ip:" + "127" + ",request:" + requestMessage);
                if (exx instanceof TimeoutException) {
                    throw (TimeoutException) exx;
                } else {
                    throw new RuntimeException(exx);
                }
            }
        }else {
            channel.writeAndFlush(requestMessage);
            return Wrapper.ok();
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof ResponseMessage){
            ResponseMessage responseMessage = (ResponseMessage) msg;
            MessageFuture messageFuture = futures.remove(responseMessage.getId());
            if(messageFuture !=null){
                messageFuture.setResultMessage(responseMessage);
            }

            return;
        }



    }
}
