package io.goji.exp.poly;

public class Z extends X{
    Y y = new Y();
    public Z() {
        System.out.println("Z");
    }

    public static void main(String[] args) {
        new Z(); //YXYZ


    }
}
