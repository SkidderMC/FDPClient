package net.ccbluex.liquidbounce.launch

/**
 * used at initialize something dynamic
 */
abstract class LaunchOption {
    /**
     * called when startClient head
     */
    open fun head() {}

    /**
     * called when startClient complete
     */
    open fun after() {}

    /**
     * called when stopClient complete
     */
    open fun stop() {}
}