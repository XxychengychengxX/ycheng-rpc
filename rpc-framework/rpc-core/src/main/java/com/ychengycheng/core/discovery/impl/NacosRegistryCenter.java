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
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Slf4j
public class NacosRegistryCenter implements RegistryCenter {
    @Override
    public boolean isInstanceExist(RegistryConfig registryConfig) {
        if (registryConfig == null) {
            log.error("注册配置项为空，请加载注册配置项后重试");
        } else {
            //从注册配置中拿到一系列信息
            String clientAddr = registryConfig.getClientAddr();
            int clientPort = registryConfig.getClientPort();
            String applicationName = registryConfig.getApplicationName();

            //todo：这里也需要解耦
            try {
                NamingService namingService = NacosUtil.getNamingService(registryConfig);
                List<Instance> allInstances = namingService.getAllInstances(applicationName);
                if (!allInstances.isEmpty()) {
                    for (Instance instance : allInstances) {
                        if (instance.getIp()
                                    .equals(clientAddr) && instance.getPort() == clientPort) {
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
        NamingService naming = null;
        List<Instance> instances = null;
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.USERNAME, "nacos");
            properties.put(PropertyKeyConst.PASSWORD, "nacos");
            //todo:这里后面重新封装
            properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1:8848");
            naming = NamingFactory.createNamingService(properties);
            instances = naming.selectInstances(applicationName, true);
        } catch (NacosException e) {
            log.error("获取服务实例【{}】异常", applicationName);
        }
        return instances;
    }

    /**
     * 向注册中心正式发布服务
     *
     * @param registerInstance 预注册的服务实例
     * @param registryConfig   注册信息
     * @return 成功返回true
     */
    @Override
    public boolean publish(Object registerInstance, RegistryConfig registryConfig) {
        Instance nacosInstance = (Instance) registerInstance;
        //todo:这里需要抽象出一个可修改的类（或者是工具类）
        String serverAddr = registryConfig.getServerAddr();
        int serverPort = registryConfig.getServerPort();
        //properties.put(PropertyKeyConst.ENDPOINT,registryConfig.getClientAddr());
        try {
            NamingService namingService = NacosUtil.getNamingService(registryConfig);
            namingService.registerInstance(registryConfig.getApplicationName(), nacosInstance);
        } catch (NacosException e) {
            log.error("注册服务失败，请检查后重试！！" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Object getRegisterInstance(RegistryConfig registryConfig) {

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
}
