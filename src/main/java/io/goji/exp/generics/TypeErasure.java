package io.goji.exp.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeErasure {
    public static void main(String[] args) {
        givenContainerClassWithGenericType_whenTypeParameterUsed_thenReturnsClassType();
        givenContainerClassWithGenericType_whenReflectionUsed_thenReturnsClassType();
        giveContainerClassWithGenericType_whenTypeTokenUsed_thenReturnsClassType();
    }


    public static void givenContainerClassWithGenericType_whenTypeParameterUsed_thenReturnsClassType(){
        var stringContainer = new ContainerTypeFromTypeParameter<>(String.class);
        Class<String> containerClass = stringContainer.getClazz();

        assertEquals(String.class, containerClass);
    }

    public static void givenContainerClassWithGenericType_whenReflectionUsed_thenReturnsClassType() {
        var stringContainer = new ContainerTypeFromReflection<>("Hello Java");
        Class<?> stringClazz = stringContainer.getClazz();
        assertEquals(String.class, stringClazz);

        var integerContainer = new ContainerTypeFromReflection<>(1);
        Class<?> integerClazz = integerContainer.getClazz();
        assertEquals(Integer.class, integerClazz);
    }



    public static void giveContainerClassWithGenericType_whenTypeTokenUsed_thenReturnsClassType(){
        class ContainerTypeFromTypeToken extends TypeToken<List<String>> {}

        var container = new ContainerTypeFromTypeToken();
        ParameterizedType type = (ParameterizedType) container.getType();
        Type actualTypeArgument = type.getActualTypeArguments()[0];

        assertEquals(String.class, actualTypeArgument);
    }

}
