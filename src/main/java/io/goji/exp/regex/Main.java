package io.goji.exp.regex;

import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile(".*");
        System.out.println(pattern.matcher("Hello World").matches());
        System.out.println("fda" == "fda");

    }
}
