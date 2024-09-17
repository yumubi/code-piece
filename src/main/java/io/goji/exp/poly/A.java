package io.goji.exp.poly;

public class A {
    public void print() {
        System.out.println("A");
    }
    public A() {
        System.out.println("A");
    }

    public static void main(String[] args) {
        A a = new A();
        System.out.println("----");
        A b = new B();
        System.out.println("----");
        A c = new C();
        System.out.println("----");
        System.out.println("----");
        a.print();
        System.out.println("----");
        b.print();
        System.out.println("----");
        c.print();

    }
}
