package com.reliable.message.common.netty.message;

import com.reliable.message.common.netty.message.Message;
import lombok.Data;

/**
 * Created by 李雷 on 2019/4/30.
 */
@Data
public class ResponseMessage extends Message {


    private String id;
    private int resultCode;
}
