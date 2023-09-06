/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 要提供的服务方法配置信息
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceConfig<T> {

    /**
     * 这里是接口
     */
    private Class<T> interfaceProvider;
    /**
     * 这里是接口具体实现类
     */
    private Object ref;


}
