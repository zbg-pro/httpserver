package com.zl.netty.hanlderProcessSequence;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by hl on 2018/6/5.
 */
public class EchoOutHandler2 extends ChannelInboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("EchoOutHandler2.write");
        // 执行下一个OutboundHandler
            /*System.out.println("at first..msg = "+msg);
            msg = "hi newed in out2";*/
        super.write(ctx, msg, promise);
    }
}
