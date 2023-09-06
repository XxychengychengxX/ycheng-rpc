/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.handler;

import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.message.YchengRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class ConsumerSimpleInboundHandler extends SimpleChannelInboundHandler<YchengRpcResponse> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                YchengRpcResponse ychengRpcResponse) throws Exception {
        //返回值结果
        Object retVal = ychengRpcResponse.getBody();
        log.info("收到的请求报文对象如下：【{}】", ychengRpcResponse);
        CompletableFuture<Object> completableFuture = YchengYchengRPCBootstrap.PENDING_REQUEST.get(
                ychengRpcResponse.getRequestId());
        completableFuture.complete(retVal);

        //channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("ozxjco".getBytes()));
    }
}
