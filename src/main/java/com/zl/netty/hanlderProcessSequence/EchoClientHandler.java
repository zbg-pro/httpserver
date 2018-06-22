package com.zl.netty.hanlderProcessSequence;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by hl on 2018/6/5.
 */
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端连接服务器，开始发送数据……");
        byte[] req = "QUERY TIME ORDER".getBytes();//消息
        ByteBuf firstMessage = Unpooled.copiedBuffer(req);

        ctx.writeAndFlush(firstMessage);//flush
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        System.out.println("EchoClientHandler.read");
        super.read(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("EchoClientHandler.channelRead  client 读取server数据..");
        // 服务端返回消息后
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("服务端数据为 :" + body);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client exceptionCaught..");
        // 释放资源
        ctx.close();
    }
}
