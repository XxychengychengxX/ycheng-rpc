package com.ychengycheng.util.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.ychengycheng.exception.SerializeException;
import com.ychengycheng.util.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
@Slf4j
public class JsonSerializer implements Serializer {
    /**
     * 用来序列化的方法
     *
     * @param object 待序列化的对象实例
     * @return 字节数组
     */
    @Override
    public byte[] serialize(Object object) throws SerializeException {
        if (object == null) {
            return null;
        }
        log.info("对象【{}】完成了序列化操作", object);
        return JSON.toJSONBytes(object);

    }

    /**
     * 反序列化
     *
     * @param bytes  字节数组
     * @param tClass clas
     * @return 转化后的泛型
     */
    @Override
    public <T> T deSerlialize(byte[] bytes, Class<T> tClass) {
        if (bytes == null || tClass == null) {
            return null;
        }
        T t = JSON.parseObject(bytes, tClass);
        log.info("类【{}】完成了反序列化操作", tClass);
        return t;

    }
}
