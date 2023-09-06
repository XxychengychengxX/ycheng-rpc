/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.message;

import lombok.*;

import java.io.Serializable;

/**
 * 服务提供方发起的恢复内容（报文）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class YchengRpcResponse implements Serializable {

    /**
     * 时间戳
     */
    private Long timeStamp;

    /**
     * 请求id
     */
    private Long requestId;

    /**
     * 压缩的类型
     */
    private byte compressType;
    /**
     * 序列化类型
     */
    private byte serializeType;



    /**
     * 响应码
     */
    private int code;
    /**
     * 信息负载
     */

    private Object body;
}
