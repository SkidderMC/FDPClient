package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

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
    var jumped = false
    var hasJumped = false
    var no = false

    override fun onEnable() {
        jumped = false
        hasJumped = false
        no = false
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
        mode.onUpdate(event)
        if (!no && autoJumpValue.get() && mc.thePlayer.onGround && MovementUtils.isMoving()) {
            jumped = true
            if (hasJumped && autoDisableValue.get()) {
                state = false
                return
            }
            mc.thePlayer.jump()
            hasJumped = true
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
        jumped = true
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
    override val values = super.values.toMutableList().also { modes.map { mode -> mode.values.forEach { value -> it.add(value.displayable { modeValue.equals(mode.modeName) }) } } }
}