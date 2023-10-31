package com.ychengycheng.config.resolver;


import com.ychengycheng.config.BootStrapConfiguration;
import com.ychengycheng.config.handler.SpiHandler;
import com.ychengycheng.config.wrapper.ObjectWrapper;
import com.ychengycheng.core.compress.Compressor;
import com.ychengycheng.core.compress.CompressorFactory;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.core.loadbalancer.LoadBalancer;
import com.ychengycheng.core.protection.CircuitBreaker;
import com.ychengycheng.core.protection.RateLimiter;
import com.ychengycheng.core.serialize.Serializer;
import com.ychengycheng.core.serialize.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SpiResolver {

    /**
     * 通过spi的方式加载配置项
     *
     * @param configuration 配置上下文
     */
    public void loadFromSpi(BootStrapConfiguration configuration) {

        // 我的spi的文件中配置了很多实现（自由定义，只能配置一个实现，还是多个）
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        // 将其放入工厂
        if (loadBalancerWrappers != null && loadBalancerWrappers.size() > 0) {
            log.debug("Detect LoadBalancer [{}] from SPI.", loadBalancerWrappers.get(0).getName());
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> objectWrappers = SpiHandler.getList(Compressor.class);
        if (objectWrappers != null) {
            objectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrappers = SpiHandler.getList(Serializer.class);
        if (serializerObjectWrappers != null) {
            serializerObjectWrappers.forEach(SerializerFactory::addSerializer);
        }
        //非工厂的类型（例如熔断器FallbackHandler，限流器RateLimiter）
        List<ObjectWrapper<CircuitBreaker>> cuicuitBreakerList = SpiHandler.getList(CircuitBreaker.class);
        if (cuicuitBreakerList != null && cuicuitBreakerList.size() != 0) {
            log.debug("Detect CircuitBreaker [{}] from SPI.", cuicuitBreakerList.get(0).getName());

            configuration.setCircuitBreaker(cuicuitBreakerList.get(0).getImpl());
        }

        List<ObjectWrapper<RateLimiter>> rateLimiterList = SpiHandler.getList(RateLimiter.class);
        if (rateLimiterList != null && rateLimiterList.size() != 0) {
            log.debug("Detect RateLimiter [{}] from SPI.", rateLimiterList.get(0).getName());
            configuration.setRateLimiter(rateLimiterList.get(0).getImpl());
        }

        List<ObjectWrapper<RegistryCenter>> registryCenter = SpiHandler.getList(RegistryCenter.class);
        if (registryCenter != null && registryCenter.size() != 0) {
            log.debug("Detect RegistryCenter [{}] from SPI.", registryCenter.get(0).getName());
            configuration.setRegistryCenter(registryCenter.get(0).getImpl());
        }

    }
}
