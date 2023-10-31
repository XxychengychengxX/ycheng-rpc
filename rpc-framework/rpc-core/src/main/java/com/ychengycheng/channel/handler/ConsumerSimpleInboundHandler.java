/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.handler;

import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.core.protection.CircuitBreaker;
import com.ychengycheng.emun.RespCode;
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
        log.info("The Response Of Request Id [{}] is ：\n [{}]", ychengRpcResponse.getRequestId(), ychengRpcResponse);
        CompletableFuture<Object> completableFuture = YchengYchengRPCBootstrap.PENDING_REQUEST.get(
                ychengRpcResponse.getRequestId());
        CircuitBreaker circuitBreaker = YchengYchengRPCBootstrap.getInstance().getConfiguration().getCircuitBreaker();
        //判断是否出现异常并进行一些记录
        if (ychengRpcResponse.getCode() == RespCode.FAIL.getCode()) {
            circuitBreaker.recordErrorRequest();
        } else {
            circuitBreaker.recordRequest();
        }
        Object retVal = ychengRpcResponse.getBody();
        completableFuture.complete(retVal);

        //channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("ozxjco".getBytes()));
    }
}
