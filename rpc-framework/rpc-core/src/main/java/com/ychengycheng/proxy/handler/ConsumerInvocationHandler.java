/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.proxy.handler;

import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.channel.initializer.NettyBootstrapInitializer;
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.exception.NettyExcetion;
import com.ychengycheng.message.RequestPayload;
import com.ychengycheng.message.YchengRpcRequest;
import com.ychengycheng.util.compress.CompressorFactory;
import com.ychengycheng.util.serialize.SerializeFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 该类封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在invoke方法中 1.发现可用服务 2.建立链接 3.发送请求 4.得到结果
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConsumerInvocationHandler implements InvocationHandler {

    /**
     * 注册中心
     */
    private RegistryConfig registryConfig;
    /**
     * 接口
     */
    private Class<?> interfaceRef;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        log.info("method--->{}", method);
        log.info("args--->{}", args);

        String name = interfaceRef.getName();
        //1.服务发现（使用负载均衡器）

        /* String ip = instance.getIp();*/
        /* int port = instance.getPort();*/
        /* InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);*/
        InetSocketAddress address = YchengYchengRPCBootstrap.loadBalancer.selectServiceAddress(name);
        //InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8848);


        //2.创建channel
        Channel channel = getAvaiChannel(address);

        /*
         * --------------------------------同步策略----------------------------
         * 每次请求都阻塞，显然不合适
         *

            ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
            //简单的api get()阻塞获取结果，getNow获取当前的结果，如果未处理完成
            if (channelFuture.isDone()) {
                Object object = channelFuture.getNow();
            } else if (!channelFuture.isSuccess()) {
                Throwable cause = channelFuture.cause();
                throw new RuntimeException(cause);
            }*/


        /*--------------------------封装报文----------------------*/
        RequestPayload payload = RequestPayload.builder()
                                               .interfaceName(interfaceRef.getName())
                                               .methodName(method.getName())
                                               .parametersType(method.getParameterTypes())
                                               .parameterValue(args)
                                               .returnType(method.getReturnType())
                                               .build();
        //todo：这里的请求id，压缩类型和请求类型等数据还要改变
        YchengRpcRequest ychengRpcRequest = YchengRpcRequest.builder()
                                                            .requestId(YchengYchengRPCBootstrap.ID_GENERATOR.getId())
                                                            .compressType(CompressorFactory.getCompressor(
                                                                    YchengYchengRPCBootstrap.compressType).getCode())
                                                            .requestType((byte) 1)
                                                            .serializeType(SerializeFactory.getSerializer(
                                                                    YchengYchengRPCBootstrap.serializeType).getCode())
                                                            .requestPayload(payload)
                                                            .timeStamp(System.currentTimeMillis())
                                                            .build();
        //将请求存入本地线程池
        YchengYchengRPCBootstrap.REQUEST_THREAD_LOCAL.set(ychengRpcRequest);
        /*
         * --------------------------------异步策略----------------------------
         * */
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        YchengYchengRPCBootstrap.PENDING_REQUEST.put(ychengRpcRequest.getRequestId(), completableFuture);
        //这里writeAndFlush 写出一个请求，这个请求实例会进入pipeline进行请求出站的一系列操作
        channel.writeAndFlush(ychengRpcRequest).sync().addListener((promise) -> {
            //只需要处理以下异常
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        //清理threadLocal
        YchengYchengRPCBootstrap.REQUEST_THREAD_LOCAL.remove();
        return completableFuture.get(10, TimeUnit.SECONDS);
        /*
         * 这里要做什么
         * 1.发现服务，从注册中心，寻找一个可用的服务
         * 2.使用netty链接服务器，发送调用的服务（服务名字，方法名字，参数列表），得到结果
         * */
        //todo:这里只针对nacos，后面要设计出一个高可用的还需要更多

            /*String applicationName = "first-provider";
            String serverType = registryConfig.getServerType();
            if (serverType.equals(RegisterConfigConstant.NACOS)) {
                NamingService namingService = NacosUtil.getNamingService(registryConfig);
                List<Instance> allInstances = namingService.getAllInstances(applicationName);
                for (Instance instance : allInstances) {
                    log.info("服务调用方，发现了服务实例：{}", instance.toInetAddr());
                }
            }*/
        //2.用netty链接服务器并进行消息发送


    }

    private Channel getAvaiChannel(InetSocketAddress inetSocketAddress) throws NettyExcetion {
        //1.尝试从缓存中获取
        Channel channel = YchengYchengRPCBootstrap.CHANNEL_CACHE.get(inetSocketAddress);
        //2.如果缓存中获取不到。执行以下操作
        if (channel == null) {/*
             await方法会阻塞，会等待链接成功再返回，netty还提供了异步处理的逻辑
             sync和await都是阻塞当前线程，获取返回值（链接的过程是异步的，发送数据的过程是异步的）
             如果发生了异常，sync回主动在主线程抛出异常，await不会，子线程，需要future中处理
             channel = NettyBootstrapInitializer.getBootstrap()
                                                .connect(inetSocketAddress)
                                                .await()
                                                .channel();*/
            //2.1从completableFuture中获取对象
            CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap()
                                     .connect(inetSocketAddress)
                                     .addListener((ChannelFutureListener) promise -> {
                                         if (promise.isDone()) {
                                             //异步的
                                             channelCompletableFuture.complete(promise.channel());
                                         } else if (!promise.isSuccess()) {
                                             channelCompletableFuture.completeExceptionally(promise.cause());
                                         }
                                     });
            try {
                channel = channelCompletableFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发生异常：" + e.getMessage());
                throw new NettyExcetion(e);
            }

            //将completableFuture暴露出去
            YchengYchengRPCBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
        }
        //3.如果进行操作后channel仍然为空，则抛异常

        if (channel == null) {
            log.error("获取建立与【{}】的通道时发生了异常",
                      registryConfig.getServerAddr() + registryConfig.getServerPort());
        }
        return channel;
    }
}
