/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.message;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RequestPayload implements Serializable {

    private String interfaceName;

    private String methodName;

    private Class<?>[] parametersType;

    private Object[] parameterValue;
    private Object[] returnValue;

    private Class<?> returnType;
}
