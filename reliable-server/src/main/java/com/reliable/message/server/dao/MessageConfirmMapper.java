package com.reliable.message.server.dao;

import com.reliable.message.server.domain.MessageConfirm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/14.
 */
public interface MessageConfirmMapper {

    void batchCreateMqConfirm(@Param("tpcMqConfirmList") List<MessageConfirm> list);

    void confirmFinishMessage(@Param("consumerGroup") String consumerGroup,@Param("producerMessageId") String producerMessageId);

    int getMessageConfirmCountByProducerMessageId(String producerMessageId);

    List<MessageConfirm> getMessageConfirmsByProducerMessageId(String producerMessageId);

    void updateById(MessageConfirm messageConfirm);
}
