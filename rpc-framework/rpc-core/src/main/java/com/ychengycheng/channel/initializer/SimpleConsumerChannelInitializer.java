/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.initializer;

import com.ychengycheng.channel.decoder.YchengDefaultResponseDecoder;
import com.ychengycheng.channel.encoder.YchengDefaultRequestRpcEncoder;
import com.ychengycheng.channel.handler.ConsumerSimpleInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SimpleConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //第一个处理器，默认自带的日志处理器
        socketChannel.pipeline()
                     //netty自带的日志处理器
                     .addLast(new LoggingHandler(LogLevel.DEBUG))
                     //自己定制的编码器
                     .addLast(new YchengDefaultRequestRpcEncoder())
                     .addLast(new YchengDefaultResponseDecoder())
                     .addLast(new ConsumerSimpleInboundHandler());
    }
}
