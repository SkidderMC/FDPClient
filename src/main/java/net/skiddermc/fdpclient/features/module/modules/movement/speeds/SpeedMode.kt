/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.movement.speeds

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.MotionEvent
import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.features.module.modules.movement.Speed
import net.skiddermc.fdpclient.utils.ClassUtils
import net.skiddermc.fdpclient.utils.MinecraftInstance
import net.skiddermc.fdpclient.value.Value

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
    open fun onPacket(event: PacketEvent) {}
    open fun onTick() {}

    open val noJump = false
}
