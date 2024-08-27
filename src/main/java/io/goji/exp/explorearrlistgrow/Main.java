package io.goji.exp.explorearrlistgrow;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {


        // 主要关注private Object[] grow(int minCapacity) ->

        // 大多数情况下 minimum growth = 1, preferred growth = oldCapacity >> 1?
        //   int newCapacity = ArraysSupport.newLength(oldCapacity,
        //                    minCapacity - oldCapacity, /* minimum growth */
        //                    oldCapacity >> 1           /* preferred growth */);


        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            list.add(i);
        }

    }
}
