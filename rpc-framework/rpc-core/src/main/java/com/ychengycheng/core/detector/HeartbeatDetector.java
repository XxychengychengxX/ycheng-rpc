package com.ychengycheng.core.detector;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.channel.initializer.NettyBootstrapInitializer;
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.core.compress.CompressorFactory;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.core.serialize.SerializerFactory;
import com.ychengycheng.emun.RequestType;
import com.ychengycheng.message.YchengRpcRequest;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * 心跳探测的核心目的是什么？探活，感知哪些服务器的连接状态是正常的，哪些是不正常的
 */
@Slf4j
public class HeartbeatDetector {


    /**
     * 每次调用不同的服务之前都需要
     */
    private static final ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();

    //ThreadPoolExecutor = new ThreadPoolExecutor(1,8,30,TimeUnit.SECONDS,);

    public static void detectHeartbeat(String serviceName) {

        // 3、任务，定期发送消息
        ScheduledFuture<?> scheduledFuture = scheduledThread.scheduleAtFixedRate(
                new HeartBeatDetectorTimerTask(serviceName), 1, 5, TimeUnit.SECONDS);
        try {
            scheduledFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static class HeartBeatDetectorTimerTask extends TimerTask {

        public HeartBeatDetectorTimerTask(String serviceName) {
            // 1、从注册中心拉取服务列表并建立连接
            RegistryCenter registry = YchengYchengRPCBootstrap.getInstance().getConfiguration().getRegistryCenter();
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
            Properties properties = new Properties();
            RegistryConfig registryConfig = YchengYchengRPCBootstrap.getInstance()
                                                                    .getConfiguration()
                                                                    .getRegistryConfig();
            properties.put(PropertyKeyConst.SERVER_ADDR,
                           registryConfig.getServerAddr() + ":" + registryConfig.getServerPort());
            properties.put(PropertyKeyConst.USERNAME, "nacos");
            properties.put(PropertyKeyConst.PASSWORD, "nacos");
            try {
                NamingService namingService = NamingFactory.createNamingService(properties);
                //namingService.subscribe(serviceName, new OnLineStatusWatcher());
            } catch (NacosException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            // 将响应时长的map清空
            YchengYchengRPCBootstrap.ANSWER_TIME_TREEMAP.clear();
            // 遍历所有的channel
            synchronized (YchengYchengRPCBootstrap.CHANNEL_CACHE) {
                Map<InetSocketAddress, Channel> cache = YchengYchengRPCBootstrap.CHANNEL_CACHE;
                for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                    // 定义一个重试的次数
                    int tryTimes = 3;
                    long id = YchengYchengRPCBootstrap.getInstance().getConfiguration().getIdGenerator().getId();
                    while (tryTimes > 0) {

                        // 通过心跳检测处理每一个channel
                        Channel channel = entry.getValue();
                        long start = System.currentTimeMillis();
                        // 构建一个心跳请求
                        YchengRpcRequest yrpcRequest = YchengRpcRequest.builder()
                                                                       .requestId(id)
                                                                       .compressType(CompressorFactory.getCompressor(
                                                                                                              YchengYchengRPCBootstrap.getInstance()
                                                                                                                                      .getConfiguration()
                                                                                                                                      .getProtocolConfig()
                                                                                                                                      .getCompressType())
                                                                                                      .getCode())
                                                                       .requestType(RequestType.HEART_BEAT.getId())
                                                                       .serializeType(SerializerFactory.getSerializer(
                                                                                                               YchengYchengRPCBootstrap.getInstance()
                                                                                                                                       .getConfiguration()
                                                                                                                                       .getProtocolConfig()
                                                                                                                                       .getSerializeType())
                                                                                                       .getCode())
                                                                       .timeStamp(System.currentTimeMillis())
                                                                       .build();

                        // 4、写出报文
                        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                        // 将 completableFuture 暴露出去
                        YchengYchengRPCBootstrap.PENDING_REQUEST.put(yrpcRequest.getRequestId(), completableFuture);
                        long endTime;

                        try {
                            channel.writeAndFlush(yrpcRequest).sync();
                            //如果有问题进行服务重试
                            // 阻塞方法，get()方法如果得不到结果，就会一直阻塞
                            completableFuture.get(2, TimeUnit.SECONDS);
                            endTime = System.currentTimeMillis();
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            // 一旦发生问题，需要优先重试
                            tryTimes--;
                            log.warn("The connection to host [{}] is abnormal. [{}] retry underway......",
                                     channel.remoteAddress(), 3 - tryTimes);


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
                        log.debug(" the response time of [{}] server is [{}].", entry.getKey(), time);
                        break;
                    }
                    if (tryTimes == 0) {
                        log.error("Failed to connect to the server, please try again later!");
                        YchengYchengRPCBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                    }
                    YchengYchengRPCBootstrap.PENDING_REQUEST.remove(id);

                }
            }


            //log.info("-----------------------响应时间的treemap----------------------");
            for (Map.Entry<Long, Channel> entry : YchengYchengRPCBootstrap.ANSWER_TIME_TREEMAP.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}]--->channelId:[{}]", entry.getKey(), entry.getValue().id());
                }
            }
        }
    }

}
