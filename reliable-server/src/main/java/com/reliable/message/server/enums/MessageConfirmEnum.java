package com.reliable.message.server.enums;

/**
 * Created by 李雷 on 2019/3/20.
 */
public enum MessageConfirmEnum {


    NOT_COMFIRM(0, "未消费"),

    /**
     * 已完成
     */
    CONFIRMED(1, "已消费");

    private int confirmFlag;

    private String value;

    MessageConfirmEnum(int confirmFlag, String value) {
        this.confirmFlag = confirmFlag;
        this.value = value;
    }

    /**
     * Confirm status int.
     *
     * @return the int
     */
    public int confirmFlag() {
        return confirmFlag;
    }

    /**
     * Value string.
     *
     * @return the string
     */
    public String value() {
        return value;
    }
}
