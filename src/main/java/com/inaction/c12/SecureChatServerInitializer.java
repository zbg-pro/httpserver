package com.inaction.c12;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import sun.security.x509.X509CertImpl;

import javax.net.ssl.SSLContext;
import java.security.cert.Certificate;

/**
 * Created by hl on 2018/6/6.
 */
public class SecureChatServerInitializer extends ChatServer {
    private final SSLContext context;


    public SecureChatServerInitializer(SSLContext context) {
        this.context = context;
    }

    @Override
    protected ChannelInitializer<Channel> createInitializer(ChannelGroup group) {
        return super.createInitializer(group);
    }

    //4.33支持  5.0未研究
    public static void main(String[] args) {
        int port = 1102;

        Certificate cert = new X509CertImpl();
        /*SSLContext context = SSLContext.getInstance(
                cert.certificate(), cert.privateKey());
        final SecureChatServer endpoint = new SecureChatServer(context);
        ChannelFuture future = endpoint.start(new InetSocketAddress(port));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                endpoint.destroy();
            }
        });*/
        //future.channel().closeFuture().syncUninterruptibly();
    }
}
