/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议配置信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolConfig {


    private String compressType;

    private String serializeType;


}
