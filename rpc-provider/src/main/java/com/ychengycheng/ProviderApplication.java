package com.ychengycheng;

import com.ychengycheng.config.ProtocolConfig;
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.config.ServiceConfig;
import com.ychengycheng.constant.RegisterConfigConstant;
import com.ychengycheng.service.impl.GreetingServiceImpl;
import com.ychengycheng.util.InetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.SocketException;
import java.util.Optional;

@Slf4j
public class ProviderApplication {
    public static void main(String[] args) {
        //服务提供方，需要注册服务，启动服务
        //定义具体的服务
        ServiceConfig<GreetingService> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterfaceProvider(GreetingService.class);
        serviceConfig.setRef(new GreetingServiceImpl());

        //todo:这里也需要重新配置，应该是根据main函数args中判断
        /*下面是配置注册中心的注册信息*/

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setServerType(RegisterConfigConstant.NACOS);
        registryConfig.setServerPort(RegisterConfigConstant.DEFAULT_NACOS_SERVER_PORT);
        registryConfig.setServerAddr(RegisterConfigConstant.DEFAULT_NACOS_SERVER_ADDR);
        //获取当前的网卡地址和自定义端口
        try {
            Optional<Inet4Address> localIp4Address = InetUtil.getLocalIp4Address();
            String hostAddress = localIp4Address.get().getHostAddress();
            registryConfig.setClientAddr(hostAddress);
            //todo:这里以后要解决端口的问题，思路：1.使用main方法的args传参
            registryConfig.setClientPort(9988);
        } catch (SocketException e) {
            log.error("获取当前网卡信息失败");
            throw new RuntimeException(e);
        }
        //定义注册的配置

        //定义协议
        ProtocolConfig protocolConfig = new ProtocolConfig("jdk");
        //服务提供方会做什么
        /*
         * 1.配置应用的名称
         * 2.配置注册中心
         * 3.配置协议（序列化，压缩等等）
         * 4.发布服务
         * 5.启动服务
         * */
        YchengYchengRPCBootstrap.getInstance()//配置注册
                                //配置服务名
                                .register(registryConfig)
                                //再配置服务名
                                .application(serviceConfig.getInterfaceProvider().getName())
                                //配置协议（序列化，压缩）
                                .protocol(protocolConfig)
                                //发布服务
                                .publish(serviceConfig)

                                .start();


    }
}