package com.reliable.message.common.netty;

import java.util.List;

/**
 * Created by 李雷 on 2019/5/6.
 */
public interface ChannelLoadBalance {


    <T> T doSelect(List<T> channels);
}
