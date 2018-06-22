package com.inaction.c12;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;

/**
 * Created by hl on 2018/6/6.
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String wsUri;

    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String path2 = URLDecoder.decode(location.getPath(), "utf-8");
            String path = location.toURI() + "index.html";
            path = !path.contains("file:")? path : path.substring(5);
            INDEX = new File(path);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to locate index.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }


    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest httpRequest = (FullHttpRequest)msg;
        if(wsUri.equalsIgnoreCase(httpRequest.getUri())) {
            ctx.fireChannelRead(httpRequest.retain());
        } else {
            if(HttpHeaders.is100ContinueExpected(httpRequest)) {
                send100Continue(ctx);
            }

            RandomAccessFile file = new RandomAccessFile(INDEX, "r");
            HttpResponse response = new DefaultHttpResponse(httpRequest.getProtocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

            //如果请求了 keep-alive， 则添加所需要的 HTTP 头信息
            if(isKeepAlive(httpRequest)) {
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }

            ctx.write(response);//将httpresponse写入到客户端

            //如果不需要加密和压缩，那么可以通过将 index.html 的内容存储到 DefaultFile- Region
            // 中来达到最佳效率。这将会利用零拷贝特性来进行内容的传输。为此，你可以检查一下，
            // 是否有 SslHandler 存在于在 ChannelPipeline 中。否则，你可以使用 ChunkedNioFile
            if(ctx.pipeline().get(SslHandler.class) == null) {
                ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            } else {
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }

            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!isKeepAlive(httpRequest)) {
                future.addListener(ChannelFutureListener.CLOSE);// 如果没有请求 keep-alive， 则在写操作完成后关闭 Channel
            }
        }

    }

    private void send100Continue(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
