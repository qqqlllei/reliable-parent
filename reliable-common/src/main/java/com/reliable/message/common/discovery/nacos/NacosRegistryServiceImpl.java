package com.reliable.message.common.discovery.nacos;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.reliable.message.common.discovery.RegistryService;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by 李雷 on 2019/5/7.
 */
public class NacosRegistryServiceImpl implements RegistryService<EventListener> {
    private static final String DEFAULT_NAMESPACE = "public";
    private static final String PRO_SERVER_ADDR_KEY = "serverAddr";
    private static final String APPLICATION_NAME = "reliableMessage";
    private static final String DEFAULT_CLUSTER = "reliableMessageCluster";
    private static final String PRO_NAMESPACE_KEY = "namespace";
    private static volatile NamingService naming;
    private static final ConcurrentMap<String, List<EventListener>> LISTENER_SERVICE_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, List<InetSocketAddress>> CLUSTER_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static volatile NacosRegistryServiceImpl instance;

    private NacosRegistryServiceImpl() {
    }

    static NacosRegistryServiceImpl getInstance() {
        if (null == instance) {
            synchronized (NacosRegistryServiceImpl.class) {
                if (null == instance) {
                    instance = new NacosRegistryServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void register(InetSocketAddress address) throws Exception {
        validAddress(address);
        getNamingInstance().registerInstance(APPLICATION_NAME, address.getAddress().getHostAddress(), address.getPort(), getClusterName());
    }

    @Override
    public void unregister(InetSocketAddress address) throws Exception {
        validAddress(address);
        getNamingInstance().deregisterInstance(APPLICATION_NAME, address.getAddress().getHostAddress(), address.getPort(), getClusterName());
    }

    @Override
    public void subscribe(String cluster, EventListener listener) throws Exception {
        List<String> clusters = new ArrayList<>();
        clusters.add(cluster);
        LISTENER_SERVICE_MAP.putIfAbsent(cluster, new ArrayList<>());
        LISTENER_SERVICE_MAP.get(cluster).add(listener);
        getNamingInstance().subscribe(APPLICATION_NAME, clusters, listener);
    }

    @Override
    public void unsubscribe(String cluster, EventListener listener) throws Exception {
        List<String> clusters = new ArrayList<>();
        clusters.add(cluster);
        List<EventListener> subscribeList = LISTENER_SERVICE_MAP.get(cluster);
        if (null != subscribeList) {
            List<EventListener> newSubscribeList = new ArrayList<>();
            for (EventListener eventListener : subscribeList) {
                if (!eventListener.equals(listener)) {
                    newSubscribeList.add(eventListener);
                }
            }
            LISTENER_SERVICE_MAP.put(cluster, newSubscribeList);
        }
        getNamingInstance().unsubscribe(APPLICATION_NAME, clusters, listener);
    }

    @Override
    public List<InetSocketAddress> lookup(String key) throws Exception {


        if(!LISTENER_SERVICE_MAP.containsKey(DEFAULT_CLUSTER)){
            List<String> clusters = new ArrayList<>();
            clusters.add(DEFAULT_CLUSTER);

            List<Instance> firstAllInstances = getNamingInstance().getAllInstances(APPLICATION_NAME, clusters);

            if (null != firstAllInstances) {
                List<InetSocketAddress> newAddressList = new ArrayList<>();
                for (Instance instance : firstAllInstances) {
                    if (instance.isEnabled() && instance.isHealthy()) {
                        newAddressList.add(new InetSocketAddress(instance.getIp(), instance.getPort()));
                    }
                }
                CLUSTER_ADDRESS_MAP.put(DEFAULT_CLUSTER, newAddressList);
            }


            subscribe(DEFAULT_CLUSTER, event -> {
                List<Instance> instances = ((NamingEvent) event).getInstances();
                if (null == instances && null != CLUSTER_ADDRESS_MAP.get(DEFAULT_CLUSTER)) {
                    CLUSTER_ADDRESS_MAP.remove(DEFAULT_CLUSTER);
                } else if (!CollectionUtils.isEmpty(instances)) {
                    List<InetSocketAddress> newAddressList = new ArrayList<>();
                    for (Instance instance : instances) {
                        if (instance.isEnabled() && instance.isHealthy()) {
                            newAddressList.add(new InetSocketAddress(instance.getIp(), instance.getPort()));
                        }
                    }
                    CLUSTER_ADDRESS_MAP.put(DEFAULT_CLUSTER, newAddressList);


                }
            });
        }
        return CLUSTER_ADDRESS_MAP.get(DEFAULT_CLUSTER);
    }

    @Override
    public void close() throws Exception {

    }

    private void validAddress(InetSocketAddress address) {
        if (null == address.getHostName() || 0 == address.getPort()) {
            throw new IllegalArgumentException("invalid address:" + address);
        }
    }

    public static NamingService getNamingInstance() throws Exception {
        if (null == naming) {
            synchronized (NacosRegistryServiceImpl.class) {
                if (null == naming) {
                    naming = NamingFactory.createNamingService(getNamingProperties());
                }
            }
        }
        return naming;
    }

    private static Properties getNamingProperties() {
        Properties properties = new Properties();
        properties.setProperty(PRO_SERVER_ADDR_KEY, "10.33.80.101");
        properties.setProperty(PRO_NAMESPACE_KEY, DEFAULT_NAMESPACE);
        return properties;
    }

    private static String getClusterName() {
        return DEFAULT_CLUSTER;
    }
}
