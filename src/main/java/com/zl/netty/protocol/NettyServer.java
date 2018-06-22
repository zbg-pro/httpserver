package com.zl.netty.protocol;


import com.zl.netty.protocol.codeProcessor.NettyMessageDecoder;
import com.zl.netty.protocol.codeProcessor.NettyMessageEncoder;
import com.zl.netty.protocol.heartBeat.HeartBeatRespHandler;
import com.zl.netty.protocol.loginAuth.LoginAuthRespHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * Created by hl on 2018/6/1.
 */
public class NettyServer {

    public void bind() throws Exception {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();


            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyMessageDecoder(1024 * 1024, 4, 4));
                            ch.pipeline().addLast(new NettyMessageEncoder());
                            ch.pipeline().addLast("readTimeoutHandler",new ReadTimeoutHandler(10));
                            ch.pipeline().addLast(new LoginAuthRespHandler());
                            ch.pipeline().addLast("HeartBeatHandler", new HeartBeatRespHandler());
                        }
                    });

            //绑定端口，同步等待成功
        ChannelFuture f =   b.bind(NettyConstant.REMOTEIP, NettyConstant.REMOTE_PORT).sync();//远程ip 端口
            System.out.println("Netty server start ok : " + NettyConstant.REMOTEIP + ":" + NettyConstant.REMOTE_PORT);


    }

    public static void main(String[] args) throws Exception {
        new NettyServer().bind();
    }

}
