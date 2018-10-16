package com.reliable.message.client.aspect;

import com.reliable.message.client.annotation.MqProducerStore;
import com.reliable.message.client.service.MqMessageService;
import com.reliable.message.model.domain.ClientMessageData;
import com.reliable.message.model.enums.DelayLevelEnum;
import com.reliable.message.model.enums.MqMessageStatusEnum;
import com.reliable.message.model.enums.MqSendTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.lang.reflect.Method;


/**
 * 生产者切面
 */
@Slf4j
@Aspect
public class MqProducerStoreAspect {
	@Resource
	private MqMessageService mqMessageService;
	@Value("${spring.application.name}")
	private String appName;



	@Value("${reliable.message.producerGroup:}")
	private String producerGroup;

	@Pointcut("@annotation(com.reliable.message.client.annotation.MqProducerStore)")
	public void mqProducerStoreAnnotationPointcut() {

	}

	@Around(value = "mqProducerStoreAnnotationPointcut()")
	public Object processMqProducerStoreJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
		log.info("processMqProducerStoreJoinPoint - 线程id={}", Thread.currentThread().getId());
		if(StringUtils.isBlank(producerGroup)) producerGroup = appName;
		Object result;
		Object[] args = joinPoint.getArgs();
		MqProducerStore annotation = getAnnotation(joinPoint);
		MqSendTypeEnum type = annotation.sendType();
		int orderType = annotation.orderType().orderType();
		DelayLevelEnum delayLevelEnum = annotation.delayLevel();
		if (args.length == 0) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050005);
		}
		ClientMessageData domain = null;
		for (Object object : args) {
			if (object instanceof ClientMessageData) {
				domain = (ClientMessageData) object;
				break;
			}
		}

		if (domain == null) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050005);
		}

		if(domain.getId() == null){
//			throw new TpcBizException(ErrorCodeEnum.TPC10050005);
		}

		domain.setOrderType(orderType);
		domain.setProducerGroup(producerGroup);

		if (type == MqSendTypeEnum.WAIT_CONFIRM) {
			if (delayLevelEnum != DelayLevelEnum.ZERO) {
				domain.setDelayLevel(delayLevelEnum.delayLevel());
			}
			mqMessageService.saveWaitConfirmMessage(domain);
		}
		result = joinPoint.proceed();
		if (type == MqSendTypeEnum.SAVE_AND_SEND) {
			mqMessageService.saveAndSendMessage(domain);
		} else if (type == MqSendTypeEnum.DIRECT_SEND) {
			mqMessageService.directSendMessage(domain);
		} else {
			mqMessageService.confirmAndSendMessage(domain.getProducerGroup()+"-"+domain.getId());
		}
		return result;
	}

	private static MqProducerStore getAnnotation(JoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method.getAnnotation(MqProducerStore.class);
	}
}
