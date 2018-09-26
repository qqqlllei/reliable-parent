package com.reliable.message.client.aspect;


import com.alibaba.fastjson.JSONObject;
import com.reliable.message.client.annotation.MqConsumerStore;
import com.reliable.message.client.service.MqMessageService;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.enums.ExceptionCodeEnum;
import com.reliable.message.model.enums.MqMessageTypeEnum;
import com.reliable.message.model.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;


@Slf4j
@Aspect
public class MqConsumerStoreAspect {

	@Resource
	private MqMessageService mqMessageService;

	@Value("${spring.application.name}")
	private String appName;


	@Value("${reliable.message.consumerGroup:}")
	private String consumerGroup;

	private static final String CONSUME_SUCCESS = "CONSUME_SUCCESS";

	/**
	 * Add exe time annotation pointcut.
	 */
	@Pointcut("@annotation(com.reliable.message.client.annotation.MqConsumerStore)")
	public void mqConsumerStoreAnnotationPointcut() {

	}

	/**
	 * Add exe time method object.
	 *
	 * @param joinPoint the join point
	 *
	 * @return the object
	 *
	 * @throws Throwable the throwable
	 */
	@Around(value = "mqConsumerStoreAnnotationPointcut()")
	public Object processMqConsumerStoreJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {

		log.info("processMqConsumerStoreJoinPoint - 线程id={}", Thread.currentThread().getId());
		if(StringUtils.isBlank(consumerGroup)) consumerGroup = appName;
		Object result;
		long startTime = System.currentTimeMillis();
		Object[] args = joinPoint.getArgs();
		MqConsumerStore annotation = getAnnotation(joinPoint);
		boolean isStorePreStatus = annotation.storePreStatus();
		ClientMessageData clientMessageData;
		if (args == null || args.length == 0) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_IS_NULL);
		}

		if (!(args[0] instanceof ClientMessageData)) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_IS_NULL);
		}

		try {
			clientMessageData = (ClientMessageData) args[0];
		} catch (Exception e) {
			log.error("processMqConsumerStoreJoinPoint={}", e.getMessage(), e);
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_CONVERT_EXCEPTION);
		}
		final Long messageId = clientMessageData.getId();



		// 重复消费检查
		boolean consumed = mqMessageService.checkMessageStatus(messageId);
		if(consumed){
			mqMessageService.confirmFinishMessage(consumerGroup, clientMessageData.getProducerMessageId());
			return null;
		}



		if (isStorePreStatus) {
			mqMessageService.confirmReceiveMessage(consumerGroup, clientMessageData);
		}
		String methodName = joinPoint.getSignature().getName();
		try {
			result = joinPoint.proceed();
			log.info("result={}", result);
			if (CONSUME_SUCCESS.equals(result.toString())) {
				mqMessageService.confirmFinishMessage(consumerGroup, clientMessageData.getProducerMessageId());
			}
		} catch (Exception e) {
			log.error("发送可靠消息, 目标方法[{}], 出现异常={}", methodName, e.getMessage(), e);
			throw e;
		} finally {
			log.info("发送可靠消息 目标方法[{}], 总耗时={}", methodName, System.currentTimeMillis() - startTime);
		}
		return result;
	}

	private MqConsumerStore getAnnotation(JoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method.getAnnotation(MqConsumerStore.class);
	}

	private ClientMessageData getTpcMqMessageDto(JSONObject messageExt) {
		ClientMessageData data = new ClientMessageData();
		data.setMessageBody(messageExt.getString("body"));
		data.setMessageKey(messageExt.getString("key"));
		data.setMessageTopic(messageExt.getString("topic"));
		data.setMessageType(MqMessageTypeEnum.CONSUMER_MESSAGE.messageType());
		return data;
	}
}
