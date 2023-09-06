/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
package com.ychengycheng.emun;

public enum RespCode {
    /**
     * 成功与失败
     */
    SUCCESS((byte) 1, "成功"), FAIL((byte) 2, "失败");
    private byte code;
    private String desc;

    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
