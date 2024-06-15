/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.JumpEvent
import me.zywl.fdpclient.event.MotionEvent
import me.zywl.fdpclient.event.MoveEvent
import me.zywl.fdpclient.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import me.zywl.fdpclient.value.Value

abstract class SpeedMode(val modeName: String) : MinecraftInstance() {
    protected val valuePrefix = "$modeName-"

    protected val speed: Speed
        get() = FDPClient.moduleManager[Speed::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    open fun onEnable() {}
    open fun onDisable() {}

    open fun onPreMotion() {}
    open fun onMotion(event: MotionEvent) {}
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onTick() {}
}
