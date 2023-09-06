/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.util;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.exception.MyNacosException;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class NacosUtil {





    public static NamingService getNamingService(RegistryConfig registryConfig) {
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
