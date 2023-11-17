package net.ccbluex.liquidbounce.utils.tuples.mutable;

import net.ccbluex.liquidbounce.utils.tuples.Unit;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MutableUnit<A> extends Unit<A> {
    private A a;

    MutableUnit(A a){
        this.a = a;
    }

    public static <A> MutableUnit<A> of(A a){
        return new MutableUnit<>(a);
    }

    @Override
    public A get() {
        return a;
    }

    public void set(A a) { this.a = a; }

    @Override
    public <R> R apply(Function<? super A, ? extends R> func) {
        return func.apply(a);
    }

    @Override
    public void use(Consumer<? super A> func) {
        func.accept(a);
    }

    public void compute(UnaryOperator<A> mapper) {
        this.a = mapper.apply(a);
    }
}
