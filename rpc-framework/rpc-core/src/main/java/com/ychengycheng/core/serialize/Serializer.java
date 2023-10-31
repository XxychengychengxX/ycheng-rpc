/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
package com.ychengycheng.core.serialize;

import com.ychengycheng.exception.SerializeException;

public interface Serializer {
    /**
     * 用来序列化的方法
     *
     * @param object 待序列化的对象实例
     * @return 字节数组
     */
    byte[] serialize(Object object) throws SerializeException;

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param tClass clas
     * @return 转化后的泛型
     * @param <T> 泛型
     */
    <T> T deSerlialize(byte[] bytes, Class<T> tClass);
}
