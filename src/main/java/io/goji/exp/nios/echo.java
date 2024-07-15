package io.goji.jav.nios;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class echo {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        //让selector监听它的连接到来的事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(8888));
        while (true){
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isConnectable()){
                    ((SocketChannel) selectionKey.channel()).finishConnect();
                    //留空 这里的意义后面讲
                    continue;
                }
                if (selectionKey.isWritable()) {
                    //留空 这里的意义后面讲
                    continue;
                }
                if (selectionKey.isReadable()) {

                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                    int c = channel.read(buffer);
                    //若读到-1就是连接断开，这里就不做这么仔细了
                    if (c != -1){
                        buffer.flip();
                        channel.write(buffer);

                    }else {
                        //会取消与其关联的全部selector监听事件
                        channel.close();
                    }
                    continue;
                }
                if (selectionKey.isAcceptable()){
                    SocketChannel accept = ((ServerSocketChannel) selectionKey.channel()).accept();
                    //很重要
                    accept.configureBlocking(false);
                    //让selector监听它的数据到来的事件
                    accept.register(selector, SelectionKey.OP_READ);
                }
                iterator.remove();
            }
        }
    }
}
