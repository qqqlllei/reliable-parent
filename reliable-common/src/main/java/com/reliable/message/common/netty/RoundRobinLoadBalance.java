package com.reliable.message.common.netty;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 李雷 on 2019/5/6.
 */

public class RoundRobinLoadBalance implements ChannelLoadBalance {

    private static AtomicInteger index = new AtomicInteger(0);
    @Override
    public <T> T doSelect(List<T> list) {

        if (index.get() >= list.size()) {
            index = new AtomicInteger(0);
        }
        int a = index.get();
        T obj = list.get(a);
        index.incrementAndGet();
        return obj;
    }
}
