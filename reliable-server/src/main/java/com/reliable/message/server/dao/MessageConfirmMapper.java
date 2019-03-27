package com.reliable.message.server.dao;

import com.alibaba.fastjson.JSONObject;
import com.reliable.message.server.domain.MessageConfirm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/14.
 */
public interface MessageConfirmMapper {

    void batchCreateMqConfirm(@Param("tpcMqConfirmList") List<MessageConfirm> list);

    void confirmFinishMessage(@Param("id") String confirmId);

    int getMessageConfirmCountByProducerMessageId(String producerMessageId);

    List<MessageConfirm> getMessageConfirmsByProducerMessageId(String producerMessageId);

    void updateById(MessageConfirm messageConfirm);

    List<MessageConfirm> getUnConfirmMessage(JSONObject jobTaskParameter);

    List<MessageConfirm> getMessageConfirmsByMessageId(@Param("messageId") String messageId);

    void deleteMessageConfirmById(@Param("id") String id);
}
