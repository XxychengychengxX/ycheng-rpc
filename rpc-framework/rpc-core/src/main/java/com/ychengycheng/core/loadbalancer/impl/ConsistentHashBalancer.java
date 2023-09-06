package com.ychengycheng.core.loadbalancer.impl;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.core.loadbalancer.AbstractLoadBalancer;
import com.ychengycheng.core.loadbalancer.Selector;
import com.ychengycheng.message.YchengRpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 轮询负载均衡策略
 *
 * @author Valar Morghulis
 * @Date 2023/9/6
 */
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {


    /**
     * 由子类进行扩展
     *
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    @Override
    protected Selector getSelector(List<Instance> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    /**
     * 一致性hash的具体算法实现
     */
    private static class ConsistentHashSelector implements Selector {
        //hash环用来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        //虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashSelector(List<Instance> instanceList, int virtualNodes) {
            this.virtualNodes = virtualNodes;
            for (Instance instance : instanceList) {
                //将每个节点加入hash中
                String ip = instance.getIp();
                int port = instance.getPort();
                InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
                addNode2Circle(inetSocketAddress);

            }
        }


        /**
         * 根据服务列表执行一种算法获取一种服务
         *
         * @return ip +port
         */
        @Override
        public InetSocketAddress getNext() {
            //1.hash环已经建立好了，接下来需要选择一个要素处理hash
            YchengRpcRequest ychengRpcRequest = YchengYchengRPCBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = String.valueOf(ychengRpcRequest.getRequestId());
            //对请求的id做hash操作
            int hash = hash(requestId);
            //判断hash落在哪个服务器
            if (!circle.containsKey(hash)) {
                //寻找最近的一个节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            return circle.get(hash);
        }

        @Override
        public void reBalence() {

        }

        /**
         * 将每个节点挂载到hash环上
         *
         * @param inetSocketAddress 节点
         */
        private void addNode2Circle(InetSocketAddress inetSocketAddress) {
            //为每个节点生成虚拟节点并挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                log.debug("hash为[{}]的节点已经挂载到了哈希环上.",hash);
            }
        }

        /**
         * 删除节点
         * @param inetSocketAddress 节点
         */
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.remove(hash);
            }
        }

        /**
         * 具体的hash算法
         *
         * @param s
         * @return
         */
        private int hash(String s) {
            MessageDigest messageDigest;
            try {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = messageDigest.digest(s.getBytes());
            //得到一个数组，但是想要int 4个字节
            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if (digest[i] < 0) {
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }

            }
            return res;
        }
    }
}
