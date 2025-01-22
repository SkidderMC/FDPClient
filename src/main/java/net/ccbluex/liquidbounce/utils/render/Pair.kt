/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

data class Pair<K, V>(
    private var _key: K,
    private var _value: V
) {
    var key: K
        get() = _key
        set(value) {
            _key = value
        }

    var value: V
        get() = _value
        set(value) {
            _value = value
        }
}