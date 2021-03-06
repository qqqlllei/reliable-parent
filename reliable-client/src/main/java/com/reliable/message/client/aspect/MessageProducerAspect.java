package com.reliable.message.client.aspect;

import com.reliable.message.client.annotation.MessageProducer;
import com.reliable.message.client.delay.DelayMessageTask;
import com.reliable.message.client.netty.NettyClient;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.domain.ReliableMessage;
import com.reliable.message.common.enums.*;
import com.reliable.message.common.exception.BusinessException;
import com.reliable.message.common.netty.message.DirectSendRequest;
import com.reliable.message.common.netty.message.SaveAndSendRequest;
import com.reliable.message.common.netty.message.WaitingConfirmRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeoutException;


/**
 * Created by 李雷
 */
@Slf4j
@Aspect
@Order
public class MessageProducerAspect {

	@Autowired
	private ReliableMessageService reliableMessageService;

	@Resource
	private NettyClient nettyClient;

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

		ReliableMessage domain = null;
		for (Object object : args) {
			if (object instanceof ReliableMessage) {
				domain = (ReliableMessage) object;
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

		domain.setMessageType(MessageTypeEnum.PRODUCER_MESSAGE.messageType());

		domain.setProducerGroup(producerGroup);

		if (type == MessageSendTypeEnum.WAIT_CONFIRM) {

			domain.setStatus(MessageSendStatusEnum.WAIT_CONFIRM.sendStatus());
			reliableMessageService.saveMessage(domain);
			try {
				WaitingConfirmRequest waitingConfirmRequest = new ModelMapper().map(domain, WaitingConfirmRequest.class);
                nettyClient.saveMessageWaitingConfirm(waitingConfirmRequest);
            }catch (TimeoutException e){
			    throw new RuntimeException(e.getCause());
            }

		}

		result = joinPoint.proceed();

		if (type == MessageSendTypeEnum.SAVE_AND_SEND) {

			domain.setStatus(MessageSendStatusEnum.SENDING.sendStatus());
			reliableMessageService.saveMessage(domain);

			SaveAndSendRequest saveAndSendRequest = new ModelMapper().map(domain, SaveAndSendRequest.class);
			nettyClient.saveAndSendMessage(saveAndSendRequest);
		} else if (type == MessageSendTypeEnum.DIRECT_SEND) {
			DirectSendRequest directSendRequest = new ModelMapper().map(domain, DirectSendRequest.class);
			nettyClient.directSendMessage(directSendRequest);
		} else if(type == MessageSendTypeEnum.WAIT_CONFIRM && !delayLevelEnum.equals(DelayLevelEnum.ZERO)) {
			messageTaskExecutor.execute(new DelayMessageTask(domain,delayMessageQueue,nettyClient));
		} else{
			nettyClient.confirmAndSendMessage(domain.getId());
		}

		return result;
	}

	private static MessageProducer getAnnotation(JoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method.getAnnotation(MessageProducer.class);
	}
}
