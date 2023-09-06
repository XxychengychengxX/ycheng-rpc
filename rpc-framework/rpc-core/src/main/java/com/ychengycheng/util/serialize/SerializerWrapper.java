package com.ychengycheng.util.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SerializerWrapper {

    private byte code;
    private String type;
    private Serializer serializer;
}
