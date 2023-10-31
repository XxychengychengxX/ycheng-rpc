package com.ychengycheng.core.loadbalancer.impl;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.core.loadbalancer.AbstractLoadBalancer;
import com.ychengycheng.core.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡策略
 *
 * @author Valar Morghulis
 * @Date 2023/9/6
 */
@Slf4j
public class RoundRobinBalancer extends AbstractLoadBalancer {


    /**
     * 由子类进行扩展
     *
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    @Override
    protected Selector getSelector(List<Instance> serviceList) {
        return new RoundRobinSelector(serviceList);
    }


    private static class RoundRobinSelector implements Selector {

        private final List<Instance> instanceList;
        private final AtomicInteger index;

        public RoundRobinSelector(List<Instance> instanceList) {
            this.instanceList = instanceList;
            this.index = new AtomicInteger(0);
        }

        /**
         * 根据服务列表执行一种算法获取一种服务
         *
         * @return ip +port
         */
        @Override
        public InetSocketAddress getNext() {
            if (instanceList == null || instanceList.size() == 0) {
                throw new RuntimeException("selector检测到服务列表为空！！");
            }
            Instance instance = instanceList.get(index.get() % instanceList.size());

            index.incrementAndGet();

            String ip = instance.getIp();
            int port = instance.getPort();

            return new InetSocketAddress(ip, port);

        }

        @Override
        public void reBalence() {

        }
    }
}
