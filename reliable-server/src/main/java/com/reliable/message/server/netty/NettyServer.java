package com.reliable.message.server.netty;

import com.reliable.message.common.discovery.RegistryFactory;
import com.reliable.message.common.netty.NamedThreadFactory;
import com.reliable.message.server.datasource.DataBaseManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 李雷 on 2019/4/29.
 */
@Component
@Slf4j
public class NettyServer {

    @Autowired
    private DataBaseManager dataBaseManager;
    private ServerRpcHandler serverRpcHandler;
    /**
     * boss 线程组用于处理连接工作
     */
    private EventLoopGroup boss = new NioEventLoopGroup(1,
            new NamedThreadFactory("NettyBoss", 1));
    /**
     * work 线程组用于数据处理
     */
    private EventLoopGroup work =  new NioEventLoopGroup(6,
            new NamedThreadFactory("NettyServerNIOWorker", 6));




    private static final int MIN_SERVER_POOL_SIZE = 100;
    private static final int MAX_SERVER_POOL_SIZE = 500;
    private static final int MAX_TASK_QUEUE_SIZE = 20000;
    private static final int KEEP_ALIVE_TIME = 500;
    private static final ThreadPoolExecutor WORKING_THREADS = new ThreadPoolExecutor(MIN_SERVER_POOL_SIZE,
            MAX_SERVER_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue(MAX_TASK_QUEUE_SIZE),
            new NamedThreadFactory("ServerHandlerThread", MAX_SERVER_POOL_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());



    @Value("${netty.server.port}")
    private Integer port;

    @Value("${netty.server.ip}")
    private String ip;
    /**
     * 启动Netty Server
     *
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        serverRpcHandler = new ServerRpcHandler(this,WORKING_THREADS);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        bootstrap.group(boss, work)
                // 指定Channel
                .channel(NioServerSocketChannel.class)
                //使用指定的端口设置套接字地址
                .localAddress(inetSocketAddress)

                //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                .option(ChannelOption.SO_BACKLOG, 1024)

                //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, true)

                //将小的数据包包装成更大的帧进行传送，提高网络的负载,即TCP延迟传输
                .childOption(ChannelOption.TCP_NODELAY, true)

                .childHandler(new ServerChannelInitializer(serverRpcHandler));
        try {
            ChannelFuture future = bootstrap.bind().sync();
            if (future.isSuccess()) {
                log.info("启动 Netty Server");
            }
            RegistryFactory.getInstance(RegistryFactory.DEFAULT_REGISTER).register(ip,port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        work.shutdownGracefully().sync();
        log.info("关闭Netty");
    }

    public DataBaseManager getDataBaseManager() {
        return dataBaseManager;
    }

    public void setServerRpcHandler(ServerRpcHandler serverRpcHandler) {
        this.serverRpcHandler = serverRpcHandler;
    }

    public ServerRpcHandler getServerRpcHandler() {
        return serverRpcHandler;
    }
}
