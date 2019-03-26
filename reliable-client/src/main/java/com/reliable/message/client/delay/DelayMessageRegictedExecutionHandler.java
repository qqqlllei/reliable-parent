package com.reliable.message.client.delay;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by 李雷 on 2019/3/26.
 */
public class DelayMessageRegictedExecutionHandler  implements RejectedExecutionHandler {

    private static final Log LOGER = LogFactory.getLog(DelayMessageRegictedExecutionHandler.class);
    @Autowired
    private DelayQueue<DelayMessageTask> delayMessageQueue;

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if(r instanceof DelayMessageTask){
            DelayMessageTask delayMessageTask = (DelayMessageTask) r;
            delayMessageQueue.remove(delayMessageTask);
            LOGER.info("===================DelayMessageRegictedExecutionHandler.delayMessageQueue size :"+ delayMessageQueue.size());
        }
    }
}
