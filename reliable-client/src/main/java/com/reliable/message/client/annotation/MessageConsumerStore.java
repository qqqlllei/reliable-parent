package com.reliable.message.client.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MessageConsumerStore {

	boolean storePreStatus() default true;
}
