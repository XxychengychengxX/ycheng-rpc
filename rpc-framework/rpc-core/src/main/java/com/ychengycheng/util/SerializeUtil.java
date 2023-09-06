package com.ychengycheng.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 序列化工具类
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
public class SerializeUtil {

    public static byte[] serialize(Object object){
        if (object==null) {
            return null;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}
