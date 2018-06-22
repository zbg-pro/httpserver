package com.zl.netty.hanlderProcessSequence;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by hl on 2018/6/5.
 */
public class EchoInHandler1 extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("EchoInHandler1.channelRead");

        //接受客户端消息
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);

        String body = new String(req, "utf-8");
        System.out.println("接收客户端数据:" + body);

        //向客户端写消息
        String sendStr = System.currentTimeMillis()+": server向client发送数据";
        ByteBuf resp = Unpooled.copiedBuffer(sendStr.getBytes());

        ctx.writeAndFlush(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("EchoInHandler1.channelReadComplete1");
        ctx.flush();//刷新后才将数据发出到SocketChannel
    }

}
