package com.zl.nio.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mycat
 *
 */
public class FileReadUtil {

    public static void main( String args[] ) throws Exception {
        /*String jdbcdriver="com.mysql.jdbc.Driver";
        String jdbcurl="jdbc:mysql://127.0.0.1:8066/TESTDB?useUnicode=true&characterEncoding=utf-8";
        String username="root";
        String password="123456";
        System.out.println("开始连接mysql:"+jdbcurl);
        Class.forName(jdbcdriver);
        Connection c = DriverManager.getConnection(jdbcurl,username,password); 
        Statement st = c.createStatement();
        print( "test jdbc " , st.executeQuery("select count(*) from currencyuser limit 10 "));
        System.out.println("OK......");*/


        //testFile2();

        readFile1();

        readFile2();

        readFile3();
    }



    //永远读取最后100行数数据
    public static void  testFile() throws Exception{
        RandomAccessFile fileRead = new RandomAccessFile("/usr/local/Cellar/mycat/logs/a.log", "r");

        List<String> list = new ArrayList<>();
        long count = 0;
        long length = fileRead.length();

        if (length == 0L) {//文件内容为空
            System.out.println("文件内容为空");
        } else {
            long pos = length - 1;

            while(pos > 0) {
                pos --;
                fileRead.seek(pos);

                if(fileRead.readByte() == '\n'){
                    String line = new String(fileRead.readLine().getBytes("ISO-8859-1"), "UTF-8");
                    list.add(line);
                    count ++;
                    System.out.println(line);
                }

                if(count == 100){
                    break;
                }

                if (pos == 0) {
                    fileRead.seek(0);
                    String line = new String(fileRead.readLine().getBytes("ISO-8859-1"),"UTF-8");
                    list.add(line);
                    System.out.println(line);
                }
            }

        }

    }


    public static void testFile2() throws IOException {
        RandomAccessFile aFile = new RandomAccessFile("/usr/local/Cellar/mycat/logs/a.log", "rw");
        FileChannel inChannel = aFile.getChannel();

        //create buffer with capacity of 48 bytes
        ByteBuffer buf = ByteBuffer.allocateDirect(48);

        int bytesRead = inChannel.read(buf); //read into buffer.
        while (bytesRead != -1) {

            buf.flip();  //make buffer ready for read

            /*byte[] arr = new byte[1];
            while(buf.hasRemaining()){
                System.out.print(new String(new byte[]{buf.get()})); // read 1 byte at a time
            }*/

            //转换为数组
            byte[] bytes=new byte[buf.remaining()];
            buf.get(bytes); //一次性读取到48个
            //转换为String变量
            String msg=new String(bytes);
            System.out.print(msg);

            buf.clear(); //make buffer ready for writing

            //((DirectBuffer)buf).cleaner().clean();

            bytesRead = inChannel.read(buf);//指针读取到buf的末尾
        }
        aFile.close();

    }

    public static void  testFile1() throws Exception{
        RandomAccessFile rf = new RandomAccessFile("/usr/local/Cellar/mycat/logs/a.log", "r");

        long len = rf.length();   //文件长度

        System.out.println("文件开始指针为"+0);
        long nextend =  len ;  //指针是从0到length-1
        String line = "";
        rf.seek(nextend); //seek到最后一个字符
        int c=-1;

        while(nextend>=0) {
            c = rf.read();

            if(c == '\n')
                line = new String(rf.readLine());
            System.out.println(line);

            rf.seek(nextend);

            if (nextend == 0) {// 当文件指针退至文件开始处，输出第一行
                System.out.println(new String(rf.readLine()));
            }
            nextend--;

        }


    }


    public static void readFile1() throws Exception {

        String path = "/Users/hl/2018-05-11 zb性能 /currencyuser.sql";

        long start = System.currentTimeMillis();//开始时间
        int bufferSize = 1024*1024*20;//1K buff
        File file = new File(path);

        FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        String enterStr = "\n";

        try {
            byte[] bytes = new byte[bufferSize];
            String tempString = null;

            while(fileChannel.read(byteBuffer) != -1){//每次读取1k的数据
                int bufferPosition = byteBuffer.position(); //记录当前缓存区域的位置, 供1k数据输出
                byteBuffer.rewind();//位置归零，清除标记，方便下次循环重新读入字符到缓存区
                byteBuffer.get(bytes);//将buf的数据读取到字节数组中
                byteBuffer.clear();//清理缓存

                /*
                 * 用默认编码将指定字节数组的数据构造成一个字符串
                 * bytes:指定的字节数组，0：数组起始位置；bufferPosition：数组结束位置
                 * */

                tempString = new String(bytes, 0, bufferPosition);

                //System.out.println(tempString);


                /*int fromIndex = 0;
                int endIndex = 0;


                //检出每一行
                while (endIndex != -1) {
                    endIndex = tempString.indexOf(enterStr, fromIndex);
                    String line = "";
                    if(endIndex == -1) {
                        line = tempString.substring(fromIndex);
                    }else
                        line = tempString.substring(fromIndex, endIndex);
                    System.out.print(line);
                    fromIndex = endIndex + 1;
                }*/


            }

            long end = System.currentTimeMillis();//结束时间
            System.out.println("\n传统IO读取数据,指定缓冲区大小，总共耗时："+(end - start)+"ms");

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            fileChannel.close();
        }
    }

    public static void readFile2() throws IOException {
        long start = System.currentTimeMillis();//开始时间
        String path = "/Users/hl/2018-05-11 zb性能 /currencyuser.sql";
        File file = new File(path);

        if(file.isFile()) {
            BufferedReader bufferedReader = null;
            FileReader fileReader = null;

            try {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);

                String line = bufferedReader.readLine();

                while(line != null) {
                    //System.out.println(line);
                    line = bufferedReader.readLine();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                fileReader.close();
                bufferedReader.close();
            }

            long end = System.currentTimeMillis();//结束时间
            System.out.println("传统IO读取数据，不指定缓冲区大小，总共耗时："+(end - start)+"ms");

        }



    }


    public static void readFile3(){
        long start = System.currentTimeMillis();//开始时间
        String path = "/Users/hl/2018-05-11 zb性能 /currencyuser.sql";

        //path = "/usr/local/Cellar/mycat/logs/a.log";

        File file = new File(path);

        long fileLength = file.length();

        final int BUFFER_SIZE = 1024*1024*10;

        try {
            /*使用FileChannel.map方法直接把整个fileLength大小的文件映射到内存中**/
            MappedByteBuffer mappedByteBuffer = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileLength);
            byte[] bytes = new byte[BUFFER_SIZE];

            for (int offset = 0; offset < fileLength; offset += BUFFER_SIZE) {
                if(fileLength - offset > BUFFER_SIZE) {

                    mappedByteBuffer.get(bytes);

                    String blockStr = new String(bytes);
                    //System.out.print(new String(bytes));

                } else {
                    //mappedByteBuffer.get(bytes);
                    bytes=new byte[mappedByteBuffer.remaining()];
                    mappedByteBuffer.get(bytes); //一次性读取到48个
                    //转换为String变量
                    String msg=new String(bytes);
                    //System.out.print(msg);

                    mappedByteBuffer.clear(); //make buffer ready for writing


                }
            }

            long end = System.currentTimeMillis();//结束时间
            System.out.println("\nNIO 内存映射读大文件，总共耗时："+(end - start)+"ms");
        } catch (Exception e) {
            e.printStackTrace();

        } finally {

        }


    }

         static void print( String name , ResultSet res )
                    throws SQLException {
                    System.out.println( name);
                    ResultSetMetaData meta=res.getMetaData();                       
                    //System.out.println( "\t"+res.getRow()+"条记录");
                    String  str="";
                    for(int i=1;i<=meta.getColumnCount();i++){
                        str+=meta.getColumnName(i)+"   ";
                        //System.out.println( meta.getColumnName(i)+"   ");
                    }
                    System.out.println("\t"+str);
                    str="";
                    while ( res.next() ){
                        for(int i=1;i<=meta.getColumnCount();i++){  
                            str+= res.getString(i)+"   ";       
                            } 
                        System.out.println("\t"+str);
                        str="";
                    }
                }
}
