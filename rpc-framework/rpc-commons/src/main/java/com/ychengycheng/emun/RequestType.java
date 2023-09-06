/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
package com.ychengycheng.emun;

/**
 * 标记请求类型
 */
public enum RequestType {
    /**
     * 普通请求
     */
    REQUEST((byte) 1, "普通请求"),
    /**
     * 心跳
     */
    HEART_BEAT((byte) 2, "心跳请求");
    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
