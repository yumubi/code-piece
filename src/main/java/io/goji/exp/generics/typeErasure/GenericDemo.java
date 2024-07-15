package io.goji.exp.generics.typeErasure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public class GenericDemo {



    public static void main(String[] args) {
        Class<?> klass = UserRepository.class;
        Type[] types = klass.getGenericInterfaces();


        printActualArguments(types);
    }

    private static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }

    private static ParameterizedType toParameterizedType(Type type) {
        return (ParameterizedType) type;
    }

    private static void printActualArguments(Type[] types) {
        Stream.of(types)
                .filter(GenericDemo::isParameterizedType)
                .map(GenericDemo::toParameterizedType)
                .forEach(t -> printArguments(t.getActualTypeArguments()));
    }

    private static void printArguments(Type[] types) {
        Stream.of(types).forEach(System.out::println);
    }
}
