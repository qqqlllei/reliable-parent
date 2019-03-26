package com.reliable.message.client.delay;

import com.reliable.message.client.service.ReliableMessageService;
import com.reliable.message.common.domain.ClientMessageData;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by 李雷 on 2019/3/26.
 */
@Data
public class DelayMessageTask implements Runnable, Delayed{

    private static final Log LOGER = LogFactory.getLog(DelayMessageTask.class);
    private long executeTime;
    private ClientMessageData clientMessageData;
    private DelayQueue<DelayMessageTask> delayMessageQueue;
    private ReliableMessageService reliableMessageService;

    public DelayMessageTask(ClientMessageData clientMessageData,DelayQueue delayMessageQueue,ReliableMessageService reliableMessageService){
        this.clientMessageData = clientMessageData;
        this.executeTime = clientMessageData.getSendTime().getTime();
        this.delayMessageQueue = delayMessageQueue;
        this.reliableMessageService = reliableMessageService;
        this.delayMessageQueue.add(this);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return executeTime - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        DelayMessageTask delayMessageTask = (DelayMessageTask) o;
        return executeTime > delayMessageTask.getExecuteTime() ? 1:  (executeTime < delayMessageTask.executeTime ? -1 : 0);
    }

    @Override
    public void run() {
        try {
            DelayMessageTask delayMessageTask = delayMessageQueue.take();
            reliableMessageService.confirmAndSendMessage(delayMessageTask.getClientMessageData().getProducerMessageId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return "DelayMessageTask(executeTime=" + this.getExecuteTime() + ", clientMessageData=" + this.getClientMessageData()+ ")";
    }

}
