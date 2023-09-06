/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.config;


import com.ychengycheng.proxy.handler.ConsumerInvocationHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

@Slf4j
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    /**
     * 获取注册信息所要知道的服务信息
     */
    private RegistryConfig registryConfig;

    /**
     * 代理设计模式生成api接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {

        //这里是使用动态代理做了一些工作
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        //获取类对象
        Class[] classes = new Class[]{interfaceRef};

        //使用动态代理生成代理对象，调用任何方法其实都会走invoke方法
        Object newProxyInstance = Proxy.newProxyInstance(contextClassLoader, classes,
                new ConsumerInvocationHandler(registryConfig, interfaceRef) {
        });
        return (T) newProxyInstance;
    }
}
