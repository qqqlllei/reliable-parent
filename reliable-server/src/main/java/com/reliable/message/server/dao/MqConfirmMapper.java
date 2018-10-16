package com.reliable.message.server.dao;

import com.reliable.message.server.domain.MessageConfirm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/14.
 */
public interface MqConfirmMapper {

    void batchCreateMqConfirm(@Param("tpcMqConfirmList") List<MessageConfirm> list);

    void confirmFinishMessage(@Param("consumerGroup") String consumerGroup,@Param("producerMessageId") String producerMessageId);

    int getMessageConfirmCountByProducerMessageId(String producerMessageId);

    List<MessageConfirm> getMessageConfirmsByProducerMessageId(String producerMessageId);
}
