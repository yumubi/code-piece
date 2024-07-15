package io.goji.exp.generics;

import java.util.ArrayList;
import java.util.List;

public class Main{

    public static void main(String[] args) {

//        Fruit apple = new Apple();
//
//        Fruit orange = new Orange();


//        Basket<Fruit> basket = new Basket<Apple>(); //编译报错

//        Fruit[] fruits = new Apple[2]; //合法
//        fruits[0] = new Apple(); //合法
//        fruits[1] = new Orange(); //编译时合法,运行时 throw ArrayStoreException


//        //协变 S<:T
//        Basket<? extends Fruit> basket1 = new Basket<>();
//        //逆变 T:>S
//        Basket<? super Apple> basket2 = new Basket<Fruit>();





    }



    public static void covariance() {
        //装苹果的篮子可以转换为装水果的篮子，前提条件是装苹果的篮子只能装苹果，不能装橘子,所以为了类型安全，编译器不允许我们往里面放任何东西
        Basket<? extends Fruit> basket = new Basket<Apple>(); //合法
        List<? extends Fruit> list = new ArrayList<Orange>(); //合法

        //basket.set(new Orange()); //报错
        //basket.set(new Apple()); //报错
        //basket.set(new Fruit()); //报错

        Basket<Apple> applesOfBasket = new Basket<>();
        applesOfBasket.set(new Apple());
        basket = applesOfBasket; // 正确




        // list同理
    }



    public static void contravariance() {

        //装水果的篮子可以转换为装苹果的篮子，前提条件是装水果的篮子可以装苹果，也可以装橘子..，所以为了类型安全，编译器不允许我们从里面取任何东西
        Basket<? super Apple> basket = new Basket<Fruit>();
     //   basket.setApple(new Apple()); //合法
        basket.set(new Apple()); //合法
//        basket.set(new Fruit()); //不合法

     //   Apple apple = basket.get(); //合法?


        Basket<Fruit> tmp = new Basket<Fruit>();
        tmp.set(new Orange());


        Basket<? super Apple> basket2 = tmp;
// Apple apple2 = basket2.get(); //无法通过编译
        Apple apple2 = (Apple)basket2.get(); //ClassCastException
    }


}
