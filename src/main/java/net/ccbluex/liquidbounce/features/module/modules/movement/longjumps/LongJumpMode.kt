package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.features.value.Value
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils

abstract class LongJumpMode(val modeName: String) : MinecraftInstance() {
    protected val valuePrefix = "$modeName-"

    protected val longjump: LongJump
        get() = LiquidBounce.moduleManager[LongJump::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    fun sendLegacy() {
        if(!longjump.legacyWarningValue.get()) return

        LiquidBounce.hud.addNotification(Notification("LongJump", "This bypass is for an outdated anti cheat version!", NotifyType.WARNING, 1000))
    }

    open fun onEnable() {}
    open fun onDisable() {}
    
    open fun onAttemptJump() {}
    open fun onAttemptDisable() {}

    open fun onUpdate(event: UpdateEvent) {}
    open fun onPreMotion(event: MotionEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onBlockBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
}
