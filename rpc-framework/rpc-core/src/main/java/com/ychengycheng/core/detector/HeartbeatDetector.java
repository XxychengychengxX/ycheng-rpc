package com.ychengycheng.core.detector;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.channel.initializer.NettyBootstrapInitializer;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.emun.RequestType;
import com.ychengycheng.message.YchengRpcRequest;
import com.ychengycheng.util.compress.CompressorFactory;
import com.ychengycheng.util.serialize.SerializeFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * 心跳探测的核心目的是什么？探活，感知哪些服务器的连接状态是正常的，哪些是不正常的
 */
@Slf4j
public class HeartbeatDetector {

    //ThreadPoolExecutor = new ThreadPoolExecutor(1,8,30,TimeUnit.SECONDS,);

    public static void detectHeartbeat(String serviceName) {
        // 1、从注册中心拉取服务列表并建立连接
        RegistryCenter registry = YchengYchengRPCBootstrap.getInstance().getRegistryCenter();
        List<Instance> instances = registry.lookup(serviceName);


        // 将连接进行缓存
        for (Instance instance : instances) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(instance.getIp(), instance.getPort());

            try {
                if (!YchengYchengRPCBootstrap.CHANNEL_CACHE.containsKey(inetSocketAddress)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap()
                                                               .connect(inetSocketAddress)
                                                               .sync()
                                                               .channel();
                    YchengYchengRPCBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        // 3、任务，定期发送消息
        ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();
        scheduledThread.scheduleAtFixedRate(new MyTimerTask(), 0, 2, TimeUnit.SECONDS);


    }

    private static class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            // 将响应时长的map清空

            YchengYchengRPCBootstrap.ANSWER_TIME_TREEMAP.clear();

            // 遍历所有的channel
            Map<InetSocketAddress, Channel> cache = YchengYchengRPCBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                // 定义一个重试的次数
                int tryTimes = 3;
                long id = YchengYchengRPCBootstrap.ID_GENERATOR.getId();
                while (tryTimes > 0) {
                    // 通过心跳检测处理每一个channel
                    Channel channel = entry.getValue();

                    long start = System.currentTimeMillis();
                    // 构建一个心跳请求
                    YchengRpcRequest yrpcRequest = YchengRpcRequest.builder()
                                                                   .requestId(id)
                                                                   .compressType(CompressorFactory.getCompressor(
                                                                                                          YchengYchengRPCBootstrap.compressType)
                                                                                                  .getCode())
                                                                   .requestType(RequestType.HEART_BEAT.getId())
                                                                   .serializeType(SerializeFactory.getSerializer(
                                                                                                          YchengYchengRPCBootstrap.serializeType)
                                                                                                  .getCode())
                                                                   .timeStamp(System.currentTimeMillis())
                                                                   .build();

                    // 4、写出报文
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    // 将 completableFuture 暴露出去
                    YchengYchengRPCBootstrap.PENDING_REQUEST.put(yrpcRequest.getRequestId(), completableFuture);

                    channel.writeAndFlush(yrpcRequest).addListener((ChannelFutureListener) promise -> {
                        if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });
                    //如果有问题进行服务重试

                    Long endTime = 0L;
                    try {
                        // 阻塞方法，get()方法如果得不到结果，就会一直阻塞
                        completableFuture.get(2, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        // 一旦发生问题，需要优先重试
                        tryTimes--;
                        log.warn("和地址为【{}】的主机连接发生异常.正在进行第【{}】次重试......", channel.remoteAddress(),
                                 3 - tryTimes);

                        // 将重试的机会用尽，将失效的地址移出服务列表
                        if (tryTimes == 0) {
                            YchengYchengRPCBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }

                        // 尝试等到一段时间后重试
                        try {
                            Thread.sleep(10 * (new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }

                        continue;
                    }
                    Long time = endTime - start;

                    // 使用treemap进行缓存
                    YchengYchengRPCBootstrap.ANSWER_TIME_TREEMAP.put(time, channel);
                    log.debug("和[{}]服务器的响应时间是[{}].", entry.getKey(), time);
                    break;
                }
                if (tryTimes == 0) {
                    log.error("链接服务器失败，请稍后再试!");
                    YchengYchengRPCBootstrap.PENDING_REQUEST.remove(id);
                }
            }

            //log.info("-----------------------响应时间的treemap----------------------");
            /*for (Map.Entry<Long, Channel> entry : YchengYchengRPCBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}]--->channelId:[{}]", entry.getKey(), entry.getValue().id());
                }
            }*/
        }
    }

}
