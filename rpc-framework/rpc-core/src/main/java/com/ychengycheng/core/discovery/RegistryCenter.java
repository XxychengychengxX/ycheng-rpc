/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.core.discovery;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.config.RegistryConfig;

import java.util.List;

/**
 * 注册中心
 *
 * @author Valar Morghulis
 */
public interface RegistryCenter {


    /**
     * 获取预创建的链接实例
     *
     * @param registryConfig 注册信息
     * @return 链接实例Instance
     */
    Object getRegisterInstance(RegistryConfig registryConfig);

    /**
     * 进行判断服务是否存在
     * @param rpcBootstrap 启动类配置
     * @return 存在返回true
     */
    boolean isInstanceExist(RegistryConfig rpcBootstrap);

    /**
     *
     * @param applicationName 服务名
     * @return
     */
    List<Instance> lookup(String applicationName);

    /**
     * 向注册中心正式发布服务
     * @param registerInstance 预注册的服务实例
     * @param registryConfig 注册信息
     * @return 成功返回true
     */
    boolean publish(Object registerInstance, RegistryConfig registryConfig);
}
