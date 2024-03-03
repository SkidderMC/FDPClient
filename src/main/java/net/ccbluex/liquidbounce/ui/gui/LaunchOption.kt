/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.gui

/**
 * used at initialize something dynamic
 */
abstract class LaunchOption {
    /**
     * called when startClient
     */
    open fun start() {}

    /**
     * called when stopClient
     */
    open fun stop() {}
}