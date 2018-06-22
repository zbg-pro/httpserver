package com.zl.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * Created by hl on 2018/5/9.
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String url;

    private static final Pattern INSECURE_URL = Pattern.compile(".*[<>&\"].*");


    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    public HttpFileServerHandler(String url) {
        this.url = url;
    }


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
       if(!request.getDecoderResult().isSuccess()) {
           sendError(ctx, HttpResponseStatus.BAD_REQUEST);
           return;
       }
       if(request.getMethod() != HttpMethod.GET) {
           sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
           return;
       }

       final String uri = request.getUri();
       final String path = sanitizeUri(uri);

       if(path == null) {
           sendError(ctx, HttpResponseStatus.FORBIDDEN);
           return;
       }
       File file = new File(path);
       if(file.isHidden() || !file.exists()) {
           sendError(ctx, HttpResponseStatus.NOT_FOUND);
           return;
       }

       if(file.isDirectory()) {
           if(uri.endsWith("/")) {
               sendLising(ctx, file);
           } else {
               sendRedirect(ctx, uri + "/");
           }
           return;
       }

       if(!file.isFile()) {
           sendError(ctx, HttpResponseStatus.FORBIDDEN);
           return;
       }

        RandomAccessFile randomAccessFile = null;
       try {
           //以只读的方式打开文件
           randomAccessFile = new RandomAccessFile(file, "r");
       }catch (Exception e) {
           sendError(ctx, HttpResponseStatus.NOT_FOUND);
       }

        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        HttpHeaders.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        if(HttpHeaders.isKeepAlive(request)) {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ctx.write(response);
        ChannelFuture sendFileFuture;
        sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                if(total < 0) { //total unknown
                    System.err.println("Transfer progress: " + progress);
                } else {
                    System.out.println("Transfer progress: "+progress+"/"+total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.println("Transfer complete.");
            }
        });

        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!HttpHeaders.isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private String sanitizeUri (String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (Exception e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                throw new Error();
            }
        }
        /*if(!uri.startsWith(url)) {
            return null;
        }*/
        if(!uri.startsWith("/")) {
            return null;
        }

        uri = uri.replace('/', File.separatorChar);
        if(uri.contains(File.separator+'.')
                || uri.contains('.'+File.separator) || uri.startsWith(".")
                ||uri.endsWith(".")||INSECURE_URL.matcher(uri).matches()) {
            return null;
        }
        return  System.getProperty("user.dir") + uri;//File.separator + uri;
    }

    private static void sendLising(ChannelHandlerContext ctx, File dir){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html;charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append("目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append("目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\"></a></li>\r\n");

        for (File f: dir.listFiles()) {
            if(f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            /*if(ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }*/
            buf.append("<li>链接: <a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }

        buf.append("</ul></body></html>\r\n");

        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);

        response.content().writeBytes(buffer);
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaders.Names.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: "
                + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file.getPath()));
    }


}
