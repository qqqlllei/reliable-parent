package com.reliable.message.server.netty;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.server.constant.MessageConstant;
import com.reliable.message.server.datasource.DataBaseManager;
import com.reliable.message.server.domain.MessageConsumer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by 李雷 on 2019/5/24.
 */
@Sharable
@Slf4j
public class ServerHttpHandler extends ChannelInboundHandlerAdapter{

    private DataBaseManager dataBaseManager;

    private ThreadPoolExecutor executor;

    public ServerHttpHandler(DataBaseManager dataBaseManager,ThreadPoolExecutor executor){
        this.dataBaseManager = dataBaseManager;
        this.executor = executor ;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("已经获取到客户端连接......");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        log.info("已经获取到客户端 request......" + request);
        String uri = request.uri();
        String[] requestInfo =  uri.split("/");
        String path="";
        if(requestInfo.length > 0 ){
            path=requestInfo[1];
        }

        switch (path){
            case MessageConstant.GET_CONSUMERS_BY_TOPIC:

                if(requestInfo.length >1){
                    String topic = requestInfo[2];
                    executor.execute(() -> {
                        List<MessageConsumer>  messageConsumers = dataBaseManager.getConsumersByTopic(topic);
                        sendMessage(ctx,JSONObject.toJSONString(messageConsumers));
                    });
                }

                break;
            default:
                sendMessage(ctx,"不合法路径！");
                break;
        }
    }


    private void sendMessage(ChannelHandlerContext ctx, String msg) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
