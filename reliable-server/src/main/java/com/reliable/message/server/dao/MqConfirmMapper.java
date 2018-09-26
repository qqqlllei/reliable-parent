package com.reliable.message.server.dao;

import com.reliable.message.server.domain.TpcMqConfirm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/14.
 */
public interface MqConfirmMapper {

    void batchCreateMqConfirm(@Param("tpcMqConfirmList") List<TpcMqConfirm> list);

    void confirmFinishMessage(@Param("consumerGroup") String consumerGroup,@Param("producerMessageId") String producerMessageId);
}
