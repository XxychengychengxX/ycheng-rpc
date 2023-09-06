/**
 * @author Valar Morghulis
 * @Date 2023/9/6
 */
package com.ychengycheng.core.loadbalancer;

import java.net.InetSocketAddress;

public interface LoadBalancer {

    /**
     * 根据服务名获取一个可用服务
     * @param serviceName 服务名称
     * @return 服务地址的InetSocketAddress对象
     */
    InetSocketAddress selectServiceAddress(String serviceName);
}
