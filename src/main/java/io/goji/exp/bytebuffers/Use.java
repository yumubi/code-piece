package io.goji.jav.bytebuffers;

import java.nio.ByteBuffer;

public class Use {
    ByteBuffer buffer = ByteBuffer.allocate(1024); //堆内
    ByteBuffer direct = ByteBuffer.allocateDirect(1024); //堆外

    // 注意allocateDirect返回的是DirectByteBuffer实例，为了兼容性请不要做类型转换获取
    //DirectByteBuffer的引用类型
    public static ByteBuffer allocateDirect(int capacity) {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        print(buffer);  // 初始状态：position: 0, limit: 6, capacity: 6

        // 往buffer中写入3个字节的数据
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        buffer.put((byte) 3);
        print(buffer);  // 写入之后的状态：position: 3, limit: 6, capacity: 6

        System.out.println("************** after flip **************");
        buffer.flip();
        print(buffer);  // 切换为读取模式之后的状态：position: 0, limit: 3, capacity: 6

        buffer.get();
        buffer.get();
        print(buffer);
        return buffer;

    }


    public static void print(ByteBuffer buffer) {
        System.out.println("position: " + buffer.position() + ", limit: " + buffer.limit() + ", capacity: " + buffer.capacity());
    }


    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        // position: 0, limit: 6, capacity: 6

        buffer.put((byte) 1);
        buffer.put((byte) 2);
        buffer.put((byte) 3);
        // position: 3, limit: 6, capacity: 6

        buffer.mark();  // 写入三个字节数据后进行标记
        // position: 3, limit: 6, capacity: 6

        buffer.put((byte) 4); // 再次写入一个字节数据
        // position: 4, limit: 6, capacity: 6

        buffer.reset(); // 对buffer进行重置，此时将恢复到Mark时的状态
        // position: 3, limit: 6, capacity: 6

        buffer.flip();  // 切换为读取模式，此时有三个数据可供读取
        // position: 0, limit: 3, capacity: 6

        buffer.get(); // 读取一个字节数据之后进行标记
        buffer.mark();
        // position: 1, limit: 3, capacity: 6

        buffer.get(); // 继续读取一个字节数据
        // position: 2, limit: 3, capacity: 6

        buffer.reset(); // 进行重置之后，将会恢复到mark的状态
        // position: 1, limit: 3, capacity: 6
    }
}
