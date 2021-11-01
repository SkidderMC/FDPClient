package net.ccbluex.liquidbounce.ui.font.renderer

abstract class AbstractCachedFont(var lastUsage: Long) {
    abstract fun finalize()
}