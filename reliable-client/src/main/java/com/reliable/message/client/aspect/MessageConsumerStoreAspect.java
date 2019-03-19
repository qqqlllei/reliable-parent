package com.reliable.message.client.aspect;


import com.reliable.message.client.annotation.MessageConsumerStore;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.domain.ServerMessageData;
import com.reliable.message.model.enums.ExceptionCodeEnum;
import com.reliable.message.model.enums.MessageTypeEnum;
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


@Slf4j
@Aspect
public class MessageConsumerStoreAspect {

	@Resource
	private ReliableMessageService reliableMessageService;

	@Value("${spring.application.name}")
	private String appName;

	@Value("${reliable.message.consumerGroup:}")
	private String consumerGroup;

	@Pointcut("@annotation(com.reliable.message.client.annotation.MessageConsumerStore)")
	public void mqConsumerStoreAnnotationPointcut() {

	}

	@Around(value = "mqConsumerStoreAnnotationPointcut()")
	public void processMqConsumerStoreJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {

		log.info("processMqConsumerStoreJoinPoint - 线程id={}", Thread.currentThread().getId());
		if(StringUtils.isBlank(consumerGroup)) consumerGroup = appName;
		long startTime = System.currentTimeMillis();
		Object[] args = joinPoint.getArgs();
		MessageConsumerStore annotation = getAnnotation(joinPoint);
		boolean isStorePreStatus = annotation.storePreStatus();
		ServerMessageData serverMessageData;
		if (args == null || args.length == 0) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_IS_NULL);
		}

		if (!(args[0] instanceof ServerMessageData)) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_TYPE_IS_WRONG);
		}

		try {
			serverMessageData = (ServerMessageData) args[0];
		} catch (Exception e) {
			log.error("processMqConsumerStoreJoinPoint={}", e.getMessage(), e);
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_CONVERT_EXCEPTION);
		}
		final Long producerMessageId = serverMessageData.getProducerMessageId();

		if (isStorePreStatus) {
			// 重复消费检查
			boolean consumed = reliableMessageService.checkMessageStatus(producerMessageId, MessageTypeEnum.CONSUMER_MESSAGE.messageType());
			if(consumed){
				reliableMessageService.confirmFinishMessage(consumerGroup, serverMessageData.getProducerMessageId());
				return ;
			}
			reliableMessageService.confirmReceiveMessage(consumerGroup, serverMessageData);
		}
		String methodName = joinPoint.getSignature().getName();
		try {
			joinPoint.proceed();
			reliableMessageService.confirmFinishMessage(consumerGroup, serverMessageData.getProducerMessageId());
		} catch (Exception e) {
			log.error("发送可靠消息, 目标方法[{}], 出现异常={}", methodName, e.getMessage(), e);
			throw e;
		} finally {
			log.info("发送可靠消息 目标方法[{}], 总耗时={}", methodName, System.currentTimeMillis() - startTime);
		}
	}

	private MessageConsumerStore getAnnotation(JoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method.getAnnotation(MessageConsumerStore.class);
	}
}
