/**
 * @author Valar Morghulis
 * @Date 2023/9/2
 */
package com.ychengycheng.channel.initializer;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyBootstrapInitializer {
    private static final Bootstrap BOOTSTRAP = new Bootstrap();


    /*
    static {
        //准备创建一个新的channel
        NioEventLoopGroup group = new NioEventLoopGroup();
        BOOTSTRAP.group(group)
                 .channel(NioServerSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel socketChannel) throws Exception {
                         socketChannel.pipeline()
                                      .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                          @Override
                                          protected void channelRead0(ChannelHandlerContext
                                          channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                              log.info("msg->{}", byteBuf.toString());
                                              channelHandlerContext.channel()
                                                                   .writeAndFlush(Unpooled
                                                                   .copiedBuffer("213".getBytes()));

                                          }
                                      });
                     }
                 });
    }
    */

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        BOOTSTRAP.group(group).channel(NioSocketChannel.class).handler(new SimpleConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {
    }

    public static Bootstrap getBootstrap() {
        //准备创建一个新的channel

        //启动客户端需要一个辅助类，bootstrap
        return BOOTSTRAP;
    }
}
