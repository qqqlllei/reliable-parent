package com.reliable.message.server.stream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class MsgSink {

	@StreamListener(Sink.INPUT)
	public void messageSink(Object payload) {
		System.out.println("Received: " + payload);
	}


}