package com.ychengycheng;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.ychengycheng.core.discovery.impl.NacosRegistryCenter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Valar Morghulis
 */
@Slf4j
public class ManageApplication {
    public static void main(String[] args) {
        //拿到一个nacos配置
        NacosRegistryCenter nacosRegistryCenter = new NacosRegistryCenter();
        //拿到服务实例
        //Instance instance = nacosRegistryCenter.getRegisterInstance("");

        NamingService naming;
        try {
            naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
           // naming.registerInstance("test-01", instance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}