package com.ychengycheng;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProviderApplication {
    public static void main(String[] args) {
        //服务提供方，需要注册服务，启动服务
        //定义具体的服务

        /*-------------------------------这里是由编程控制，优先级最高---------------------------*/
        /*-------------------------------这里是由编程控制，优先级最高---------------------------*/
        /*-------------------------------这里是由编程控制，优先级最高---------------------------*/

        /*ServiceConfig<GreetingService> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterfaceProvider(GreetingService.class);
        serviceConfig.setRef(new GreetingServiceImpl());

        *//*下面是配置注册中心的注册信息*//*
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setServerType(RegisterConfigConstant.NACOS);
        registryConfig.setServerPort(RegisterConfigConstant.DEFAULT_NACOS_SERVER_PORT);
        registryConfig.setServerAddr(RegisterConfigConstant.DEFAULT_NACOS_SERVER_ADDR);
        //获取当前的网卡地址和自定义端口
        try {
            Optional<Inet4Address> localIp4Address = InetUtil.getLocalIp4Address();
            String hostAddress = localIp4Address.get().getHostAddress();
            registryConfig.setClientAddr(hostAddress);
            registryConfig.setClientPort(9988);
        } catch (SocketException e) {
            log.error("获取当前网卡信息失败");
            throw new RuntimeException(e);

        }

        //定义协议
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setCompressType("gzip");
        protocolConfig.setSerializeType("jdk");
        */
        //定义注册的配置


        YchengYchengRPCBootstrap.getInstance()//配置注册
                                .scan("com.ychengycheng.service.impl")
                                .start();


    }
}