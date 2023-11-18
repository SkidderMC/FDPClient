package net.ccbluex.liquidbounce.utils.tuples.mutable;

import net.ccbluex.liquidbounce.utils.tuples.Pair;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * @author Zywl
 * @since 15/11/2023
 */
public class MutablePair<A, B> extends Pair<A, B> {
    private A a;
    private B b;

    MutablePair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> MutablePair<A, B> of(A a, B b) {
        return new MutablePair<>(a, b);
    }

    public static <A> MutablePair<A, A> of(A a) {
        return new MutablePair<>(a, a);
    }

    public MutablePair<A, A> pairOfFirst() { return of(a); }

    public MutablePair<B, B> pairOfSecond() { return of(b); }


    @Override
    public A getFirst() { return a; }

    @Override
    public B getSecond() { return b; }

    public void setFirst(A a) { this.a = a; }

    public void setSecond(B b) { this.b = b; }

    @Override
    public <R> R apply(BiFunction<? super A, ? super B, ? extends R> func) { return func.apply(a, b); }

    @Override
    public void use(BiConsumer<? super A, ? super B> func) {
        func.accept(a, b);
    }

    public void computeFirst(UnaryOperator<A> operator) {
        this.a = operator.apply(a);
    }

    public void computeSecond(UnaryOperator<B> operator) {
        this.b = operator.apply(b);
    }
}
