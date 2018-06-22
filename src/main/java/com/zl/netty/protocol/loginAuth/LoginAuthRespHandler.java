package com.zl.netty.protocol.loginAuth;

import com.zl.netty.protocol.MessageType;
import com.zl.netty.protocol.entity.Header;
import com.zl.netty.protocol.entity.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hl on 2018/6/1.
 */
public class LoginAuthRespHandler extends ChannelHandlerAdapter {

    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();

    /**
     * 通过白名单验证是否是合法的接入
     */
    private String[] whiteklist = {"127.0.0.1", "192.168.11.222"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        Header header = message.getHeader();
        if(header != null && header.getType() == MessageType.LOGIN_REQ.value()) {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;

            if(nodeCheck.containsKey(nodeIndex)) {
                loginResp = buildResponse((byte) -1 );
            } else {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOK = false;
                for (String WIP: whiteklist) {
                    if(WIP.equals(ip)) {
                        isOK = true;
                        break;
                    }
                }

                loginResp = isOK ? buildResponse((byte)0) : buildResponse((byte) -1);
                if(isOK)
                    nodeCheck.put(nodeIndex, true);
            }
            System.out.println("The login response is : " + loginResp + " body[" + loginResp.getBody() + "]");
            ctx.writeAndFlush(loginResp);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildResponse(byte result){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.value());
        message.setHeader(header);
        message.setBody(result);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        nodeCheck.remove(ctx.channel().remoteAddress().toString());// 删除缓存
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
