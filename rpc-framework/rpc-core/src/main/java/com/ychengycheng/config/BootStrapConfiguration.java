package com.ychengycheng.config;

import com.ychengycheng.config.resolver.SpiResolver;
import com.ychengycheng.config.resolver.XmlResolver;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.core.discovery.impl.NacosRegistryCenter;
import com.ychengycheng.core.loadbalancer.LoadBalancer;
import com.ychengycheng.core.protection.CircuitBreaker;
import com.ychengycheng.core.protection.RateLimiter;
import com.ychengycheng.util.IdGenerator;
import lombok.Data;

/**
 * @author Valar Morghulis
 * @Date 2023/10/2
 */
@Data
public class BootStrapConfiguration {

    /**
     * 端口
     */
    private int port;

    private LoadBalancer loadBalancer;

    /**
     * 服务注册配置
     */
    private RegistryConfig registryConfig;
    /**
     * 协议
     */
    private ProtocolConfig protocolConfig;
    /**
     * 服务注册中心
     */
    private RegistryCenter registryCenter = new NacosRegistryCenter();
    /**
     * Id生成器
     */
    private IdGenerator idGenerator;
    /**
     * 限流器（服务端）
     */
    private RateLimiter rateLimiter;

    /**
     * 熔断器（客户端）
     */
    private CircuitBreaker circuitBreaker;

    public BootStrapConfiguration() {
        //依次从spi,xml加载相对应的配置
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);
    }


}
