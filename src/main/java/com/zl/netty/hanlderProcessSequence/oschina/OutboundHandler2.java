package com.zl.netty.hanlderProcessSequence.oschina;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;


public class OutboundHandler2 extends ChannelHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutboundHandler2.write");
        // 执行下一个OutboundHandler
        super.write(ctx, msg, promise);

    }
}