/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
abstract class SpeedMode(val modeName: String) : MinecraftInstance() {
    val isActive: Boolean
        get() {
            val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed?
            return speed != null && !mc.thePlayer.isSneaking && speed.state && speed.modeValue.get() == modeName
        }

    open fun onMotion() {}
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onTick() {}
    open fun onEnable() {}
    open fun onDisable() {}
}