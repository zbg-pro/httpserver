package com.zl.netty.protocol;


import com.zl.netty.protocol.loginAuth.LoginAuthReqHandler;
import com.zl.netty.protocol.codeProcessor.NettyMessageDecoder;
import com.zl.netty.protocol.codeProcessor.NettyMessageEncoder;
import com.zl.netty.protocol.heartBeat.HeartBeatReqHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



/**
 * Created by hl on 2018/6/1.
 *
 * 加入readtimeoutHandler，让客户端自动超时，如果在规定时间未读取到内容自动断掉关闭channel，ctx close
 *
 * 加入loginHandler,并且设置channel连接成功后发送一次login消息，让服务端loginhandler也相应接受到进行监听，连接成功后，开始进行心跳定时器运行，建立双方的心跳，客户端主动发起心跳，服务端响应
 */
public class NettyClient {

    private ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(1);
    EventLoopGroup group = new NioEventLoopGroup();

    public void connect(int port, String host) throws Exception {

        // 配置客户端NIO线程组

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new NettyMessageDecoder(1024 * 1024, 4, 4));
                            ch.pipeline().addLast("MessageEncoder",new NettyMessageEncoder());

                            //readTimeoutHandler 如果在指定的时间x秒后没有读取到数据，那么就引发超时，然后关闭当前channel
                            ch.pipeline().addLast("readTimeoutHandler",new ReadTimeoutHandler(10));
                            ch.pipeline().addLast("LoginAuthHandler",new LoginAuthReqHandler());
                            ch.pipeline().addLast("HeartBeatHandler",new HeartBeatReqHandler());
                        }
                    });
            // 发起异步连接操作
            ChannelFuture future = b.connect(
                    new InetSocketAddress(host, port),
                    new InetSocketAddress(NettyConstant.LOCALIP,
                            NettyConstant.LOCAL_PORT)).sync();
            future.channel().closeFuture().sync();
        } finally {
            // 所有资源释放完成之后，清空资源，再次发起重连操作
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        try {
                            connect(NettyConstant.REMOTE_PORT, NettyConstant.REMOTEIP);// 发起重连操作
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new NettyClient().connect(NettyConstant.REMOTE_PORT, NettyConstant.REMOTEIP);
    }
}
