package com.ychengycheng.util.serialize.impl;

import com.ychengycheng.exception.SerializeException;
import com.ychengycheng.util.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * jdk序列化
 *
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
@Slf4j
public class JdkSerializer implements Serializer {
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
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(object);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            log.info("对象【{}】完成了序列化操作，序列化后的字节数为：【{}】", object, bytes.length);
            return bytes;
        } catch (IOException e) {
            log.error("序列化对象【{}】发生异常.", object);
            throw new SerializeException(e);
        }
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
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object object = objectInputStream.readObject();
            log.info("类【{}】完成了反序列化操作，反序列化后的对象为：【{}】", tClass, object);

            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化类：【{}】发生异常！", tClass);
        }
        return null;
    }
}
