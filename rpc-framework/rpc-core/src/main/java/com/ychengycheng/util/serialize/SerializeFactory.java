package com.ychengycheng.util.serialize;

import com.ychengycheng.util.serialize.impl.HessianSerializer;
import com.ychengycheng.util.serialize.impl.JdkSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
public class SerializeFactory {
    private static final ConcurrentHashMap<String, SerializerWrapper> STRING_SERIALIZER_MAP =
            new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Byte, SerializerWrapper> BYTE_SERIALIZER_MAP =
            new ConcurrentHashMap<>();

    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JdkSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian",
                                                          new HessianSerializer());

        STRING_SERIALIZER_MAP.put("jdk", jdk);
        STRING_SERIALIZER_MAP.put("hessian", hessian);
        STRING_SERIALIZER_MAP.put("json", json);
        BYTE_SERIALIZER_MAP.put((byte) 2, json);
        BYTE_SERIALIZER_MAP.put((byte) 1, jdk);
        BYTE_SERIALIZER_MAP.put((byte) 3, hessian);

    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     *
     * @param serializeType 序列化的类型
     * @return 包装类
     */
    public static SerializerWrapper getSerializer(String serializeType) {

        return STRING_SERIALIZER_MAP.get(serializeType);
    }

    public static SerializerWrapper getSerializer(byte serializeType) {

        return BYTE_SERIALIZER_MAP.get(serializeType);
    }
}
