/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.server.S19PacketEntityStatus
import org.lwjgl.input.Keyboard
import java.awt.Color

@ModuleInfo(name = "Fly", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.FLAG, keyBind = Keyboard.KEY_F)
class Fly : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.flys", FlyMode::class.java)
        .map { it.newInstance() as FlyMode }
        .sortedBy { it.modeName }

    private val mode: FlyMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Vanilla") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val motionResetValue = BoolValue("MotionReset", false)


    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Up", "Down", "Off"), "Up")
    private val fakeDamageValue = BoolValue("FakeDamage", false)
    private val viewBobbingValue = BoolValue("ViewBobbing", false)
    private val viewBobbingYawValue = FloatValue("ViewBobbingYaw", 0.1f, 0f, 0.5f).displayable { viewBobbingValue.get() }
    val legacyWarningValue = BoolValue("LegacyWarn", false)

    var launchX = 0.0
    var launchY = 0.0
    var launchZ = 0.0
    var launchYaw = 0f
    var launchPitch = 0f

    var antiDesync = false

    var needReset = true

    override fun onEnable() {
        antiDesync = false
        needReset = true
        if (mc.thePlayer.onGround && fakeDamageValue.get()) {
            val event = PacketEvent(S19PacketEntityStatus(mc.thePlayer, 2.toByte()), PacketEvent.Type.RECEIVE)
            LiquidBounce.eventManager.callEvent(event)
            if (!event.isCancelled) {
                mc.thePlayer.handleStatusUpdate(2.toByte())
            }
        }

        launchX = mc.thePlayer.posX
        launchY = mc.thePlayer.posY
        launchZ = mc.thePlayer.posZ
        launchYaw = mc.thePlayer.rotationYaw
        launchPitch = mc.thePlayer.rotationPitch

        mode.onEnable()
    }

    override fun onDisable() {
        antiDesync = false
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.capabilities.flySpeed = 0.05f
        mc.thePlayer.noClip = false

        mc.timer.timerSpeed = 1F
        mc.thePlayer.speedInAir = 0.02F

        if (motionResetValue.get() && needReset) MovementUtils.resetMotion(true)

        mode.onDisable()
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        if (markValue.equals("Off")) {
            return
        }

        RenderUtils.drawPlatform(
            if (markValue.equals("Up")) launchY + 2.0 else launchY,
            if (mc.thePlayer.entityBoundingBox.maxY < launchY + 2.0) Color(0, 255, 0, 90) else Color(255, 0, 0, 90),
            1.0)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(viewBobbingValue.get()) {
            mc.thePlayer.cameraYaw = viewBobbingYawValue.get()
            mc.thePlayer.prevCameraYaw = viewBobbingYawValue.get()
        }
        mode.onMotion(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        mode.onWorld(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
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
