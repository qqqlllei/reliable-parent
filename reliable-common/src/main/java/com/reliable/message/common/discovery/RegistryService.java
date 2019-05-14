package com.reliable.message.common.discovery;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by 李雷 on 2019/5/7.
 */
public interface RegistryService<T> {

    void register(String ip,int port) throws Exception;

    void unregister(String ip,int port) throws Exception;

    void subscribe(String cluster, T listener) throws Exception;

    void unsubscribe(String cluster, T listener) throws Exception;

    List<InetSocketAddress> lookup(String key) throws Exception;

    void close() throws Exception;
}
