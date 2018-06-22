package com.zl.nio.file;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Created by hl on 2018/5/30.
 */
public class FileWriteUtil {

    public static void fileCopyNormal(File fromfile, File toFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
          in = new BufferedInputStream(new FileInputStream(fromfile));
          out = new BufferedOutputStream(new FileOutputStream(toFile));

          byte[] bytes = new byte[1024*1024];
          int i;

          while((i = in.read(bytes))!=-1) {
            out.write(bytes);
          }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }

    }

    public static void fileCopyFileWithChannel(File fromfile, File toFile) throws IOException {
        FileChannel inChannel = new RandomAccessFile(fromfile, "rw").getChannel();
        FileChannel outChannel = new RandomAccessFile(toFile, "rw").getChannel();

//        FileChannel inChannel = new FileInputStream(fromfile).getChannel();
//        FileChannel outChannel = new FileOutputStream(toFile).getChannel();

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            inChannel.close();
            outChannel.close();
        }

    }

    public static void fileCopyFileWithChannelMT(File fromfile, File toFile) throws IOException {
        final FileChannel inChannel = new RandomAccessFile(fromfile, "rw").getChannel();
        final FileChannel outChannel = new RandomAccessFile(toFile, "rw").getChannel();

//        FileChannel inChannel = new FileInputStream(fromfile).getChannel();
//        FileChannel outChannel = new FileOutputStream(toFile).getChannel();



        List<Future> list = new ArrayList();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        long blockCount = 4;
        final long blockSize = inChannel.size()/blockCount;
        try {

            for (int i = 0; i < blockCount-1; i++) {
                Future future = threadPool.submit(new FileCopyThread(inChannel, outChannel, blockSize*i, blockSize*(i+1)));
                list.add(future);
            }

            Future future =  threadPool.submit(new FileCopyThread(inChannel, outChannel, blockSize*(blockCount-1), inChannel.size()));
            list.add(future);

            for (int i = 0; i < list.size(); i++) {
                list.get(i).get();
            }

            threadPool.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            inChannel.close();
            outChannel.close();
        }

    }



    public static class FileCopyThread implements Runnable {
        private FileChannel inChannel = null;
        private FileChannel outChannel = null;

        private long start, end;

        public FileCopyThread(FileChannel inChannel, FileChannel outChannel, long start, long end){
            this.inChannel = inChannel;
            this.outChannel = outChannel;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            FileLock fileLock = null;
            try {
                fileLock = outChannel.lock(start, end-start, false);
                inChannel.transferTo(start, end, outChannel);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fileLock.release();
//                    outChannel.close();
//                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    public static void main(String[] args) throws Exception {
        File fromfile = new File("/Users/hl/2018-05-11 zb性能 /currencyuser.sql");
        File toFile1  = new File("/Users/hl/2018-05-11 zb性能 /currencyuser_cp1.sql");
        File toFile2  = new File("/Users/hl/2018-05-11 zb性能 /currencyuser_cp2.sql");
        File toFile3  = new File("/Users/hl/2018-05-11 zb性能 /currencyuser_cp3.sql");


        long start = System.currentTimeMillis();
        fileCopyNormal(fromfile, toFile1);
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        fileCopyFileWithChannel(fromfile, toFile2);
        System.out.println(System.currentTimeMillis() - start);


        start = System.currentTimeMillis();

        fileCopyFileWithChannelMT(fromfile,toFile3);

        System.out.println(System.currentTimeMillis()-start);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
    }

}
