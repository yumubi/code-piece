package io.goji.jav.nios;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class client {

    final private String host = "localhost";
    final private int port = 8888;

    public static void main(String[] args) {
        new client().echo("localhost", 8888);
    }

    public void echo(String host, int port) {
        //读取数据 和 写入数据
        try {
            //创建一个Socket对象
            Socket socket = new Socket(host, port);
            //创建一个输入流
            InputStream inputStream = socket.getInputStream();
            //创建一个输出流
            OutputStream outputStream = socket.getOutputStream();
            //创建一个缓冲区
            byte[] bytes = new byte[1024];
//            byte[] strBytes = "Hello World! ".getBytes();
//            System.arraycopy(strBytes,0,bytes,0,strBytes.length);
            //读取数据
            int len = inputStream.read(bytes);
            //写入数据
            outputStream.write(bytes, 0, len);
            //关闭资源
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
