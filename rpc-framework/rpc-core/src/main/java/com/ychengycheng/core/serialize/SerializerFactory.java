package com.ychengycheng.core.serialize;

import com.ychengycheng.config.wrapper.ObjectWrapper;
import com.ychengycheng.core.serialize.impl.HessianSerializer;
import com.ychengycheng.core.serialize.impl.JdkSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
public class SerializerFactory {
    private static final ConcurrentHashMap<String, com.ychengycheng.config.wrapper.ObjectWrapper<Serializer>> STRING_SERIALIZER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Byte, com.ychengycheng.config.wrapper.ObjectWrapper<Serializer>> BYTE_SERIALIZER_MAP = new ConcurrentHashMap<>();



    /**
     * 使用工厂方法获取一个SerializerWrapper
     *
     * @param serializeType 序列化的类型
     * @return 包装类
     */
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {

        return STRING_SERIALIZER_MAP.get(serializeType);
    }

    public static ObjectWrapper<Serializer> getSerializer(byte serializeType) {

        return BYTE_SERIALIZER_MAP.get(serializeType);
    }

    public static void addSerializer(
            com.ychengycheng.config.wrapper.ObjectWrapper<Serializer> serializerObjectWrapper) {
        STRING_SERIALIZER_MAP.put(serializerObjectWrapper.getName(), serializerObjectWrapper);
        BYTE_SERIALIZER_MAP.put(serializerObjectWrapper.getCode(), serializerObjectWrapper);
    }
}
