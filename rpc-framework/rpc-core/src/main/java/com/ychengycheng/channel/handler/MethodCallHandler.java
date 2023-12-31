/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
package com.ychengycheng.channel.handler;

import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.config.ServiceConfig;
import com.ychengycheng.emun.RespCode;
import com.ychengycheng.message.RequestPayload;
import com.ychengycheng.message.YchengRpcRequest;
import com.ychengycheng.message.YchengRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<YchengRpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, YchengRpcRequest msg) throws Exception {
        Object o = null;
        //1.获取负载内容
        RequestPayload requestPayload = msg.getRequestPayload();
        if (requestPayload != null) {
            o = callTargetMethod(requestPayload);

        }
        //2.根据负载内容进行方法调用

        //3.封装响应
        YchengRpcResponse ychengRpcResponse = new YchengRpcResponse();
        ychengRpcResponse.setCode(RespCode.SUCCESS.getCode());
        ychengRpcResponse.setRequestId(msg.getRequestId());
        ychengRpcResponse.setCompressType(msg.getCompressType());
        ychengRpcResponse.setSerializeType(msg.getSerializeType());
        ychengRpcResponse.setBody(o);
        ychengRpcResponse.setTimeStamp(msg.getTimeStamp());
        //4.写出相应
        ctx.channel().writeAndFlush(ychengRpcResponse);

    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parameterValue = requestPayload.getParameterValue();

        ServiceConfig<?> serviceConfig = YchengYchengRPCBootstrap.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        Class<?> aClass = refImpl.getClass();
        Object invoke = null;
        try {
            Method method = aClass.getMethod(methodName, parametersType);
            invoke = method.invoke(refImpl, parameterValue);
            if (invoke != null) {
                log.info("请求完成了方法调用，返回结果->{}", invoke);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常", interfaceName, methodName, e);
            throw new RuntimeException(e);
        }
        return invoke;
    }
}
