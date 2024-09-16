package io.goji.exp.classload;

public class StatictTest {
    public static void main(String[] args) {
        BBB bbb = new BBB();
    }
}

class AAA {

    private String name;
    private String nameWithDefault;{

        System.out.println("AAA的普通代码块");
    }
    public AAA() {
        System.out.println("AAA()构造器被调用");
    }
}
class BBB extends AAA{
    {
        System.out.println("BBB的普通代码块被调用");
    }
    public BBB() {
        System.out.println("BBB()构造器被调用");
    }
}
