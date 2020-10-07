package com.sm.sdk.demo.tuple;

public final class TupleUtil {

    private TupleUtil() {
        throw new AssertionError();
    }

    /**
     * 创建二元组
     */
    public static <A, B> Tuple<A, B> tuple(A a, B b) {
        return new Tuple<>(a, b);
    }

    /**
     * 创建三元组
     */
    public static <A, B, C> Tuple3<A, B, C> tuple(A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }

    /**
     * 创建四元组
     */
    public static <A, B, C, D> Tuple4<A, B, C, D> tuple(A a, B b, C c, D d) {
        return new Tuple4<>(a, b, c, d);
    }

    /**
     * 创建五元组
     */
    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> tuple(A a, B b, C c, D d, E e) {
        return new Tuple5<>(a, b, c, d, e);
    }

}
