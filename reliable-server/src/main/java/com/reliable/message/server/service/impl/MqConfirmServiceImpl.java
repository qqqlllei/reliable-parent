package com.reliable.message.server.service.impl;

import com.reliable.message.server.domain.TpcMqConfirm;
import com.reliable.message.server.service.MqConfirmService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 李雷 on 2018/5/11.
 */
@Service
public class MqConfirmServiceImpl implements MqConfirmService{
    @Override
    public void batchCreateMqConfirm(List<TpcMqConfirm> list) {

    }
}
