package com.zl.netty.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by hl on 2018/5/30.
 */
public class ChineseProverbServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final String[] DICTIONARY = {"只要功夫深，铁杵磨成针。", "劳心者治人，劳力者治于人。", "先天下之忧而忧，后天下之乐而乐。"
    , "落霞与孤鹜起飞，秋水共长天一色", "天时地利人和", "得道者多助，失道者寡助"};


    private String nextQuote(){
        int quoteId = ThreadLocalRandom.current().nextInt(DICTIONARY.length);
        return DICTIONARY[quoteId];
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String req = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println(req);

        if("谚语字典查询？".equals(req)) {
            ByteBuf byteBuf = Unpooled.copiedBuffer("谚语查询结果： " + nextQuote(), CharsetUtil.UTF_8);
            ctx.writeAndFlush(new DatagramPacket(byteBuf, packet.sender()));
        } else {
            ByteBuf byteBuf = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);
            ctx.writeAndFlush(new DatagramPacket(byteBuf, packet.sender()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
