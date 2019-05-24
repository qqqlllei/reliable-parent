package com.reliable.message.server.netty;

import com.reliable.message.common.netty.NamedThreadFactory;
import com.reliable.message.server.datasource.DataBaseManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 李雷 on 2019/5/24.
 */
@Component
@Slf4j
public class NettyHttpServer {


    @Autowired
    private DataBaseManager dataBaseManager;
    /**
     * boss 线程组用于处理连接工作
     */
    private EventLoopGroup boss = new NioEventLoopGroup(1,
            new NamedThreadFactory("NettyWebBoss", 1));
    /**
     * work 线程组用于数据处理
     */
    private EventLoopGroup work =  new NioEventLoopGroup(6,
            new NamedThreadFactory("NettyWebNIOWorker", 6));

    private Integer httpPort = 8080 ;

    private static final ThreadPoolExecutor WORKING_THREADS = new ThreadPoolExecutor(5,
            10, 500, TimeUnit.SECONDS,
            new LinkedBlockingQueue(15),
            new NamedThreadFactory("ServerHttpHandlerThread", 15), new ThreadPoolExecutor.CallerRunsPolicy());

    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss,work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(httpPort))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast("httpDecode", new HttpRequestDecoder());
                        pipeline.addLast("httpEncode", new HttpResponseEncoder());
                        //对http进行聚合，设置最大聚合字节长度为10M
                        pipeline.addLast(new HttpObjectAggregator(10 * 1024 * 1024));
                        pipeline.addLast(new HttpContentCompressor());
                        //添加自定义处理器
                        pipeline.addLast(new ServerHttpHandler(dataBaseManager,WORKING_THREADS));
                    }
                });
        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            log.info("启动 Netty Web");
        }
    }
}
