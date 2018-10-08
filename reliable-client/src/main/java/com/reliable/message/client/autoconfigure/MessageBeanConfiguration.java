package com.reliable.message.client.autoconfigure;

import com.reliable.message.client.aspect.MqConsumerStoreAspect;
import com.reliable.message.client.aspect.MqProducerStoreAspect;
import com.reliable.message.client.web.ReliableController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageBeanConfiguration {
	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageProducer:false}")
	public MqProducerStoreAspect mqProducerStoreAspect() {
		return new MqProducerStoreAspect();
	}


	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageConsumer:false}")
	public MqConsumerStoreAspect mqConsumerStoreAspect() {
		return new MqConsumerStoreAspect();
	}

	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageConsumer:false}")
	public ReliableController reliableController(){
		return new ReliableController();
	}
}
