package com.reliable.message.client.aspect;

import com.reliable.message.client.annotation.MqProducerStore;
import com.reliable.message.client.service.MqMessageService;
import com.reliable.message.model.domain.MqMessageData;
import com.reliable.message.model.enums.DelayLevelEnum;
import com.reliable.message.model.enums.MqSendTypeEnum;
import lombok.extern.slf4j.Slf4j;
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
	@Value("${reliable.message.producerGroup}")
	private String producerGroup;

	/**
	 * Add exe time annotation pointcut.
	 */
	@Pointcut("@annotation(com.reliable.message.client.annotation.MqProducerStore)")
	public void mqProducerStoreAnnotationPointcut() {

	}

	/**
	 * Add exe time method object.
	 *
	 * @param joinPoint the join point
	 *
	 * @return the object
	 */
	@Around(value = "mqProducerStoreAnnotationPointcut()")
	public Object processMqProducerStoreJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
		log.info("processMqProducerStoreJoinPoint - 线程id={}", Thread.currentThread().getId());
		Object result;
		Object[] args = joinPoint.getArgs();
		MqProducerStore annotation = getAnnotation(joinPoint);
		MqSendTypeEnum type = annotation.sendType();
		int orderType = annotation.orderType().orderType();
		DelayLevelEnum delayLevelEnum = annotation.delayLevel();
		if (args.length == 0) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050005);
		}
		MqMessageData domain = null;
		for (Object object : args) {
			if (object instanceof MqMessageData) {
				domain = (MqMessageData) object;
				break;
			}
		}

		if (domain == null) {
//			throw new TpcBizException(ErrorCodeEnum.TPC10050005);
		}

		domain.setOrderType(orderType);
		domain.setProducerGroup(producerGroup);
		if (type == MqSendTypeEnum.WAIT_CONFIRM) {
			if (delayLevelEnum != DelayLevelEnum.ZERO) {
				domain.setDelayLevel(delayLevelEnum.delayLevel());
			}
//			domain.setStatus(MqMessageStatusEnum.WAIT_CONFIRM.messageStatus());
			mqMessageService.saveWaitConfirmMessage(domain);
		}
		result = joinPoint.proceed();
		if (type == MqSendTypeEnum.SAVE_AND_SEND) {
//			mqMessageService.saveAndSendMessage(domain);
		} else if (type == MqSendTypeEnum.DIRECT_SEND) {
//			mqMessageService.directSendMessage(domain);
		} else {
			//TODO 这里应该是异步发送确认消息的通知
			mqMessageService.confirmAndSendMessage(domain.getMessageKey());
		}
		return result;
	}

	private static MqProducerStore getAnnotation(JoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method.getAnnotation(MqProducerStore.class);
	}
}
