package com.reliable.message.model.enums;

public enum ExceptionCodeEnum {

	MSG_CONSUMER_ARGS_IS_NULL(100000,"消息服务消费者参数为空!"),
	MSG_CONSUMER_ARGS_TYPE_IS_WRONG(100001,"消息服务消费者参数类型错误!"),
	MSG_CONSUMER_ARGS_CONVERT_EXCEPTION(100002,"消息服务消费者参数类型转换异常!"),
	MSG_CONSUMER_CONFIRM_FINISH_MESSAGE_ERROR(100003, "消息服务消费者确认完成消息异常!"),

	MSG_PRODUCER_ARGS_IS_NULL(200000,"消息服务发送者参数为空!"),
	MSG_PRODUCER_ARGS_TYPE_IS_WRONG(200001,"消息服务发送者参数类型错误!"),
	MSG_PRODUCER_ENTITY_ID_IS_EMPTY(200002,"消息服务发送者消息id为null!"),
	MSG_PRODUCER_CONFIRM_AND_SEND_MESSAGE_ERROR(200003, "消息服务发送者确认并发送消息异常!"),
	MSG_PRODUCER_ARGS_OF_MESSAGE_TOPIC_IS_NULL(200004,"消息服务发送者参数[topic] is null!" ),
	MSG_PRODUCER_ARGS_OF_MESSAGE_BODY_IS_NULL(200005, "消息服务发送者参数[body] is null!"),
	MSG_PRODUCER_ARGS_OF_MESSAGE_GROUP_IS_NULL(200006, "消息服务发送者参数[group] is null!");
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
