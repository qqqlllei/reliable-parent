package com.reliable.message.common.util;

import java.util.UUID;

/**
 * Created by 李雷 on 2019/3/20.
 */
public class UUIDUtil {

    public static String getId(){
       return UUID.randomUUID().toString().replace("-", "");
    }
}
