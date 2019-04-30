package com.reliable.message.server.netty;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class TCPServerHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(TCPServerHandler.class);

    /** 空闲次数 */
    private AtomicInteger idle_count = new AtomicInteger(1);
    /** 发送次数 */
    private AtomicInteger count = new AtomicInteger(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        logger.info("连接的客户端地址:" + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {
           if(msg instanceof String){
               String messageBody = (String) msg;
               logger.info(messageBody);
               JSONObject messageInfo = JSONObject.parseObject(messageBody);
               if("request".equals(messageInfo.getString("type"))){
                   messageInfo.put("type","response");
                   messageInfo.put("code","success");
                   ctx.channel().writeAndFlush(messageInfo+"\n");
               }

           }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
        count.getAndIncrement();
    }
    
    //检测到空闲连接，触发
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {

        logger.info("===========================userEventTriggered============================"+this.getClass()+"======="+Thread.currentThread().getName());
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果读通道处于空闲状态，说明没有接收到心跳命令
            if (IdleState.READER_IDLE.equals(event.state())) {
                logger.info("已经5秒没有接收到客户端的信息了");
                if (idle_count.get() > 1) {
                    logger.info("关闭这个不活跃的channel");
                    ctx.channel().close();
                }
                idle_count.getAndIncrement();
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}