package com.reliable.message.client.netty;

import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageFuture {
    private JSONObject requestMessage;
    private long timeout = 30 * 1000L;
    private long start = System.currentTimeMillis();
    private volatile Object resultMessage;
    private static final Object NULL = new Object();
    private final CountDownLatch latch = new CountDownLatch(1);

    public boolean isTimeout() {
        return System.currentTimeMillis() - start > timeout;
    }

    public Object get(long timeout, TimeUnit unit) throws TimeoutException,
        InterruptedException {
        boolean success = latch.await(timeout, unit);
        if (!success) {
            throw new TimeoutException("cost " + (System.currentTimeMillis() - start) + " ms");
        }

        if (resultMessage instanceof RuntimeException) {
            throw (RuntimeException)resultMessage;
        } else if (resultMessage instanceof Throwable) {
            throw new RuntimeException((Throwable)resultMessage);
        }

        return resultMessage;
    }

    public void setResultMessage(Object obj) {
        this.resultMessage = (obj == null ? NULL : obj);
        latch.countDown();
    }

    public JSONObject getRequestMessage() {
        return requestMessage;
    }


    public void setRequestMessage(JSONObject requestMessage) {
        this.requestMessage = requestMessage;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}