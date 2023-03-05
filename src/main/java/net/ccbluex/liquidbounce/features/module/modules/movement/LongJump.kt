/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.FloatValue

@ModuleInfo(name = "LongJump", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.FLAG)
class LongJump : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.longjumps", LongJumpMode::class.java)
        .map { it.newInstance() as LongJumpMode }
        .sortedBy { it.modeName }

    private val mode: LongJumpMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "NCP") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    val autoJumpValue = BoolValue("AutoJump", true)
    val autoDisableValue = BoolValue("AutoDisable", true)
    val timerValue = FloatValue("GlobalTimer", 1.0f, 0.1f, 2.0f)
    val onlyAirValue = BoolValue("TimerOnlyAir", true)
    val legacyWarningValue = BoolValue("LegacyWarn", true)
    var airTick = 0
    var isJumped = false
    var noTimerModify = false

    override fun onEnable() {
        airTick = 0
        isJumped = false
        noTimerModify = false
        mode.onEnable()
    }

    override fun onDisable() {
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.capabilities.flySpeed = 0.05f
        mc.thePlayer.noClip = false
        mc.timer.timerSpeed = 1F
        mc.thePlayer.speedInAir = 0.02F
        mode.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(!state) return
        if ((!onlyAirValue.get() || !mc.thePlayer.onGround) && !noTimerModify) {
            mc.timer.timerSpeed = timerValue.get()
        }
        if (!mc.thePlayer.onGround) {
            airTick++
        }else {
            if (airTick > 1 && autoDisableValue.get()) {
                mode.onAttemptDisable()
            } else if (!autoDisableValue.get()) {
                airTick = 0
            }
        }
        mode.onUpdate(event)
        if (autoJumpValue.get() && mc.thePlayer.onGround && MovementUtils.isMoving() && airTick < 2) {
            mode.onAttemptJump()
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(!state) return
        mode.onMotion(event)
        if(event.eventState != EventState.PRE) return
        mode.onPreMotion(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if(!state) return
        mode.onPacket(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if(!state) return
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if(!state) return
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if(!state) return
        mode.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        if(!state) return
        mode.onStep(event)
    }

    override val tag: String
        get() = modeValue.get()

    /**
     * 读取mode中的value并和本体中的value合并
     * 所有的value必须在这个之前初始化
     */
    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                //it.add(value.displayable { modeValue.equals(mode.modeName) })
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}
