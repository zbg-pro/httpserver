package com.zl.netty;

import java.nio.ByteBuffer;

/**
 * Created by hl on 2018/5/8.
 */
public class ByteBufferTest {

    public static void main(String[] args) {

        //10个字节大小
        ByteBuffer buffer = ByteBuffer.allocate(10);

        //容量是10，EOF位置是10，初始位置也是0
        v(buffer.capacity());
        v(buffer.limit());

        v("*********1*********");

        //输出看看，输出是10个0
        printBuffer(buffer);
        v("*********2*********");
        //此时，指针指向位置10，已经是最大容量了。
        //把指针挪回位置1
        v(buffer.position());
        buffer.rewind();
        v(buffer.position());
        //写操作，指针会自动移动
        buffer.putChar('a');
        v(buffer.position()); //指针指向2
        buffer.putChar('啊');
        v(buffer.position()); //指针指向4
        v("*********3*********");
        //当前位置设置为EOF，指针挪回位置1
        //相当于下面两句：
        //buffer.limit(4);
        //buffer.position(0);
        buffer.flip();

        //输出前4个字节看看，输出是0 61 55 4a
        printBuffer(buffer);
        v("******************");
        //指针挪到位置1，压缩一下
        //输出是61 55 4a 4a 0 0 0 0 0 0
        //compact方法会把EOF位置重置为最大容量，这里就是10
        buffer.position(1);
        buffer.compact();
        printBuffer(buffer);
        v("******************");
        //注意当前指针指向3，继续写入数据的话，就会覆盖后面的数据了。
        v(buffer.position());
        v("******************");
    }

    /**
     * 输出buffer内容.
     */
    public static void printBuffer(ByteBuffer buffer) {

        //记住当前位置
        int p = buffer.position();

        //指针挪到0
        buffer.position(0);

        //循环输出每个字节内容
        for (int i = 0; i < buffer.limit(); i++) {
            byte b = buffer.get(); //读操作，指针会自动移动
            v(Integer.toHexString(b));
        }

        //指针再挪回去
        buffer.position(p);

        //本想用mark()和reset()来实现。
        //但是，它们貌似只能正向使用。
        //如，位置6的时候，做一下Mark，
        //然后在位置10（位置要大于6）的时候，用reset就会跳回位置6.

        //而position(n)这个方法，如果之前做了Mark，但是Mark位置大于新位置，Mark会被清除。
        //也就是说，做了Mark后，只能向前跳，不能往回跳，否则Mark就丢失。
        //rewind()方法，更干脆，直接清除mark。
        //flip()方法，也清除mark
        //clear()方法，也清除mark
        //compact方法，也清除mark

        //所以，mark方法干脆不要用了，自己拿变量记一下就完了。
    }

    public static void v(Object o) {
        System.out.println(o);
    }

}
