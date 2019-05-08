package com.reliable.message.common.discovery;

import com.reliable.message.common.discovery.nacos.NacosRegistryProvider;

/**
 * Created by 李雷 on 2019/5/8.
 */
public class RegistryFactory {

    private static final String NACOS_REGISTER="nacos_register";


    private static final String ZOOKEEPER_REGISTER="zookeeper_register";

    public static RegistryService getInstance(String registerType){


        switch (registerType){
            case ZOOKEEPER_REGISTER:
                return new NacosRegistryProvider().provide();
            default:
                return new NacosRegistryProvider().provide();
        }
    }
}
