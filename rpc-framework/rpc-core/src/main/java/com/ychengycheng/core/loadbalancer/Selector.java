/**
 * @author Valar Morghulis
 * @Date 2023/9/6
 */
package com.ychengycheng.core.loadbalancer;

import java.net.InetSocketAddress;

public interface Selector {
    /**
     * 根据服务列表执行一种算法获取一种服务
     *
     * @return
     */
    InetSocketAddress getNext();

    //todo:服务动态上下线感知
    void reBalence();
}
