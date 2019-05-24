package com.reliable.message.server.domain;

import lombok.Data;

/**
 * Created by 李雷 on 2019/5/24.
 */
@Data
public class MessageConsumer {

    private String id;
    private String consumerGroup;
    private String topic;
}
