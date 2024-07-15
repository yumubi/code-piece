package io.goji.exp.generics;

import java.io.Serializable;

public interface CrudRepositoryPlus<E extends Serializable> {
    E findAll();

    void add(E e);

    boolean contain(E e);
}
