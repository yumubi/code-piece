package io.goji.exp.classload;

public class myTest1 {
    static {
        System.out.println("父类静态代码块");
    }
    public myTest1() {
        System.out.println("父类无参构造方法");
    }

    {
        System.out.println("父类普通代码块");
    }

    public void sayHello() {
        {
            System.out.println("父类方法中的代码块");
        }
    }

    public static void main(String[] args) {
//        System.out.println("执行了main方法");
//        myTest myTest = new myTest();
//        myTest.sayHello();
//        System.out.println("--------------");
        System.out.println("再次执行了main方法");
        SubClass subClass = new SubClass();

    }
}

class SubClass extends myTest1 {
    static {
        System.out.println("子类静态代码块");
    }

    {
        System.out.println("子类普通代码块");
    }

    public SubClass() {
        System.out.println("子类无参构造方法");
    }
}

