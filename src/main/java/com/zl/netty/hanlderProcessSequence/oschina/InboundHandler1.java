package com.zl.netty.hanlderProcessSequence.oschina;

/**
 * Created by hl on 2018/6/6.
 */

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;


public class InboundHandler1 extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("InboundHandler1.channelRead: ctx :" + ctx);

        // 通知执行下一个InboundHandler
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("InboundHandler1.channelReadComplete");
        ctx.flush();
    }
}
