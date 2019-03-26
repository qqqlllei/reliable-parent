package com.reliable.message.client.autoconfigure;

import com.reliable.message.client.aspect.MessageConsumerAspect;
import com.reliable.message.client.aspect.MessageProducerAspect;
import com.reliable.message.client.delay.DelayMessageRegictedExecutionHandler;
import com.reliable.message.client.delay.DelayMessageTask;
import com.reliable.message.client.job.ClientMessageDataflow;
import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.client.service.impl.ReliableMessageServiceImpl;
import com.reliable.message.client.web.ReliableController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class MessageBeanConfiguration {

	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageProducer:false}")
	public MessageProducerAspect messageProducerStoreAspect() {
		return new MessageProducerAspect();
	}


	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageConsumer:false}")
	public MessageConsumerAspect messageConsumerStoreAspect() {
		return new MessageConsumerAspect();
	}

	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageConsumer:false} || ${reliable.message.reliableMessageProducer:false}")
	public ReliableController reliableController(){
		return new ReliableController();
	}

	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageConsumer:false} || ${reliable.message.reliableMessageProducer:false}")
	public ReliableMessageService reliableMessageService(){
		return new ReliableMessageServiceImpl();
	}


	@Bean
	@ConditionalOnExpression("${reliable.message.producerMessageDelteFlag:false}")
	public ClientMessageDataflow clientMessageDataflow(){
		return new ClientMessageDataflow();
	}



	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageProducer:false}")
	public TaskExecutor messageTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 设置核心线程数
		executor.setCorePoolSize(10);
		// 设置最大线程数
		executor.setMaxPoolSize(20);
		// 设置队列容量
		executor.setQueueCapacity(200);
		// 设置线程活跃时间（秒）
		executor.setKeepAliveSeconds(60);
		// 设置默认线程名称
		executor.setThreadNamePrefix("delay-message-thread-");
		// 设置拒绝策略
		executor.setRejectedExecutionHandler(delayMessageRegictedExecutionHandler());
		return executor;
	}


	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageProducer:false}")
	public DelayQueue<DelayMessageTask> delayQueue() {
		return new DelayQueue<>();
	}

	@Bean
	@ConditionalOnExpression("${reliable.message.reliableMessageProducer:false}")
	public DelayMessageRegictedExecutionHandler delayMessageRegictedExecutionHandler(){
		return  new DelayMessageRegictedExecutionHandler();
	}

}
