package com.reliable.message.server.netty;

import com.reliable.message.common.netty.MessageCodecHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by 李雷 on 2019/4/29.
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        //IdleStateHandler心跳机制,如果超时触发Handle中userEventTrigger()方法
        pipeline.addLast("idleStateHandler",
                new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));

//        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
//        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));

        pipeline.addLast("messageCodec", new MessageCodecHandler());

        //自定义Hadler
        pipeline.addLast("handler",new TCPServerHandler());
    }
}
