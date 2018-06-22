package com.zl.netty.protocol.entity;


/**
 * Created by hl on 2018/6/4.
 */
public class NettyMessage {

    private Header header;//消息头
    private Object body;//消息体

    public final Header getHeader() {
        return header;
    }

    public final void setHeader(Header header) {
        this.header = header;
    }

    public final Object getBody() {
        return body;
    }

    public final void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NettyMessage{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }
}