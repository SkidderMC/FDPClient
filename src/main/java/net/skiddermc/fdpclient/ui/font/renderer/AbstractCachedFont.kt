package net.skiddermc.fdpclient.ui.font.renderer

abstract class AbstractCachedFont(var lastUsage: Long) {
    abstract fun finalize()
}