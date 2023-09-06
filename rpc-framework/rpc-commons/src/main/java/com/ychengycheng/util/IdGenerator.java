/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
package com.ychengycheng.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.atomic.LongAdder;

@Data
@AllArgsConstructor

public class IdGenerator {

    //单机版的线程安全
    /*private static LongAdder longAdder =new LongAdder();
    public static long getId(){
        longAdder.increment();
        return longAdder.sum();
    }*/

    /*
     * 手写一个雪花算法
     * 1.机房号 5bit
     * 2.机器号 5bit
     * 3.时间戳 42bit
     * 4.序列号 12bit
     * 同一个机房同一个机器号的同一个时间因为并发量大才会需要多个id
     * */
    //起始时间戳
    public static final long START_TIMESTAMP = DateUtil.get("2022-1-1").getTime();
    //定义bit位
    /**
     * 机房比特位
     */
    public static final long DATACENTER_BIT = 5L;
    /**
     * 机器比特位
     */
    public static final long MACHINE_BIT = 5L;
    /**
     * 序列号比特位
     */
    public static final long SEQUENCE_BIT =12L;
    /**
     * 数据中心的最大数
     */
    public static final long DATACENTER_MAX = ~(-1L << DATACENTER_BIT);
    /**
     * 每个数据中心最多的机器数
     */
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    public static final long TIMESTAMP_SHIFTLEFT = DATACENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATACENTER_SHIFTLEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_SHIFTLEFT = SEQUENCE_BIT;
    public long lastTimestamp = -1L;
    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();

    public IdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > DATACENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("传入的数据中心编号或机器号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {

        long currentTime = System.currentTimeMillis();
        long timestamp = currentTime - START_TIMESTAMP;
        //判断时间回拨
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("服务器进行了时间回调！！");
        }
        //对sequenceId进行处理，如果是同一个时间节点，则自增
        if (timestamp == lastTimestamp) {
            sequenceId.increment();
        } else {
            sequenceId.reset();
        }
        long sum = sequenceId.sum();
        return timestamp << TIMESTAMP_SHIFTLEFT | dataCenterId << DATACENTER_SHIFTLEFT | machineId << MACHINE_SHIFTLEFT | sum;
    }
}
