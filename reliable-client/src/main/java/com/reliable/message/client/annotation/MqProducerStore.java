package com.reliable.message.client.annotation;


import com.reliable.message.model.enums.DelayLevelEnum;
import com.reliable.message.model.enums.MqOrderTypeEnum;
import com.reliable.message.model.enums.MqSendTypeEnum;

import java.lang.annotation.*;


/**
 * 保存生产者消息
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MqProducerStore {
	MqSendTypeEnum sendType() default MqSendTypeEnum.WAIT_CONFIRM;

	MqOrderTypeEnum orderType() default MqOrderTypeEnum.ORDER;

	DelayLevelEnum delayLevel() default DelayLevelEnum.ZERO;
}
