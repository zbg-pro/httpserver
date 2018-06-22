package com.zl.netty.hanlderProcessSequence;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by hl on 2018/6/5.
 */
public class EchoInHandler2 extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("EchoInHandler2.channelRead");
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("EchoInHandler2.channelReadComplete");
        ctx.flush();//刷新后才将数据发出到SocketChannel
    }

}
