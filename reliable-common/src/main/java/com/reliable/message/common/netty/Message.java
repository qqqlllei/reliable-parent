package com.reliable.message.common.netty;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by 李雷 on 2019/4/30.
 */
@Data
public class Message implements Serializable{
    private static final long serialVersionUID = 1L;
    private int messageType;

}
