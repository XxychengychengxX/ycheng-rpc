/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.config;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 协议配置信息
 */
@Data
@AllArgsConstructor
public class ProtocolConfig {
    /**
     * 协议名字
     */
    private String protocolName;


}
