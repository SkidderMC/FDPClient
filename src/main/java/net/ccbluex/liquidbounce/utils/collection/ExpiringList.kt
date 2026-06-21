package net.ccbluex.liquidbounce.utils.collection

class ExpiringList<E>(private val defaultTtl: Long = 1000L) : Iterable<E> {

    private class Entry<T>(val value: T, val expiresAt: Long)

    private val entries = ArrayDeque<Entry<E>>()

    private fun purge(now: Long = System.currentTimeMillis()) {
        while (entries.isNotEmpty() && entries.first().expiresAt <= now) {
            entries.removeFirst()
        }
    }

    fun add(element: E) = add(element, defaultTtl)

    fun add(element: E, ttl: Long) {
        val now = System.currentTimeMillis()
        purge(now)
        entries.add(Entry(element, now + ttl))
    }

    val size: Int
        get() {
            purge()
            return entries.size
        }

    fun isEmpty(): Boolean {
        purge()
        return entries.isEmpty()
    }

    fun contains(element: E): Boolean {
        purge()
        return entries.any { it.value == element }
    }

    fun timeToDie(element: E): Long {
        val now = System.currentTimeMillis()
        purge(now)
        val entry = entries.firstOrNull { it.value == element } ?: return 0L
        return (entry.expiresAt - now).coerceAtLeast(0L)
    }

    fun clear() = entries.clear()

    fun toList(): List<E> {
        purge()
        return entries.map { it.value }
    }

    override fun iterator(): Iterator<E> {
        purge()
        return entries.map { it.value }.iterator()
    }
}
