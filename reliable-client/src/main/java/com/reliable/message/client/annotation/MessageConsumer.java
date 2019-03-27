package com.reliable.message.client.annotation;

import java.lang.annotation.*;
/**
 * Created by 李雷
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MessageConsumer {

	boolean storageStatus() default true;
}
