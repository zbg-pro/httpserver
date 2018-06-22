package com.zl.netty.serializable.marshing;

import com.zl.netty.serializable.netty.SubscribeReq;
import com.zl.netty.serializable.netty.SubscribeResp;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by hl on 2018/5/8.
 */
public class SubReqServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SubscribeReq req = (SubscribeReq) msg;

        if("zl239".equalsIgnoreCase(req.getUserName())) {
            System.out.println("Service accept client subscribe req : " + req.toString());
        }

        ctx.writeAndFlush(resp(req.getSubReqID()));
    }

    private SubscribeResp resp(int subReqID) {
        SubscribeResp resp = new SubscribeResp();

        resp.setSubReqID(subReqID);
        resp.setRespCode(0);
        resp.setDesc("Netty book order succeed, 3 days later,sent to the designated address");

        return resp;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
