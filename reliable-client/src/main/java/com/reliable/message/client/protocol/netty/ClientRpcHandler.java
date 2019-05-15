package com.reliable.message.client.protocol.netty;

import com.reliable.message.common.discovery.RegistryFactory;
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
import java.util.Date;
import java.util.List;
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
        ctx.disconnect();
        ctx.close();
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
                    if(nettyClient.getReliableMessageService().hasProducedMessage(waitConfirmCheckRequest.getId())){
                        responseMessage.setResultCode(Wrapper.SUCCESS_CODE);
                    }else{
                        responseMessage.setResultCode(Wrapper.WITHOUT_MESSAGE);
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

    public ConcurrentMap<String, Channel> getChannels() {

        logger.info("===========================clientChannels size = " + channels.size());
        return channels;
    }


    public InetSocketAddress loadBalance(){
        InetSocketAddress address;
        try {
            List<InetSocketAddress> inetSocketAddresses =  RegistryFactory.getInstance("nacos_register").
                    lookup("");
            address = roundRobinLoadBalance.doSelect(inetSocketAddresses);
            return address;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
