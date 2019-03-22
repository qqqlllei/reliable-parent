package com.reliable.message.common.enums;

/**
 * Created by 李雷 on 2019/3/22.
 */
public enum GrayFlagEnum {

    GRAY_MESSAGE(true,"灰度消息"),
    NOT_GRAY_MESSAGE(false,"普通消息");


    private boolean isGray;
    private String value;

    GrayFlagEnum(boolean isGray, String value) {
        this.isGray = isGray;
        this.value = value;
    }




    public boolean isGray() {
        return isGray;
    }

    public java.lang.String getValue() {
        return value;
    }
}
