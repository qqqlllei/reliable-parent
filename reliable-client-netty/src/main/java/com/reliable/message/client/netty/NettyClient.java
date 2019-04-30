package com.reliable.message.client.netty;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by 李雷 on 2019/4/29.
 */
@Service("nettyClient")
public class NettyClient {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);
    @Value("${netty.server.ip}")
    private String host;

    @Value("${netty.server.port}")
    private Integer port;

    /**唯一标记 */
    private boolean initFalg = true;


    private NettyClientHandler nettyClientHandler;

    private EventLoopGroup group;
    private ChannelFuture f;


    @PostConstruct
    public void init() {
        group = new NioEventLoopGroup();
        doConnect(new Bootstrap(), group);
    }

    public void doConnect(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {
        try {
            if (bootstrap != null) {
                bootstrap.group(eventLoopGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.handler(new NettyClientInitializer(this));
                bootstrap.remoteAddress(host, port);
                f = bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    final EventLoop eventLoop = futureListener.channel().eventLoop();
                    if (!futureListener.isSuccess()) {
                        logger.info("与服务端断开连接!在10s之后准备尝试重连!");
                        eventLoop.schedule(() -> doConnect(new Bootstrap(), eventLoop), 10, TimeUnit.SECONDS);
                    }
                });
                if(initFalg){
                    logger.info("Netty客户端启动成功!");
                    initFalg=false;
                }
            }
        } catch (Exception e) {
            logger.info("客户端连接失败!"+e.getMessage());
        }

    }

    public void setNettyClientHandler(NettyClientHandler nettyClientHandler) {
        this.nettyClientHandler = nettyClientHandler;
    }

    public NettyClientHandler getNettyClientHandler() {
        return nettyClientHandler;
    }
}
