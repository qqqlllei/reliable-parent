package com.reliable.message.client.annotation;


import com.reliable.message.model.enums.DelayLevelEnum;
import com.reliable.message.model.enums.MessageSendTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MessageProducerStore {
	MessageSendTypeEnum sendType() default MessageSendTypeEnum.WAIT_CONFIRM;

	DelayLevelEnum delayLevel() default DelayLevelEnum.ZERO;
}
