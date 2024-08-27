package io.goji.exp.syncstream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestStreamSyncOrAsync {


    // I want to test the java List Stream() API to see if it is synchronous or asynchronous
    public static void main(String[] args) {

        // Create a list of integers
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        // Create a stream from the list
        Stream<Integer> stream = list.stream();

        // Print the elements of the stream
        stream.forEach(System.out::println);



        // The output will be the integers from 0 to 99 in order

        // This means that the stream is synchronous, as the elements are printed in order

        // Then test the parallel stream to see if it is synchronous or asynchronous
        System.out.println("Parallel Stream");
        Stream<Integer> parallelStream = list.parallelStream();

        // Print the elements of the parallel stream
        parallelStream.forEach(System.out::println);


        // actually, I want to implement a async and non-blocking stream




    }
}
