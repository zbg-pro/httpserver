package com.zl.netty.protocol.entity;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteInput;

import java.io.IOException;

/**
 * Created by hl on 2018/6/4.
 */
public class ChannelBufferByteInput implements ByteInput {

    private final ByteBuf buffer;

    public ChannelBufferByteInput(ByteBuf buffer) {
        this.buffer = buffer;
    }

    /**
     * & 0xff的作用是：从byte扩展到大的类型 int时，对于无符号正整数都一样，因为符号位为0，所以无论如何都是补充0扩展，
     * 但负数补零扩展和按符号位扩展结果是完全不同的：补充符号数：原数值不变，补零相当于把有符号数看作是无符号数，如
     * -127=0x81,前面其实有个表示符号位的1，但是如果作为无符号数时就成了129
     *
     *  对于有符号数，从小扩展大时，需要用&0xff这样方式来确保是按补零扩展。
        而从大向小处理，符号位自动无效，所以不用处理。
     http://blog.163.com/asm_c/blog/static/248203113201011310200949/
     進行了& 0xFF後,就把符號問題忽略掉了,將byte以純0/1地引用其內容,所以要0xFF,不是多餘的,
     * @return
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if(buffer.isReadable()) {
            return buffer.readByte() & 0xff;
        }
        return -1;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public int read(byte[] bytes, int bytesIndex, int length) throws IOException {
        int available = available();
        if(available == 0)
            return -1;
        length = Math.min(available, length);
        buffer.readBytes(bytes, 0, length);
        return length;
    }

    @Override
    public int available() throws IOException {
        return buffer.readableBytes();
    }

    @Override
    public long skip(long skipIndex) throws IOException {
        int readable = buffer.readableBytes();
        if(readable < skipIndex) {
            skipIndex = readable;
        }
        buffer.readerIndex((int)(buffer.readerIndex()+skipIndex));
        return skipIndex;
    }

    @Override
    public void close() throws IOException {

    }

    public static void main(String[] args) {
        byte[] a = new byte[10];
        a[0]= 127;
        System.out.println(a[0]);
        int c = a[0]&0xff;
        System.out.println(c);

        System.out.println( (byte)(a[0]&0xff));

    }
}
