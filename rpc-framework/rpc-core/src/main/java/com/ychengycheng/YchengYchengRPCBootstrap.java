package com.ychengycheng;

import com.ychengycheng.annotation.YchengApi;
import com.ychengycheng.channel.initializer.SimpleProviderChannelInitializer;
import com.ychengycheng.config.*;
import com.ychengycheng.core.detector.HeartbeatDetector;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.core.loadbalancer.LoadBalancer;
import com.ychengycheng.core.protection.impl.TokenBucketRateLimiter;
import com.ychengycheng.message.YchengRpcRequest;
import com.ychengycheng.util.ClassFileUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Valar Morghulis
 */
@Slf4j
public class YchengYchengRPCBootstrap {


    public static final ThreadLocal<YchengRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 定义netty——channel的缓存（用于服务消费者）
     */
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    /**
     * 响应时间
     */
    public static final TreeMap<Long, Channel> ANSWER_TIME_TREEMAP = new TreeMap<>();
    /**
     * 定义全局的对外挂起的completableFutrue
     */
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();
    /**
     * 维护发布且暴露的服务列表，key->interface的全限定名，value->serviceConfig
     */
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>();
    private static final YchengYchengRPCBootstrap ychengYchengRPCBootstrap = new YchengYchengRPCBootstrap();
    private final BootStrapConfiguration configuration;

    /**
     * 将构造方法私有化
     */
    private YchengYchengRPCBootstrap() {
        configuration = new BootStrapConfiguration();
    }

    public static YchengYchengRPCBootstrap getInstance() {
        return ychengYchengRPCBootstrap;
    }

    public BootStrapConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 用来自定义使用的名字
     *
     * @param applicationName 应用名称
     * @return this
     */
    public YchengYchengRPCBootstrap application(String applicationName) {
        //设置注册的服务名
        configuration.getRegistryConfig().setApplicationName(applicationName);
        return this;
    }

    /**
     * 配置一个注册中心，
     *
     * @param registryConfig 注册中心的信息
     * @return this
     */
    public YchengYchengRPCBootstrap register(RegistryConfig registryConfig) {
        if (log.isDebugEnabled()) {
            log.debug("The register info is： [{}]." + registryConfig.toString());
        }
        configuration.setRegistryConfig(registryConfig);
        return this;
    }

    /**
     * 配置负载均衡策略
     *
     * @param loadBalancer 负载均衡策略
     * @return this
     */
    public YchengYchengRPCBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /**
     * 配置当前要启用的服务使用的协议
     *
     * @param protocolConfig 协议信息
     * @return this
     */
    public YchengYchengRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        configuration.setProtocolConfig(protocolConfig);
        return this;
    }

    /**
     * 发布服务，将接口-> 实现都注册到服务注册中心
     *
     * @param serviceConfig 服务的信息封装
     */
    public void publish(ServiceConfig<?> serviceConfig) {
        boolean publish;
        RegistryCenter registryCenter = configuration.getRegistryCenter();
        RegistryConfig registryConfig = configuration.getRegistryConfig();
        String name = serviceConfig.getInterfaceProvider().getName();
        publish = registryCenter.publish(name, registryConfig);
        /*
         *  当服务调用方使用接口，方法名，具体的方法参数列表向提供者发起调用，怎么知道使用哪一个实现呢
         * 1.自己new 一个
         * 2.spring beanFactory.getBean(Class)
         * 3.手动维护一个映射关系
         * */
        if (publish) {
            SERVICE_LIST.put(serviceConfig.getInterfaceProvider().getName(), serviceConfig);
            if (log.isDebugEnabled()) {
                log.debug("publish service [{}] successfully .", serviceConfig.getInterfaceProvider().getName());
            }
        }

    }

    /**
     * 批量发布服务
     *
     * @param serviceConfigs 批量服务信息集合+
     * @return this
     */
    public YchengYchengRPCBootstrap publish(List<? extends ServiceConfig> serviceConfigs) {
        serviceConfigs.forEach(this::publish);
        return this;
    }

    /**
     * 启动服务
     */
    public void start() {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(10, 2);
        configuration.setRateLimiter(rateLimiter);
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        int clientPort = configuration.getPort();
        String clientAddr = configuration.getRegistryConfig().getClientAddr();
        try {
            //2.需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //3.配置服务器
            serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                           //配置默认的简单服务提供者channel
                           .childHandler(new SimpleProviderChannelInitializer());
            //4.进行服务器端口监听
            ChannelFuture channelFuture = serverBootstrap.bind(clientAddr, clientPort).sync();
            log.info("netty server start successfully ，listening ip ：{} ", clientAddr + ":" + clientPort);
            //关闭通道
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }


    /*----------------------------消费方使用的api-------------------------*/


    /**
     * 提供给服务调用方（消费者使用的api）
     *
     * @param referenceConfig 来自远端的引用的接口
     * @return this
     */
    public YchengYchengRPCBootstrap reference(ReferenceConfig<?> referenceConfig) {
        //开启心跳检测
        HeartbeatDetector.detectHeartbeat(referenceConfig.getInterfaceRef().getName());
        //拿到相关的配置项，方便服务调用方创造代理对象
        if (configuration.getRegistryConfig() == null) {
            log.error("注册配置信息丢失");
        } else {
            referenceConfig.setRegistryConfig(configuration.getRegistryConfig());
        }
        return this;
    }

    /**
     * 配置序列化方式
     *
     * @param serializeType
     * @return
     */
    public YchengYchengRPCBootstrap serialize(String serializeType) {
        configuration.getProtocolConfig().setSerializeType(serializeType);
        return this;
    }

    /**
     * 配置压缩方式
     *
     * @param compressType
     * @return
     */
    public YchengYchengRPCBootstrap compress(String compressType) {
        configuration.getProtocolConfig().setCompressType(compressType);

        return this;
    }

    /**
     * 扫描服务包进行服务发布
     *
     * @param packageName
     * @return
     */
    public YchengYchengRPCBootstrap scan(String packageName) {
        // 1、需要通过packageName获取其下的所有的类的权限定名称
        List<String> classNames = ClassFileUtil.getAllClassNames(packageName);
        // 2、通过反射获取他的接口，构建具体实现
        List<Class<?>> classes = classNames.stream().map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).filter(clazz -> clazz.getAnnotation(YchengApi.class) != null).collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            // 获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            // 获取分组信息
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterfaceProvider(anInterface);
                serviceConfig.setRef(instance);

                // 3、发布
                publish(serviceConfig);
            }
        }
        return ychengYchengRPCBootstrap;
    }

}