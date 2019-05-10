package com.reliable.message.client.protocol.netty;

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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by 李雷 on 2019/4/29.
 */
@Sharable
public class ClientRpcHandler extends AbstractRpcHandler {
    private static Logger logger = LoggerFactory.getLogger(ClientRpcHandler.class);
    private NettyClient nettyClient;
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    private ThreadPoolExecutor messageExecutor;

    public ClientRpcHandler(NettyClient nettyClient,ThreadPoolExecutor messageExecutor){
        this.nettyClient = nettyClient;
        this.messageExecutor = messageExecutor;
        nettyClient.setClientRpcHandler(this);
        super.roundRobinLoadBalance = new RoundRobinLoadBalance();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("关闭连接时：" + new Date());
        nettyClient.doConnect((InetSocketAddress) ctx.channel().remoteAddress());
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

                messageExecutor.execute(() -> {
                    WaitConfirmCheckRequest waitConfirmCheckRequest = (WaitConfirmCheckRequest) msg;
                    ResponseMessage responseMessage = new ResponseMessage();
                    responseMessage.setId(waitConfirmCheckRequest.getId());
                    responseMessage.setMessageType(MessageSendTypeEnum.WAIT_CONFIRM);
                    if(nettyClient.getReliableMessageService().hasProducedMessage(waitConfirmCheckRequest.getId())){
                        responseMessage.setResultCode(Wrapper.SUCCESS_CODE);
                    }
                    ctx.writeAndFlush(responseMessage);
                });
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
    public Collection<Channel> getAllChannels(String applicationId) {
        Collection<Channel> channelCollection = channels.values();
        if(channelCollection.size() == 0){
            this.nettyClient.connect();
            return channels.values();
        }
        return channelCollection;
    }

    public ConcurrentMap<String, Channel> getChannels() {
        return channels;
    }
}
