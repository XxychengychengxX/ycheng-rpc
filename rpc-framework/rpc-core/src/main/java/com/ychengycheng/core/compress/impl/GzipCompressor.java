package com.ychengycheng.core.compress.impl;

import com.ychengycheng.core.compress.Compressor;
import com.ychengycheng.exception.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 使用gzip算法进行压缩的具体实现
 *
 * @author it楠老师
 * @createTime 2023-07-05
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) throws CompressException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对字节数组进行了压缩长度由【{}】压缩至【{}】.", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }

    }

    /**
     * 对字节数组进行解压缩
     *
     * @param bytes 待解压缩的字节数组
     * @return 解压缩后的字节数组
     */
    @Override
    public byte[] decompress(byte[] bytes) throws CompressException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(bais);) {
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("对字节数组进行了解压缩长度由【{}】变为【{}】.", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            throw new CompressException(e);
        }
    }
}
