package com.zl.netty.hanlderProcessSequence.oschina;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.TimeUnit;

public class HelloClientIntHandler extends ChannelHandlerAdapter {
    @Override
    // 读取服务端的信息
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("HelloClientIntHandler.channelRead");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        result.release();
        //ctx.close();
        System.out.println("Server said:" + new String(result1));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(2);
                    String msg = "Are you ok?2";
                    ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
                    encoded.writeBytes(msg.getBytes());
                    ctx.writeAndFlush(encoded);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    @Override
    public void read(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("HelloClientIntHandler.read");


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        String msg = "Are you ok?2";
                        ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
                        encoded.writeBytes(msg.getBytes());

                        boolean a = ctx.channel().isActive();
                        boolean b = ctx.isRemoved();
                        boolean c = ctx.executor().isShutdown();
                        boolean d = ctx.executor().isTerminated();
                        System.out.println(a+""+b+c+d);
                        if(b)
                            ctx.writeAndFlush(encoded);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }).start();



        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("HelloClientIntHandler.write");
        super.write(ctx,msg,promise);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("HelloClientIntHandler.channelWritabilityChanged");
        super.channelWritabilityChanged(ctx);
    }

    @Override
    // 当连接建立的时候向服务端发送消息 ，channelActive 事件当连接建立的时候会触发
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("HelloClientIntHandler.channelActive");
        String msg = "Are you ok?";
        ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
        encoded.writeBytes(msg.getBytes());
        ctx.write(encoded);
        ctx.flush();
    }
}