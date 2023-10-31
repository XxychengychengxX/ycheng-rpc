package com.ychengycheng.core.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.ychengycheng.core.serialize.Serializer;
import com.ychengycheng.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
@Slf4j
public class HessianSerializer implements Serializer {

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
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            log.info("对象【{}】完成了序列化操作", object);
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
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
            Object object = hessian2Input.readObject();
            log.info("类【{}】完成了反序列化操作", tClass);
            return (T) object;
        } catch (IOException e) {
            log.error("反序列化类：【{}】发生异常！", tClass);
        }
        return null;
    }
}
