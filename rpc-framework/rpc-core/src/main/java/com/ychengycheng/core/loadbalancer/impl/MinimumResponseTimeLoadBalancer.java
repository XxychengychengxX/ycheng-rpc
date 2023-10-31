package com.ychengycheng.core.loadbalancer.impl;


import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.core.loadbalancer.AbstractLoadBalancer;
import com.ychengycheng.core.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 最短响应时间的负载均衡策略
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {



    /**
     * 由子类进行扩展
     *
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    @Override
    protected Selector getSelector(List<Instance> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector {
        
        public MinimumResponseTimeSelector(List<Instance> serviceList) {
        
        }
        
        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = YchengYchengRPCBootstrap.ANSWER_TIME_TREEMAP.firstEntry();
            if (entry != null) {
                if (log.isDebugEnabled()){
                    log.debug("选取了响应时间为【{}ms】的服务节点.",entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }
            
            // 直接从缓存中获取一个可用的就行了
            log.info("----->"+Arrays.toString(YchengYchengRPCBootstrap.CHANNEL_CACHE.values().toArray()));
            Channel channel = (Channel)YchengYchengRPCBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress)channel.remoteAddress();
        }

        @Override
        public void reBalence() {

        }

    }
}
