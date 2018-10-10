package com.reliable.message.client.autoconfigure;

import com.reliable.message.client.aspect.MqConsumerStoreAspect;
import com.reliable.message.client.aspect.MqProducerStoreAspect;
import com.reliable.message.client.job.ClientMessageDataflow;
import com.reliable.message.client.service.MqMessageService;
import com.reliable.message.client.service.impl.MqMessageServiceImpl;
import com.reliable.message.client.web.ReliableController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageBeanConfiguration {


	@Value("${reliable.message.reliableMessageProducer:false}")
	private boolean reliableMessageProducer;


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

	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageConsumer:false} || ${reliable.message.reliableMessageProducer:false}")
	public MqMessageService mqMessageService(){
		return new MqMessageServiceImpl();
	}


	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageProducer:false}")
	public ClientMessageDataflow clientMessageDataflow(){
		return new ClientMessageDataflow();
	}

}
