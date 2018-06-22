package com.zl.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

/**
 * Created by hl on 2018/5/7.
 */
public class TimeClientHandler extends ChannelHandlerAdapter {

    private static final Logger loggger = Logger.getLogger(TimeClientHandler.class.getName());

    private final ByteBuf firstMessage;


    public TimeClientHandler() {
        byte[] req = "QUERY TIME ORDER".getBytes();
        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        byte[] req = new byte[buf.readableBytes()];

        buf.readBytes(req);

        String body = new String(req, "UTF-8");

        System.out.println("NOW is : "+body);


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        loggger.warning("unexcept exception from downStream: " + cause.getMessage());
        ctx.close();
    }
}
