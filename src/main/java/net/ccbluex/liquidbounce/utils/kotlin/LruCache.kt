/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.kotlin

/**
 * Thread-safe LRU (Least Recently Used) cache implementation.
 *
 * This cache automatically removes the least recently used entries when the maximum size is exceeded.
 * Access order is maintained, so calling get() or put() moves the entry to the end.
 *
 * @param K The type of keys maintained by this cache
 * @param V The type of mapped values
 * @param maxSize The maximum number of entries to keep in the cache
 */
class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize, 0.75f, true) {

    /**
     * Returns true if the eldest entry should be removed.
     * This method is invoked by put and putAll after inserting a new entry.
     */
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }

    /**
     * Returns the value for the given key, or computes it using the defaultValue function if not present.
     * The computed value is stored in the cache.
     *
     * @param key The key whose associated value is to be returned
     * @param defaultValue A function that computes the value if the key is not present
     * @return The value associated with the key, either existing or newly computed
     */
    @Synchronized
    fun getOrPut(key: K, defaultValue: () -> V): V {
        return get(key) ?: defaultValue().also { put(key, it) }
    }

    /**
     * Thread-safe get operation.
     */
    @Synchronized
    override fun get(key: K): V? {
        return super.get(key)
    }

    /**
     * Thread-safe put operation.
     */
    @Synchronized
    override fun put(key: K, value: V): V? {
        return super.put(key, value)
    }
}
