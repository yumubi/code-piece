package io.goji.exp.generics.typeErasure;

import java.io.Serializable;

public interface CrudRepository<E extends Serializable> {
    E find();

    void add(E e);

    boolean contain(E e);
}
