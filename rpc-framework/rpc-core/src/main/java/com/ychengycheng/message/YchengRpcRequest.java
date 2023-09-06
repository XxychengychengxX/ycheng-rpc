/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.message;

import lombok.*;

import java.io.Serializable;

/**
 * 服务调用方发起的请求内容（报文）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class YchengRpcRequest implements Serializable {
    /**
     * 时间戳
     */
    private Long timeStamp;

    /**
     * 请求id
     */
    private Long requestId;
    /**
     * 请求的类型
     */
    private byte requestType;
    /**
     * 压缩的类型
     */
    private byte compressType;
    /**
     * 序列化类型
     */
    private byte serializeType;

    /**
     * 信息负载
     */
    private RequestPayload requestPayload;

}
