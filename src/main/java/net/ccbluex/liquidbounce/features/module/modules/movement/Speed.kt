/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.ReflectUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Speed", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.FLAG)
class Speed : Module() {
    val modes=ReflectUtils.getReflects("${this.javaClass.`package`.name}.speeds",SpeedMode::class.java)
        .map { it.newInstance() as SpeedMode }
        .sortedBy { it.modeName }

    val mode: SpeedMode
        get() = modes.filter { it.modeName.equals(modeValue.get(),true) }.get(0)

    val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "NCPBHop") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    val customSpeedValue = FloatValue("CustomSpeed", 1.6f, 0.2f, 2f)
    val customLaunchSpeedValue = FloatValue("CustomLaunchSpeed", 1.6f, 0.2f, 2f)
    val customAddYMotionValue = FloatValue("CustomAddYMotion", 0f, 0f, 2f)
    val customYValue = FloatValue("CustomY", 0f, 0f, 4f)
    val customUpTimerValue = FloatValue("CustomUpTimer", 1f, 0.1f, 2f)
    val customDownTimerValue = FloatValue("CustomDownTimer", 1f, 0.1f, 2f)
    val customStrafeValue = ListValue("CustomStrafe", arrayOf("Strafe","Boost","Plus","PlusOnlyUp","Non-Strafe"),"Boost")
    val customGroundStay = IntegerValue("CustomGroundStay",0,0,10)
    val groundResetXZValue = BoolValue("CustomGroundResetXZ", false)
    val resetXZValue = BoolValue("CustomResetXZ", false)
    val resetYValue = BoolValue("CustomResetY", false)
    val launchSpeedValue = BoolValue("CustomDoLaunchSpeed", true)
    val portMax = FloatValue("AAC-PortLength", 1F, 1F, 20F)
    val redeSkyHopGSpeed = FloatValue("RedeSkyHop-GSpeed", 0.3f, 0.1f, 0.7f)
    val redeSkyHeight = FloatValue("RedeSkyHeight", 0.45f, 0.30f, 0.55f)
    val redeSkyHopTimer = FloatValue("RedeSkyHop-Timer", 6f, 1.1f, 10f)
    val redeSkyHop3Speed = FloatValue("RedeSkyHop3-Speed", 0.07f, 0.01f, 0.1f)
    val aacGroundTimerValue = FloatValue("AACGround-Timer", 3f, 1.1f, 10f)
    val cubecraftPortLengthValue = FloatValue("CubeCraft-PortLength", 1f, 0.1f, 2f)

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.isSneaking) return
        if (MovementUtils.isMoving()) mc.thePlayer.isSprinting = true
        mode.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isSneaking || event.eventState !== EventState.PRE) return
        if (MovementUtils.isMoving()) mc.thePlayer.isSprinting = true
        mode.onMotion()
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer.isSneaking) return
        mode.onMove(event)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.thePlayer.isSneaking) return
        mode.onTick()
    }

    override fun onEnable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        mode.onEnable()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        mode.onDisable()
    }

    override val tag: String
        get() = modeValue.get()
}
