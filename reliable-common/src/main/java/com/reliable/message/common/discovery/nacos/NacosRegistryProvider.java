package com.reliable.message.common.discovery.nacos;


import com.reliable.message.common.discovery.RegistryProvider;
import com.reliable.message.common.discovery.RegistryService;

/**
 * Created by 李雷 on 2019/5/7.
 */
public class NacosRegistryProvider implements RegistryProvider {
    @Override
    public RegistryService provide() {
        return NacosRegistryServiceImpl.getInstance();
    }
}
