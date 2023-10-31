/**
 * @author Valar Morghulis
 * @Date 2023/9/6
 */
package com.ychengycheng.core.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.List;

public interface LoadBalancer {

    /**
     * 根据服务名获取一个可用服务
     * @param serviceName 服务名称
     * @return 服务地址的InetSocketAddress对象
     */
    InetSocketAddress selectServiceAddress(String serviceName);

    /**
     * 感知节点发生动态上下线，重新进行负载均衡
     * @param serviceName 服务名称
     * @param lookup 节点列表
     */
    void reLoadBalance(String serviceName, List<Instance> lookup);
}
