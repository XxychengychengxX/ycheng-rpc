package com.ychengycheng;

import com.ychengycheng.config.ReferenceConfig;
import com.ychengycheng.service.GreetingService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        //这里要获取代理对象，才能实现Rpc，这里获取的是rpc-api中的接口对象，下面要寻找它的服务提供者的代理
        ReferenceConfig<GreetingService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterfaceRef(GreetingService.class);

        /*RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setServerType(RegisterConfigConstant.NACOS);
        registryConfig.setServerPort(RegisterConfigConstant.DEFAULT_NACOS_SERVER_PORT);
        registryConfig.setServerAddr(RegisterConfigConstant.DEFAULT_NACOS_SERVER_ADDR);*/
       /* try {
            Optional<Inet4Address> localIp4Address = InetUtil.getLocalIp4Address();
            String hostAddress = localIp4Address.get().getHostAddress();
            registryConfig.setClientAddr(hostAddress);

            registryConfig.setClientPort(9989);

        } catch (SocketException e) {
            log.error("获取当前网卡信息失败");
        }*/
        //代理会做什么
        /*
         * 1.链接注册中心
         * 2.拉取服务列表
         * 3.选择一个服务器并链接
         * 4.发送请求，携带一些信息（接口名，参数列表，方法名等等）
         * */
        YchengYchengRPCBootstrap.getInstance()
                                //配置注册中心
                                //选择一个服务并链接
                                .reference(referenceConfig);
        //获取代理对象
        //（调用方法）
        //todo:如果这里fork多个子线程，每个子线程分别调用不同的service，会不会复用同一个断路器？
        GreetingService greetingService = referenceConfig.get();
        log.info("Get the instance：{}，the remote procedure call is about to begin....",
                 greetingService.getClass().getName());
        String s = greetingService.sayHello();
        log.info("The Result is : ---> {}", s);
        //log.info(greetingService.toString());
        //String s = greetingService.sayHello();
        //log.info(s);

    }
}