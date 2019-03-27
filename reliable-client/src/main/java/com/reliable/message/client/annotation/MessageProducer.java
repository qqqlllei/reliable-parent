package com.reliable.message.client.annotation;


import com.reliable.message.common.enums.DelayLevelEnum;
import com.reliable.message.common.enums.GrayFlagEnum;
import com.reliable.message.common.enums.MessageSendTypeEnum;

import java.lang.annotation.*;
/**
 * Created by 李雷
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MessageProducer {

	MessageSendTypeEnum sendType() default MessageSendTypeEnum.WAIT_CONFIRM;

	DelayLevelEnum delayLevel() default DelayLevelEnum.ZERO;

	GrayFlagEnum grayMessage() default GrayFlagEnum.GRAY_MESSAGE;
}
