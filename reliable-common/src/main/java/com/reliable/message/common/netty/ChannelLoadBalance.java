package com.reliable.message.common.netty;

import io.netty.channel.Channel;

import java.util.List;

/**
 * Created by 李雷 on 2019/5/6.
 */
public interface ChannelLoadBalance {


    Channel doSelect(List<Channel> channels);
}
