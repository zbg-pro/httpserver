package com.zl.netty.hanlderProcessSequence;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by hl on 2018/6/5.
 */
public class EchoServer {

    private final int port;

    public EchoServer(int port){
        this.port = port;
    }

    public void start() throws InterruptedException {

        EventLoopGroup group = null;

        try {
            //server 引导类
            ServerBootstrap b = new ServerBootstrap();

            //连接池处理数据
            group = new NioEventLoopGroup();
            b.group(group)
                    .channel(NioServerSocketChannel.class) //指定通道类型为NioServerSocketChannel，一种异步模式，OIO阻塞模式为OioServerSocketChannel
                    .localAddress("localhost", port) //设置InetSocketAddress让服务器监听某个端口已等待客户端连接。
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {

                            //类似servlet的过滤器filter
                            //in的执行顺序2，1
                            //out的执行顺序2，1
                            ch.pipeline().addLast(new EchoOutHandler1());
                            ch.pipeline().addLast(new EchoOutHandler2());
                            ch.pipeline().addLast(new EchoInHandler2());
                            ch.pipeline().addLast(new EchoInHandler1());
                        }
                    });

            //绑定服务器等待直到绑定完成，syn方法作用是阻塞直到绑定完成，然后服务器等待通道关闭，因为使用sync()，所以关闭操作也会被阻塞。
            //ChannelFuture channelFuture = b.bind().sync();
            //System.out.println("开始监听，端口为：" + channelFuture.channel().localAddress());

            //channelFuture.channel().close().sync();

            //绑定端口，同步等待成功
            ChannelFuture future = b.bind(port).sync();

            //等待服务监听端口关闭
            future.channel().closeFuture().sync();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }


    }

    public static void main(String[] args) throws Exception {
        new EchoServer(1122).start();
    }

}
