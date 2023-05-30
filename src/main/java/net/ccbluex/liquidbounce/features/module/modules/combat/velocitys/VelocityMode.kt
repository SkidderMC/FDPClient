package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.features.value.Value

abstract class VelocityMode(val modeName: String) : MinecraftInstance() {
    protected val valuePrefix = "$modeName-"

    protected val velocity: Velocity
        get() = FDPClient.moduleManager[Velocity::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    open fun onEnable() {}
    open fun onDisable() {}

    open fun onUpdate(event: UpdateEvent) {}
    open fun onVelocity(event: UpdateEvent) {}
    open fun onVelocityPacket(event: PacketEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onAttack(event: AttackEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onWorld(event: WorldEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onBlockBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
    open fun onStrafe(event: StrafeEvent) {}
}
