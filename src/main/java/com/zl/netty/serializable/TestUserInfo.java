package com.zl.netty.serializable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by hl on 2018/5/8.
 */
public class TestUserInfo {
    public static void main(String[] args) throws IOException {
        test2();

    }

    public static void test1()  throws IOException{
        UserInfo info = new UserInfo();
        info.buildUserID(100).buildUserName("Welcome to Netty");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream os = new ObjectOutputStream(bos);

        os.writeObject(info);

        os.flush();

        os.close();

        byte[] b = bos.toByteArray();

        System.out.println("jdk serializable length is : " + b.length);

        bos.close();

        System.out.println("byte array serialiable length is : " + info.codeC().length);
    }

    public static void test2()  throws IOException{
        UserInfo info = new UserInfo();
        info.buildUserID(100).buildUserName("Welcome to Netty");
        ByteArrayOutputStream bos = null;
        ObjectOutputStream os = null;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            bos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bos);
            os.writeObject(info);
            os.flush();
            os.close();
            byte[] b = bos.toByteArray();
            bos.close();
        }
        long endTime = System.currentTimeMillis();

        System.out.println("jdk serializable cost time is : " + (endTime-startTime));

        System.out.println("===================================");


        ByteBuffer buffer = ByteBuffer.allocate(1024);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            byte[] b = info.codeC(buffer);
        }
        endTime = System.currentTimeMillis();

        System.out.println("byte array serialiable cost time is : " + (endTime-startTime));
    }
}
