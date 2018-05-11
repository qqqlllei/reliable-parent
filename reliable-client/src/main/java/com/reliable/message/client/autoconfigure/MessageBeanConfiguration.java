package com.reliable.message.client.autoconfigure;

import com.reliable.message.client.aspect.MqProducerStoreAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class MessageBeanConfiguration {
	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageProducer:true}")
	public MqProducerStoreAspect mqProducerStoreAspect() {
		return new MqProducerStoreAspect();
	}
}
