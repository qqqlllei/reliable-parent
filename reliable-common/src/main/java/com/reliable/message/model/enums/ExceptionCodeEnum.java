package com.reliable.message.model.enums;

public enum ExceptionCodeEnum {

	MSG_CONSUMER_ARGS_IS_NULL(100000,"消息服务消费者参数为空!"),
	MSG_CONSUMER_ARGS_TYPE_IS_WRONG(100001,"消息服务消费者参数类型错误!"),
	MSG_CONSUMER_ARGS_CONVERT_EXCEPTION(100001,"消息服务消费者参数类型转换异常!")
	;
	private int code;
	private String msg;

	/**
	 * Msg string.
	 *
	 * @return the string
	 */
	public String msg() {
		return msg;
	}

	/**
	 * Code int.
	 *
	 * @return the int
	 */
	public int code() {
		return code;
	}

	ExceptionCodeEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	/**
	 * Gets enum.
	 *
	 * @param code the code
	 *
	 * @return the enum
	 */
	public static ExceptionCodeEnum getEnum(int code) {
		for (ExceptionCodeEnum ele : ExceptionCodeEnum.values()) {
			if (ele.code() == code) {
				return ele;
			}
		}
		return null;
	}
}
