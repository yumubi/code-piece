package io.goji.jav.nios.asyncChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FileIO {

    public static void main(String[] args) {
//        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("a.zip"))) {
//            ByteBuffer buffer = ByteBuffer.allocate(21 * 1024);
//            Future<Integer> future = channel.read(buffer, 0);
//            while (!future.isDone()) {
//                System.out.println("正在执行的是:" + Thread.currentThread().getName());
//            }
//            // 阻塞式方法,直到线程执行完毕
//            Integer result = future.get();
//            buffer.flip();
//            System.out.println(result);
//            System.out.println(new String(buffer.array(), 0, buffer.limit()));
//
//        } catch (IOException | InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }


        try(AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("tmp.jpg"))) {
            ByteBuffer buffer = ByteBuffer.allocate(21 * 1024);
            channel.read(buffer, 0, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    buffer.flip();
                    System.out.println(new String(buffer.array(), 0, buffer.limit()));
                    System.out.println("读取完毕");
                }

                @Override
                public void failed(Throwable exc, Object attachment) {

                }
            });
            while (true) {
                // 这里是为了验证系统开辟了一个线程,所做的打印。
                System.out.println("正在执行的是:" + Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(5);
            }

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
