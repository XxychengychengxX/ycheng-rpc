/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
package com.ychengycheng.util.compress;

import com.ychengycheng.exception.CompressException;

public interface Compressor {
    /**
     * 对字节数组进行压缩
     *
     * @param bytes 待压缩的字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes) throws CompressException;

    /**
     * 对字节数组进行解压缩
     *
     * @param bytes 待解压缩的字节数组
     * @return 解压缩后的字节数组
     */
    byte[] decompress(byte[] bytes) throws CompressException;
}
