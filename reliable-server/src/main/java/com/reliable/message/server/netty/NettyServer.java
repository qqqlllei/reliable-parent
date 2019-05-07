package com.reliable.message.server.netty;

import com.reliable.message.common.netty.message.ResponseMessage;
import com.reliable.message.common.netty.message.WaitConfirmCheckRequest;
import com.reliable.message.common.wrapper.Wrapper;
import com.reliable.message.server.constant.MessageConstant;
import com.reliable.message.server.datasource.DataBaseManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by 李雷 on 2019/4/29.
 */
@Component
public class NettyServer implements MessageProtocol{

    private static Logger logger = LoggerFactory.getLogger(NettyServer.class);


    @Autowired
    private DataBaseManager dataBaseManager;


    private TCPServerHandler tcpServerHandler;


    private final ConcurrentMap<String, ConcurrentMap<String,Channel>> channels = new ConcurrentHashMap<>();


    /**
     * boss 线程组用于处理连接工作
     */
    private EventLoopGroup boss = new NioEventLoopGroup(1);
    /**
     * work 线程组用于数据处理
     */
    private EventLoopGroup work = new NioEventLoopGroup(6);
    @Value("${netty.server.port}")
    private Integer port;
    /**
     * 启动Netty Server
     *
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        tcpServerHandler = new TCPServerHandler(this);
        bootstrap.group(boss, work)
                // 指定Channel
                .channel(NioServerSocketChannel.class)
                //使用指定的端口设置套接字地址
                .localAddress(new InetSocketAddress(port))

                //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                .option(ChannelOption.SO_BACKLOG, 1024)

                //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, true)

                //将小的数据包包装成更大的帧进行传送，提高网络的负载,即TCP延迟传输
                .childOption(ChannelOption.TCP_NODELAY, true)

                .childHandler(new ServerChannelInitializer(tcpServerHandler));
        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            logger.info("启动 Netty Server");
        }
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        work.shutdownGracefully().sync();
        logger.info("关闭Netty");
    }

    public DataBaseManager getDataBaseManager() {
        return dataBaseManager;
    }

    public TCPServerHandler getTcpServerHandler() {
        return tcpServerHandler;
    }

    public void setTcpServerHandler(TCPServerHandler tcpServerHandler) {
        this.tcpServerHandler = tcpServerHandler;
    }

    @Override
    public String getClientMessageDataByProducerMessageId(String producerGroup, String producerMessageId) throws Exception {
        WaitConfirmCheckRequest waitConfirmCheckRequest = new WaitConfirmCheckRequest();
        waitConfirmCheckRequest.setProducerGroup(producerGroup);
        waitConfirmCheckRequest.setId(producerMessageId);
        Channel channel = tcpServerHandler.getChannel(producerGroup);
        if(channel == null){
            logger.error("服务："+producerGroup+" 没有启动");
            return MessageConstant.CLIENT_SERVER_DOWN;
        }
        ResponseMessage object = (ResponseMessage) tcpServerHandler.sendMessage(waitConfirmCheckRequest,channel);
        if(Wrapper.SUCCESS_CODE == object.getResultCode()){
            return MessageConstant.CLIENT_TRANSACTION_OK;
        }

        return MessageConstant.CLIENT_TRANSACTION_ERROR;
    }

    public ConcurrentMap<String, ConcurrentMap<String, Channel>> getChannels() {
        return channels;
    }
}
