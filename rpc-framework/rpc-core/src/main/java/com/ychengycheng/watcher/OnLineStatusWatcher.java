package com.ychengycheng.watcher;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.channel.initializer.NettyBootstrapInitializer;
import com.ychengycheng.core.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Valar Morghulis
 * @Date 2023/9/26
 */
@Slf4j
public class OnLineStatusWatcher implements EventListener {
    /**
     * callback event.
     *
     * @param event event
     */
    @Override
    public void onEvent(Event event) {
        if (event instanceof NamingEvent) {
            if (log.isDebugEnabled()) {
                log.debug("Service onLine Status changed detected , flush channel cache later....");
            }

            String serviceName = ((NamingEvent) event).getServiceName();
            List<Instance> instances = ((NamingEvent) event).getInstances();
            synchronized (YchengYchengRPCBootstrap.CHANNEL_CACHE) {
                log.debug("OnLineStatusWatcher gets the key [YchengYchengRPCBootstrap.CHANNEL_CACHE]...");
                YchengYchengRPCBootstrap.CHANNEL_CACHE.clear();
                instances.forEach(instance -> {
                    InetSocketAddress inetSocketAddress = new InetSocketAddress(instance.getIp(), instance.getPort());
                    try {
                        Channel channel = NettyBootstrapInitializer.getBootstrap()
                                                                   .connect(inetSocketAddress)
                                                                   .sync()
                                                                   .channel();
                        YchengYchengRPCBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                log.debug("OnLineStatusWatcher releash the key [YchengYchengRPCBootstrap.CHANNEL_CACHE]...");
            }

            LoadBalancer loadBalancer = YchengYchengRPCBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName, instances);
        }
    }
}
