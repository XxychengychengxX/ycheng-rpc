package com.ychengycheng.core.limit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Valar Morghulis
 * @Date 2023/9/7
 */
public class RequestFallback {

    //理论上又有三个状态：半开，全开，关闭
    private volatile boolean isOpen = false;


    //总请求数
    private AtomicInteger requestCount = new AtomicInteger(0);

    //异常的请求数
    private AtomicInteger errorRequestCount = new AtomicInteger(0);

    private int maxErrorCount;
    private float maxErrorPartition;

    public RequestFallback(int maxErrorCount, float maxErrorPartition) {
        this.maxErrorCount = maxErrorCount;
        this.maxErrorPartition = maxErrorPartition;
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        //todo:这里以后优化成可以先发送一个请求，如果失败不放行，成功了才放行
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequestCount.set(0);
    }

    /**
     * 判断是否被熔断
     *
     * @return 被熔断返回true否则返回false
     */
    public boolean isBreak() {
        //优先返回，否则判断是否指标是否达标
        if (isOpen) {
            return true;
        }
        if (errorRequestCount.get() > maxErrorCount) {
            this.isOpen = true;
            return true;
        }
        if (errorRequestCount.get() > 0 && requestCount.get() > 0 && errorRequestCount.get() / (float) requestCount.get() > maxErrorPartition) {
            this.isOpen = true;
            return true;
        }
        return false;
    }


    //每次发生请求之前,发生异常就进行记录
    public void recordRequest() {
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest() {
        this.errorRequestCount.getAndIncrement();
    }
}
