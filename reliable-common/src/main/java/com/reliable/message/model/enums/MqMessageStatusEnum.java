package com.reliable.message.model.enums;

/**
 * Created by 李雷 on 2018/5/10.
 */
public enum MqMessageStatusEnum {


    /**
     * WAIT_CONFIRM.
     */
    WAIT_CONFIRM(1, "待确认"),
    /**
     * 消费者.
     */
    SEDING(1, "发送中");

    private int messageStatus;

    private String value;

    MqMessageStatusEnum(int messageStatus, String value) {
        this.messageStatus = messageStatus;
        this.value = value;
    }

    /**
     * Message type int.
     *
     * @return the int
     */
    public int messageStatus() {
        return messageStatus;
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
