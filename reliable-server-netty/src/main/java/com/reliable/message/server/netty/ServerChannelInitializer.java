package com.reliable.message.server.netty;

import com.reliable.message.common.netty.MessageCodecHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by 李雷 on 2019/4/29.
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("idleStateHandler",
                new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast("messageCodec", new MessageCodecHandler());

        pipeline.addLast("handler",new TCPServerHandler());
    }
}
