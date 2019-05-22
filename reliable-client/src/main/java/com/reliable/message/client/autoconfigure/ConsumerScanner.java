package com.reliable.message.client.autoconfigure;

import com.reliable.message.client.annotation.MessageConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.task.TaskExecutor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Properties;

/**
 * Created by 李雷 on 2019/5/22.
 */
public class ConsumerScanner implements BeanPostProcessor {

    private TaskExecutor consumerTaskExecutor;


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Method[] methods = AopUtils.getTargetClass(bean).getMethods();




        for (Method method: methods){
            MessageConsumer messageConsumer = method.getAnnotation(MessageConsumer.class);
            if(messageConsumer !=null){
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(new Properties());
                consumer.subscribe(Collections.singletonList(messageConsumer.topics()));
                consumerTaskExecutor.execute(() -> {
                    while (true){
                        ConsumerRecords<String, String> records =  consumer.poll(1000l);
                    }
                });
            }
        }
        return null;
    }
}
