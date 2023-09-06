import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;

/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */

public class NettyTest {

    @Test
    public void nettyServer() throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)

                       // 指定Channel
                       .channel(NioServerSocketChannel.class)

                       //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                       .option(ChannelOption.SO_BACKLOG, 1024)

                       //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                       .childOption(ChannelOption.SO_KEEPALIVE, true)

                       //将小的数据包包装成更大的帧进行传送，提高网络的负载
                       .childOption(ChannelOption.TCP_NODELAY, true)

                       .childHandler(new ChannelInitializer<NioSocketChannel>() {
                           @Override
                           protected void initChannel(NioSocketChannel ch) {
                               ch.pipeline().addLast(new SimpleChannelInboundHandler<>() {

                                   @Override
                                   protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                                       ByteBuf byteBuf = (ByteBuf) o;
                                       System.out.println(byteBuf.toString());
                                       //可以不管，也可以写回去
                                       channelHandlerContext.channel()
                                                            .writeAndFlush(Unpooled.copiedBuffer(
                                                                    "ozxjco".getBytes()));
                                   }
                               });
                           }
                       });

        serverBootstrap.bind(8070).sync();
    }

    @Test
    public void nettyClient() {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                // 1.指定线程模型
                .group(workerGroup)
                // 2.指定 IO 类型为 NIO
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                // 3.IO 处理逻辑
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                     .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                         @Override
                                         protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {

                                             channelHandlerContext.channel()
                                                                  .writeAndFlush(Unpooled.copiedBuffer("213".getBytes()));

                                         }
                                     });
                    }
                });
        // 4.建立连接
        bootstrap.connect("127.0.0.1", 8070).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else {
                System.err.println("连接失败!");

            }

        });
    }
}
