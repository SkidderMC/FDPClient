package net.skiddermc.fdpclient.features.module.modules.movement.glides

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.*
import net.skiddermc.fdpclient.features.module.modules.movement.Glide
import net.skiddermc.fdpclient.utils.ClassUtils
import net.skiddermc.fdpclient.utils.MinecraftInstance
import net.skiddermc.fdpclient.value.Value

abstract class GlideMode(val modeName: String) : MinecraftInstance() {
    protected val valuePrefix = "$modeName-"

    protected val glide: Glide
        get() = FDPClient.moduleManager[Glide::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    open fun onEnable() {}
    open fun onDisable() {}

    open fun onUpdate(event: UpdateEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onBlockBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
}