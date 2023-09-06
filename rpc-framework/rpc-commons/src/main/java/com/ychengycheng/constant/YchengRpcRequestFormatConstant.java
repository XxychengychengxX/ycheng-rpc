/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.constant;

public class YchengRpcRequestFormatConstant {

    public static final byte[] MAGIC = "ycyc".getBytes();
    /**
     * 版本号
     */
    public static final byte VERSION = 1;

    /**
     * 版本号占用的字节数
     */
    public static final byte VERSION_LENGTH = 1;

    /**
     * 头部信息长度
     */
    public static final short HEADER_LENGTH = (byte) (MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8 + 8);

    /**
     * 头部信息长度占用的字节数
     */
    public static final short HEADER_FIELD_LENGTH = 2;
    public static final short FULL_FIELD_LENGTH = 4;


    /**
     * 最大帧长度
     */
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;

}
