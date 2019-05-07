package com.reliable.message.common.netty;

import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 李雷 on 2019/5/6.
 */

public class RoundRobinLoadBalance implements ChannelLoadBalance {

    private static AtomicInteger index = new AtomicInteger(0);
    @Override
    public Channel doSelect(List<Channel> channels) {

        if (index.get() >= channels.size()) {
            index = new AtomicInteger(0);
        }
        Channel channel = channels.get(index.get());
        index.incrementAndGet();
        return channel;
    }
}
