/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.math;

import java.util.Iterator;

public class IntRange implements Iterable<Integer> {
    private final int start;
    private final int end;
    private final int step;

    public IntRange(int end) {
        this(0, end, 1);
    }

    public IntRange(int start, int end) {
        this(start, end, 1);
    }

    public IntRange(int start, int end, int step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Itr();
    }
    private class Itr implements Iterator<Integer> {
        int current = start;
        @Override
        public boolean hasNext() {
            return step > 0 ? current < end : current > end;
        }

        @Override
        public Integer next() {
            int t = current;
            current += step;
            return t;
        }
    }
}
