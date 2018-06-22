package com.zl.netty.protocol.loginAuth;

import com.zl.netty.protocol.MessageType;
import com.zl.netty.protocol.entity.Header;
import com.zl.netty.protocol.entity.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by hl on 2018/6/1.
 */
public class LoginAuthReqHandler extends ChannelHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        Header header = message.getHeader();
        //如果是握手应答消息，需要判断是否认证成功
        if(header != null && header.getType() == MessageType.LOGIN_RESP.value()) {
            byte loginResult = (byte)message.getBody();
            if(loginResult != (byte)0) {
                //握手失败，关闭连接
                ctx.close();
            } else {
                System.out.println("Lgin is ok : " + message);
                ctx.fireChannelRead(msg);
            }
        }else
            ctx.fireChannelRead(msg);
    }

    private NettyMessage buildLoginReq(){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_REQ.value());
        message.setHeader(header);

        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
