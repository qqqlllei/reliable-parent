package com.reliable.message.common.enums;

public enum DelayLevelEnum {
	ZERO(0, "不延时"),
	ONE(1,"1分钟"),
	TWO(2, "2分钟"),
	THREE(3, "3分钟"),
	FOUR(4, "4分钟"),
	FIVE(5, "5分钟");

	DelayLevelEnum(int delayLevel, String value) {
		this.delayLevel = delayLevel;
		this.value = value;
	}
	private int delayLevel;
	private String value;
	public int delayLevel() {
		return delayLevel;
	}
	public String value() {
		return value;
	}
}
