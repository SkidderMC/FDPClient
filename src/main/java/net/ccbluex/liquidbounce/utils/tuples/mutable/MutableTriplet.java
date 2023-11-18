package net.ccbluex.liquidbounce.utils.tuples.mutable;


import net.ccbluex.liquidbounce.utils.tuples.Triplet;

import java.util.function.UnaryOperator;

public class MutableTriplet<A, B, C> extends Triplet<A, B, C> {
    private A a;
    private B b;
    private C c;

    MutableTriplet(A a, B b, C c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> MutableTriplet<A, B, C> of(A a, B b, C c) {
        return new MutableTriplet<>(a, b, c);
    }

    public static <A> MutableTriplet<A, A, A> of(A a) {
        return new MutableTriplet<>(a, a, a);
    }

    public MutableTriplet<A, A, A> pairOfFirst() { return of(a); }

    public MutableTriplet<B, B, B> pairOfSecond() { return of(b); }

    public MutableTriplet<C, C, C> pairOfThird() { return of(c); }

    @Override
    public A getFirst() { return a; }

    @Override
    public B getSecond() { return b; }

    @Override
    public C getThird() { return c; }

    public void setFirst(A a) { this.a = a; }

    public void setSecond(B b) { this.b = b; }

    public void setThird(C c) { this.c = c; }

    @Override
    public <R> R apply(TriFunction<? super A, ? super B, ? super C, ? extends R> func) { return func.apply(a, b, c); }

    @Override
    public void use(TriConsumer<? super A, ? super B, ? super C> func) { func.accept(a, b, c); }

    public void computeFirst(UnaryOperator<A> operator) {
        this.a = operator.apply(a);
    }

    public void computeSecond(UnaryOperator<B> operator) {
        this.b = operator.apply(b);
    }

    public void computeThird(UnaryOperator<C> operator) {
        this.c = operator.apply(c);
    }
}
