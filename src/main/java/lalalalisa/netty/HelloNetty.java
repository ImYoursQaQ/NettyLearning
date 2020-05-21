package lalalalisa.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class HelloNetty {
    public static void main(String[] args) {
        new NettyServer(8888).serverStart();
    }
}

class NettyServer {


    int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void serverStart() {
        //配置netty服务
//        EventLoopGroup bossGroup = new DefaultEventLoop();//可以是单线程的bossGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new Handler());
                    }
                });

        try {
            //启动服务
            ChannelFuture f = b.bind(port).sync();
            System.out.println("netty started");
            //阻塞方法：等待服务端关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭事件轮训以终止所有线程
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
}

class Handler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        System.out.println("server: channel read");
        ByteBuf buf = (ByteBuf) msg;
        System.out.println(buf.toString(CharsetUtil.UTF_8));
//        ctx.writeAndFlush("i am netty server.".getBytes(Charset.defaultCharset()));
        msg = "i am netty server.";
        ctx.writeAndFlush(Unpooled.copiedBuffer("HelloClient ,i am server".getBytes()));
        ctx.close();
        //buf.release();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
