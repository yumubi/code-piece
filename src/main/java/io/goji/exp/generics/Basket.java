package io.goji.exp.generics;

class Basket<T> {
    T t;

    public void set(T t) {
        this.t = t;
    }

    public T get() {
        return this.t;
    }
}
