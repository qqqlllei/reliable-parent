package com.reliable.message.common.enums;

public enum MessageSendStatusEnum {
	/**
	 * 未发送.
	 */
	WAIT_CONFIRM(10, "未发送"),

	/**
	 * 已发送.
	 */
	SENDING(20, "已发送"),

	/**
	 * 已完成
	 */
	FINISH(30, "已完成");

	private int sendStatus;

	private String value;

	MessageSendStatusEnum(int sendStatus, String value) {
		this.sendStatus = sendStatus;
		this.value = value;
	}

	/**
	 * Confirm status int.
	 *
	 * @return the int
	 */
	public int sendStatus() {
		return sendStatus;
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
