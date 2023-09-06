/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.initializer;

import com.ychengycheng.channel.decoder.YchengDefaultRequestDecoder;
import com.ychengycheng.channel.encoder.YchengDefaultResponseRpcEncoder;
import com.ychengycheng.channel.handler.MethodCallHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SimpleProviderChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //第一个处理器，默认自带的日志处理器
        //第一个处理器，默认自带的日志处理器
        socketChannel.pipeline()
                     .addLast(new LoggingHandler(LogLevel.DEBUG))
                     .addLast(new YchengDefaultRequestDecoder())
                     .addLast(new MethodCallHandler())
                     .addLast(new YchengDefaultResponseRpcEncoder());
        //netty自带的日志处理器

    }
}
