package com.zl.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by hl on 2018/5/30.
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());

    private WebSocketServerHandshaker handshaker;

    private static ConcurrentMap<ChannelHandlerContext, String> clientUsers = new ConcurrentHashMap<>();

    private ExecutorService threadPool = Executors.newCachedThreadPool();

    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                montor();
            }
        }).start();
    }

    public static void montor() {
        while(clientUsers.size()>=0){

            Set<ChannelHandlerContext> users = clientUsers.keySet();
            for(ChannelHandlerContext user: users) {
                user.channel().write(new TextWebSocketFrame(clientUsers.get(user).toString() + ", Welcome use Netty WebSocket service, it's time: " + new Date().toString()));
                user.flush();
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        //传统的http接入
        if(msg instanceof FullHttpRequest) {
            handleHttpRequst(ctx, (FullHttpRequest)msg);
        }
        //websocket接入
        else if(msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame)msg);
        }

        clientUsers.put(ctx, msg.toString());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequst(ChannelHandlerContext ctx, FullHttpRequest req) {
        //如果http解码失败，返回http异常
        if(!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpRespone(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }


        //构造握手相应返回，本机测试
        handshaker = new WebSocketServerHandshakerFactory("ws://localhost:1122/websocket", null, false).newHandshaker(req);
        if(handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //判断是否是关闭链路的指令
        if(frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //todo #本历程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        String request = ((TextWebSocketFrame) frame).text();
        logger.info(String.format("%s received %s", ctx.channel(), request));
        ctx.channel().write(new TextWebSocketFrame(request + "，Welcome use Netty WebSocket service, it's time: " + new Date().toString()));


    }

    private void sendHttpRespone(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse resp) {
        //返回应答给客户端
        if(resp.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(resp.getStatus().toString(), CharsetUtil.UTF_8);
            resp.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(resp, resp.content().readableBytes());
        }

        //如果是非 Keep-Alive,关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(resp);
        if(!HttpHeaders.isKeepAlive(req) || resp.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }

    }

    /**
     * http 1.0中默认是关闭的，需要在http头加入”Connection: Keep-Alive”，才能启用Keep-Alive；http 1.1中默认启用Keep-Alive
     * 如果加入”Connection: close “，才关闭。目前大部分浏览器都是用http1.1协议，也就是说默认都会发起Keep-Alive的连接请求了
     *
     * HttpHeaders 类有详细实现方法
     *
     * @param req
     * @return
     */
    private boolean isKeepAlive(FullHttpRequest req) {
        return !"close".equals(req.headers().get("Connection"));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
