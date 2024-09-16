package io.goji.exp.classload;

public class myTest {

    public myTest() {
        System.out.println("外部类无参构造方法");
    }
    static {
        System.out.println("外部类静态代码块");
    }


    {
        System.out.println("外部类普通代码块");
    }

    public void outMethod() {
        System.out.println("外部类的普通方法,这时候还没有看到内部类有任何输出");
        InnerClass innerClass = new InnerClass();
        innerClass.innerMethod();
        InnerStaticClass innerStaticClass = new InnerStaticClass();
        innerStaticClass.innerMethod();
    }

    //内部类登场
    public class InnerClass{
        int a = 10;
        {
            System.out.println("成员内部类的普通代码块---a=" + a);
        }

        InnerClass() {
            System.out.println("成员内部类的无参构造方法---a=" + a);
            a = 0;
        }
        public void innerMethod() {
            System.out.println("成员内部类的普通方法---a=" + a);
        }
    }


    //静态内部类

    public static  class InnerStaticClass {
        int a = 10;

        static {
            System.out.println("静态内部类的静态代码块");

        }

        InnerStaticClass() {
            System.out.println("静态内部类的无参构造方法a=" + a);
        }

        public static void innerStaticMethod() {
            System.out.println("静态内部类的静态方法");
        }

        public void innerMethod() {
            System.out.println("静态内部类的普通方法a=" + a);
        }


    }


    public static void main(String[] args) {

        System.out.println("main方法执行");
        myTest myTest = new myTest();
        myTest.outMethod();

    }
}

