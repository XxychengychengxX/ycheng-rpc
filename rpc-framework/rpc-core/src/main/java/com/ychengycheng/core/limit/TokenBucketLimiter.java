package com.ychengycheng.core.limit;

/**
 * @author Valar Morghulis
 * @Date 2023/9/7
 */

public class TokenBucketLimiter {

    //代表令牌的数量，>0有令牌方形，<0则没有
    private int tokens;

    private int capacity;

    //没有令牌要均匀加令牌
    private int rate;

    //上一次放令牌的时间
    private Long lastTokenTime;

    public TokenBucketLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = 0;
    }

    /**
     * 判断请求是否可以放行
     *
     * @return 可以返回true，否则false
     */
    public synchronized boolean allowRequest() {
        //1.给令牌桶添加令牌
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        if (timeInterval>=1000){
            int needTokens = (int) timeInterval * rate / 1000;
            //添加令牌
            tokens = Math.min(capacity, tokens + needTokens);
            //标记最后一次放入令牌的时间
            lastTokenTime = System.currentTimeMillis();
            //2.自己获取令牌
        }
        if (tokens > 0) {
            tokens--;
            return true;
        } else {
            return false;
        }

    }
}

