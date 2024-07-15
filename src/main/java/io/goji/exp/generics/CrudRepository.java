package io.goji.exp.generics;

public interface CrudRepository<E> {
    E findAll();

    void add(E e);

    boolean contain(E e);
}
