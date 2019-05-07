package com.reliable.message.common.netty.message;

import lombok.Data;

/**
 * Created by 李雷 on 2019/5/5.
 */
@Data
public class ClientRegisterRequest extends Message {

    private String applicationId;

}
