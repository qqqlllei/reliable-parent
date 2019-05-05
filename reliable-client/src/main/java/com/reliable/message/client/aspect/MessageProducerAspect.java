package com.reliable.message.client.aspect;

import com.reliable.message.client.annotation.MessageProducer;
import com.reliable.message.client.delay.DelayMessageTask;
import com.reliable.message.client.protocol.ProtocolManager;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.domain.ClientMessageData;
import com.reliable.message.common.enums.DelayLevelEnum;
import com.reliable.message.common.enums.ExceptionCodeEnum;
import com.reliable.message.common.enums.MessageSendTypeEnum;
import com.reliable.message.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.DelayQueue;


/**
 * Created by 李雷
 */
@Slf4j
@Aspect
public class MessageProducerAspect {

	@Autowired
	private ReliableMessageService reliableMessageService;

	@Resource
	private ProtocolManager protocolManager;

	@Value("${spring.application.name}")
	private String appName;


	@Value("${info.version}")
	private String serverVersion;


	@Resource(name = "messageTaskExecutor")
	private TaskExecutor messageTaskExecutor;

	@Autowired
	private DelayQueue<DelayMessageTask> delayMessageQueue;



	@Value("${reliable.message.producerGroup:}")
	private String producerGroup;

	@Pointcut("@annotation(com.reliable.message.client.annotation.MessageProducer)")
	public void messageProducerAnnotationPointcut() {

	}

	@Around(value = "messageProducerAnnotationPointcut()")
	public Object processMessageProducerJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
		log.info("processMessageProducerJoinPoint - 线程id={}", Thread.currentThread().getId());
		if(StringUtils.isBlank(producerGroup)) producerGroup = appName;
		Object result;
		Object[] args = joinPoint.getArgs();
		MessageProducer annotation = getAnnotation(joinPoint);
		MessageSendTypeEnum type = annotation.sendType();

		DelayLevelEnum delayLevelEnum = annotation.delayLevel();

		if (args.length == 0) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_IS_NULL);
		}

		ClientMessageData domain = null;
		for (Object object : args) {
			if (object instanceof ClientMessageData) {
				domain = (ClientMessageData) object;
				break;
			}
		}

		if (domain == null) {
			throw new BusinessException(ExceptionCodeEnum.MSG_PRODUCER_ARGS_TYPE_IS_WRONG);
		}

		domain.initParam(delayLevelEnum.delayLevel());

		if(annotation.grayMessage().isGray()){
			domain.setMessageVersion(serverVersion);
		}

		domain.setProducerGroup(producerGroup);

		if (type == MessageSendTypeEnum.WAIT_CONFIRM) {
//			reliableMessageService.saveWaitConfirmMessage(domain); //原方法

			reliableMessageService.saveProducerMessage(domain);
			protocolManager.getMessageProtocol().saveMessageWaitingConfirm(domain);
		}

		result = joinPoint.proceed();

		if (type == MessageSendTypeEnum.SAVE_AND_SEND) {
			protocolManager.getMessageProtocol().saveAndSendMessage(domain);
//			reliableMessageService.saveAndSendMessage(domain);
		} else if (type == MessageSendTypeEnum.DIRECT_SEND) {

			protocolManager.getMessageProtocol().directSendMessage(domain);
//			reliableMessageService.directSendMessage(domain);
		} else if(type == MessageSendTypeEnum.WAIT_CONFIRM && !delayLevelEnum.equals(DelayLevelEnum.ZERO)) {
			messageTaskExecutor.execute(new DelayMessageTask(domain,delayMessageQueue,reliableMessageService));
		} else{
//			reliableMessageService.confirmAndSendMessage(domain.getId()); 原方法

			protocolManager.getMessageProtocol().confirmAndSendMessage(domain.getId());
		}

		return result;
	}

	private static MessageProducer getAnnotation(JoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method.getAnnotation(MessageProducer.class);
	}
}
