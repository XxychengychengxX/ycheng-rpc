/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.core.discovery.impl;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.core.discovery.RegistryCenter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Slf4j
public class NacosRegistryCenter implements RegistryCenter {
    @Override
    public boolean isInstanceExist(RegistryConfig registryConfig, int port) {
        if (registryConfig == null) {
            log.error("注册配置项为空，请加载注册配置项后重试");
        } else {
            //从注册配置中拿到一系列信息
            String clientAddr = registryConfig.getClientAddr();

            String applicationName = registryConfig.getApplicationName();

            //todo：这里也需要解耦
            try {
                NamingService namingService = getNamingService(registryConfig);
                List<Instance> allInstances = namingService.getAllInstances(applicationName);
                if (!allInstances.isEmpty()) {
                    for (Instance instance : allInstances) {
                        if (instance.getIp().equals(clientAddr) && instance.getPort() == port) {
                            return true;
                        }
                    }
                }
            } catch (NacosException e) {
                log.error(e.getMessage());
            }

        }
        return false;
    }

    @Override
    public List<Instance> lookup(String applicationName) {
        NamingService naming;
        List<Instance> instances = null;
        try {
            //todo:这里后面重新封装
            RegistryConfig registryConfig = YchengYchengRPCBootstrap.getInstance()
                                                                    .getConfiguration()
                                                                    .getRegistryConfig();
            String serverAddr = registryConfig.getServerAddr();
            int serverPort = registryConfig.getServerPort();
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr + ":" + serverPort);
            properties.put(PropertyKeyConst.USERNAME, "nacos");
            properties.put(PropertyKeyConst.PASSWORD, "nacos");

            naming = NamingFactory.createNamingService(properties);

            //naming.subscribe(applicationName, new OnLineStatusWatcher());
            instances = naming.selectInstances(applicationName, true);
        } catch (NacosException e) {
            log.error("获取服务实例【{}】异常", applicationName);
        }
        return instances;
    }

    /**
     * 向注册中心正式发布服务
     *
     * @param interfaceName  接口的全限定类名
     * @param registryConfig 注册信息
     * @return 成功返回true
     */
    @Override
    public boolean publish(String interfaceName, RegistryConfig registryConfig) {
        //properties.put(PropertyKeyConst.ENDPOINT,registryConfig.getClientAddr());
        try {
            NamingService namingService = getNamingService(registryConfig);
            Instance registerInstance = getRegisterInstance(registryConfig);
            namingService.registerInstance(interfaceName, registerInstance);
        } catch (NacosException e) {
            log.error("注册服务失败，请检查后重试！！" + e.getMessage());
            return false;
        }
        return true;
    }


    @Override
    public Instance getRegisterInstance(RegistryConfig registryConfig) {

        String clientAddr = registryConfig.getClientAddr();
        int clientPort = registryConfig.getClientPort();

        Instance instance = new Instance();
        //创建服务节点
        instance.setIp(clientAddr);
        instance.setPort(clientPort);
        instance.setHealthy(true);
        instance.setWeight(1.0);
        //默认非持久化
        instance.setEphemeral(true);
        //设置节点的元数据
        HashMap<String, String> metaHashMap = new HashMap<>();
        instance.setMetadata(metaHashMap);


        return instance;
    }

    /**
     * 获取nacos定义的namingSerivce
     *
     * @param registryConfig 服务注册的配置
     * @return
     */
    private NamingService getNamingService(RegistryConfig registryConfig) {
        NamingService namingService;
        String serverAddr = registryConfig.getServerAddr();
        int serverPort = registryConfig.getServerPort();
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.USERNAME, "nacos");
        properties.put(PropertyKeyConst.PASSWORD, "nacos");
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr + ":" + serverPort);
        try {
            namingService = NamingFactory.createNamingService(properties);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
        return namingService;
    }
}
