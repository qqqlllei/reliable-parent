package com.reliable.message.client.aspect;


import com.reliable.message.client.annotation.MessageConsumer;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.domain.ServerMessageData;
import com.reliable.message.common.dto.MessageData;
import com.reliable.message.common.enums.ExceptionCodeEnum;
import com.reliable.message.common.enums.MessageTypeEnum;
import com.reliable.message.common.exception.BusinessException;
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
public class MessageConsumerAspect {

	@Resource
	private ReliableMessageService reliableMessageService;

	@Value("${spring.application.name}")
	private String appName;

	@Value("${reliable.message.consumerGroup:}")
	private String consumerGroup;

	@Pointcut("@annotation(com.reliable.message.client.annotation.MessageConsumer)")
	public void messageConsumerAnnotationPointcut() {

	}

	@Around(value = "messageConsumerAnnotationPointcut()")
	public void processMessageConsumerJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {

		log.info("processMessageConsumerJoinPoint - 线程id={}", Thread.currentThread().getId());
		if(StringUtils.isBlank(consumerGroup)) consumerGroup = appName;
		long startTime = System.currentTimeMillis();
		Object[] args = joinPoint.getArgs();
		MessageConsumer annotation = getAnnotation(joinPoint);

		MessageData messageData;
		if (args == null || args.length == 0) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_IS_NULL);
		}

		if (!(args[0] instanceof MessageData)) {
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_TYPE_IS_WRONG);
		}

		try {
			messageData = (MessageData) args[0];
		} catch (Exception e) {
			log.error("processMessageConsumerJoinPoint={}", e.getMessage(), e);
			throw new BusinessException(ExceptionCodeEnum.MSG_CONSUMER_ARGS_CONVERT_EXCEPTION);
		}
		final String producerMessageId = messageData.getProducerMessageId();

		boolean isStore = annotation.storageStatus();
		if (isStore) {
			// 重复消费检查
			boolean consumed = reliableMessageService.checkMessageStatus(producerMessageId, MessageTypeEnum.CONSUMER_MESSAGE.messageType());
			if(consumed){
				reliableMessageService.confirmFinishMessage( messageData.getConfirmId());
				log.info("processMessageConsumerJoinPoint - 线程id={} 已经消费producerId为{} 的消息", Thread.currentThread().getId(),messageData.getProducerMessageId());
				return ;
			}
			reliableMessageService.confirmReceiveMessage(consumerGroup, messageData);
		}
		String methodName = joinPoint.getSignature().getName();
		try {
			joinPoint.proceed();
			reliableMessageService.confirmFinishMessage( messageData.getConfirmId());
			log.info("processMessageConsumerJoinPoint - 线程id={} 消费producerId为{} 的消息", Thread.currentThread().getId(),messageData.getProducerMessageId());
		} catch (Exception e) {
			log.error("发送可靠消息, 目标方法[{}], 出现异常={}", methodName, e.getMessage(), e);
			throw e;
		} finally {
			log.info("发送可靠消息 目标方法[{}], 总耗时={}", methodName, System.currentTimeMillis() - startTime);
		}
	}

	private MessageConsumer getAnnotation(JoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method.getAnnotation(MessageConsumer.class);
	}
}
