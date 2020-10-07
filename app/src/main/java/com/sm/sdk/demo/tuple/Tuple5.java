package com.sm.sdk.demo.tuple;

/**
 * 五元组
 */
public class Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {

    public final E e;

    public Tuple5(A a, B b, C c, D d, E e) {
        super(a, b, c, d);
        this.e = e;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple5)) {
            return false;
        }
        Tuple5<?, ?, ?, ?, ?> t = (Tuple5<?, ?, ?, ?, ?>) o;
        return equalsEx(t.a, a) && equalsEx(t.b, b) && equalsEx(t.c, c) && equalsEx(t.d, d) && equalsEx(t.e, e);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + hashCodeEx(a);
        result = result * 31 + hashCodeEx(b);
        result = result * 31 + hashCodeEx(c);
        result = result * 31 + hashCodeEx(d);
        result = result * 31 + hashCodeEx(e);
        return result;
    }

}
