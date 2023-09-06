/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 注册配置信息
 *
 * @author Valar Morghulis
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistryConfig {

    //服务名
    private String applicationName;
    //指定链接的注册中心的ip地址
    private String serverAddr;
    //指定链接的注册中心的端口
    private int serverPort;
    private String serverType;
    private String clientAddr;
    private int clientPort;

}
