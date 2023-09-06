package com.ychengycheng.core.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.YchengYchengRPCBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractLoadBalancer implements LoadBalancer {

    // 一个服务会匹配一个selector


    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {

        // 1、优先从cache中获取一个选择器
        Selector selector = cache.get(serviceName);

        // 2、如果没有，就需要为这个service创建一个selector
        if (selector == null) {
            // 对于这个负载均衡器，内部应该维护服务列表作为缓存
            List<Instance> instanceList = YchengYchengRPCBootstrap.getInstance()
                                                                  .getRegistryCenter()
                                                                  .lookup(serviceName);

            // 提供一些算法负责选取合适的节点
            selector = getSelector(instanceList);

            // 将select放入缓存当中
            cache.put(serviceName, selector);
        }

        // 获取可用节点
        return selector.getNext();
    }


    /**
     * 由子类进行扩展
     *
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<Instance> serviceList);

}
