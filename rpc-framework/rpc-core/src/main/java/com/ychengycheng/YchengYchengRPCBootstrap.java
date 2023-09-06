package com.ychengycheng;

import com.ychengycheng.channel.initializer.SimpleProviderChannelInitializer;
import com.ychengycheng.config.ProtocolConfig;
import com.ychengycheng.config.ReferenceConfig;
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.config.ServiceConfig;
import com.ychengycheng.core.detector.HeartbeatDetector;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.core.discovery.impl.NacosRegistryCenter;
import com.ychengycheng.core.loadbalancer.LoadBalancer;
import com.ychengycheng.core.loadbalancer.impl.RoundRobinBalancer;
import com.ychengycheng.message.YchengRpcRequest;
import com.ychengycheng.util.IdGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Valar Morghulis
 */
@Slf4j
public class YchengYchengRPCBootstrap {

    public static final ThreadLocal<YchengRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1, 2);
    /**
     * 定义netty——channel的缓存
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
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new HashMap<>();
    public static LoadBalancer loadBalancer;
    public static String serializeType = "jdk";
    public static String compressType = "gzip";
    private static YchengYchengRPCBootstrap ychengYchengRPCBootstrap = new YchengYchengRPCBootstrap();
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
    private RegistryCenter registryCenter;

    /**
     * 将构造方法私有化
     */
    private YchengYchengRPCBootstrap() {
    }


    public static YchengYchengRPCBootstrap getInstance() {
        return ychengYchengRPCBootstrap;
    }

    /**
     * 用来自定义使用的名字
     *
     * @param applicationName 应用名称
     * @return this
     */
    public YchengYchengRPCBootstrap application(String applicationName) {
        //设置注册的服务名
        registryConfig.setApplicationName(applicationName);
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
            log.debug("目前工程使用的注册信息：" + registryConfig.toString());
        }
        //TODO:这里要支持多个配置中心和多个balancer，需要依赖抽象,耦合度还是高，后面想想怎么优化
        this.registryCenter = new NacosRegistryCenter();
        this.registryConfig = registryConfig;
        YchengYchengRPCBootstrap.loadBalancer = new RoundRobinBalancer();
        return this;
    }

    /**
     * 配置当前要启用的服务使用的协议
     *
     * @param protocolConfig 协议信息
     * @return this
     */
    public YchengYchengRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()) {
            log.debug("目前工程使用的协议信息：" + protocolConfig.toString());
        }
        this.protocolConfig = protocolConfig;
        return this;
    }

    /**
     * 发布服务，将接口-》实现都注册到服务注册中心
     *
     * @param serviceConfig 服务的信息封装
     * @return this
     */
    public YchengYchengRPCBootstrap publish(ServiceConfig<?> serviceConfig) {
        boolean publish = false;
        if (log.isDebugEnabled()) {
            log.debug("服务{}，即将被注册", serviceConfig.getInterfaceProvider().getName());
        }
        //判断相同服务实例是否已经存在
        boolean instanceExist = registryCenter.isInstanceExist(registryConfig);
        //如果不存在则开始注册
        if (!instanceExist) {
            //todo:getRegisterInstance这里需要抽象出一个返回类型，而不是使用Object
            Object registerInstance = registryCenter.getRegisterInstance(registryConfig);
            publish = registryCenter.publish(registerInstance, registryConfig);
        }
        /*
         *  当服务调用方使用接口，方法名，具体的方法参数列表向提供者发起调用，怎么知道使用哪一个实现呢
         * 1.自己new 一个
         * 2.spring beanFactory.getBean(Class)
         * 3.手动维护一个映射关系
         * */
        if (publish) {

            SERVICE_LIST.put(serviceConfig.getInterfaceProvider().getName(), serviceConfig);
        }
        return this;
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
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        int clientPort = registryConfig.getClientPort();
        String clientAddr = registryConfig.getClientAddr();
        try {
            //2.需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //3.配置服务器
            serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                           //配置默认的简单服务提供者channel
                           .childHandler(new SimpleProviderChannelInitializer());
            //4.进行服务器端口监听
            ChannelFuture channelFuture = serverBootstrap.bind(clientAddr, clientPort).sync();
            log.info("netty服务器启动成功，服务器监听ip地址：{}", clientAddr + ":" + clientPort);
            //关闭通道
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    public RegistryCenter getRegistryCenter() {
        return registryCenter;
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
        if (registryConfig == null) {
            log.error("注册配置信息丢失");
        } else {
            referenceConfig.setRegistryConfig(registryConfig);
        }
        return this;
    }

    public YchengYchengRPCBootstrap serialize(String jdk) {
        serializeType = jdk;
        return this;
    }
}