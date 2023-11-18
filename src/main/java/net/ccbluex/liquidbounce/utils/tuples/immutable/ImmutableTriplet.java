package net.ccbluex.liquidbounce.utils.tuples.immutable;

import net.ccbluex.liquidbounce.utils.tuples.Triplet;

public final class ImmutableTriplet<A, B, C> extends Triplet<A, B, C> {
    private final A a;
    private final B b;
    private final C c;

    ImmutableTriplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> ImmutableTriplet<A, B, C> of(A a, B b, C c) {
        return new ImmutableTriplet<>(a, b, c);
    }

    public Triplet<A, A, A> pairOfFirst() { return Triplet.of(a); }

    public Triplet<B, B, B> pairOfSecond() { return Triplet.of(b); }

    public Triplet<C, C, C> pairOfThird() { return Triplet.of(c); }


    @Override
    public A getFirst() {
        return a;
    }

    @Override
    public B getSecond() {
        return b;
    }

    @Override
    public C getThird() {
        return c;
    }

    @Override
    public <R> R apply(TriFunction<? super A, ? super B, ? super C, ? extends R> func) {
        return func.apply(a, b, c);
    }

    @Override
    public void use(TriConsumer<? super A, ? super B, ? super C> func) {
        func.accept(a, b, c);
    }
}
