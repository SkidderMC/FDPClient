/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.FloatValue
import net.ccbluex.liquidbounce.features.ListValue
import net.ccbluex.liquidbounce.features.module.AutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.redesky.*
import net.ccbluex.liquidbounce.utils.MovementUtils

@ModuleInfo(name = "Speed", description = "Allows you to move faster.", category = ModuleCategory.MOVEMENT, autoDisable = AutoDisableType.FLAG)
class Speed : Module() {
    private val speedModes = arrayOf( // NCP
        NCPBHop(),
        NCPFHop(),
        SNCPBHop(),
        NCPHop(),
        YPort(),
        YPort2(),
        NCPYPort(),
        Boost(),
        Frame(),
        MiJump(),
        OnGround(),  // AAC
        AACBHop(),
        AAC2BHop(),
        AAC3BHop(),
        AAC4BHop(),
        AAC5BHop(),
        AAC6BHop(),
        AAC7BHop(),
        AACHop3313(),
        AACHop350(),
        AACLowHop(),
        AACLowHop2(),
        AACLowHop3(),
        AACGround(),
        AACGround2(),
        AACYPort(),
        AACYPort2(),
        AACPort(),
        OldAACBHop(),  // Spartan
        SpartanYPort(),  // Spectre
        SpectreLowHop(),
        SpectreBHop(),
        SpectreOnGround(),
        TeleportCubeCraft(),  // Server
        HiveHop(),
        HypixelHop(),
        MineplexGround(),  // Other
        SlowHop(),
        CustomSpeed(),  // RedeSky
        RedeSkyHop(),
        RedeSkyHop2(),
        RedeSkyHop3(),
        RedeSkyHopOld(),
        RedeSkyGround()
    )

    @JvmField
    val modeValue: ListValue = object : ListValue("Mode", modes, "NCPBHop") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    @JvmField
    val customSpeedValue = FloatValue("CustomSpeed", 1.6f, 0.2f, 2f)
    @JvmField
    val customYValue = FloatValue("CustomY", 0f, 0f, 4f)
    @JvmField
    val customTimerValue = FloatValue("CustomTimer", 1f, 0.1f, 2f)
    @JvmField
    val customStrafeValue = BoolValue("CustomStrafe", true)
    @JvmField
    val resetXZValue = BoolValue("CustomResetXZ", false)
    @JvmField
    val resetYValue = BoolValue("CustomResetY", false)
    @JvmField
    val portMax = FloatValue("AAC-PortLength", 1F, 1F, 20F)
    val redeSkyHopGSpeed = FloatValue("RedeSkyHop-GSpeed", 0.3f, 0.1f, 0.7f)
    val redeSkyHeight = FloatValue("RedeSkyHeight", 0.45f, 0.30f, 0.55f)
    val redeSkyHopTimer = FloatValue("RedeSkyHop-Timer", 6f, 1.1f, 10f)
    val redeSkyHop3Speed = FloatValue("RedeSkyHop3-Speed", 0.07f, 0.01f, 0.1f)
    @JvmField
    val aacGroundTimerValue = FloatValue("AACGround-Timer", 3f, 1.1f, 10f)
    @JvmField
    val cubecraftPortLengthValue = FloatValue("CubeCraft-PortLength", 1f, 0.1f, 2f)
    @JvmField
    val mineplexGroundSpeedValue = FloatValue("MineplexGround-Speed", 0.5f, 0.1f, 1f)

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.isSneaking) return
        if (MovementUtils.isMoving()) mc.thePlayer.isSprinting = true
        val speedMode = mode
        speedMode?.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isSneaking || event.eventState !== EventState.PRE) return
        if (MovementUtils.isMoving()) mc.thePlayer.isSprinting = true
        val speedMode = mode
        speedMode?.onMotion()
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer.isSneaking) return
        val speedMode = mode
        speedMode?.onMove(event)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.thePlayer.isSneaking) return
        val speedMode = mode
        speedMode?.onTick()
    }

    override fun onEnable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        val speedMode = mode
        speedMode?.onEnable()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        val speedMode = mode
        speedMode?.onDisable()
    }

    override val tag: String
        get() = modeValue.get()

    private val mode: SpeedMode?
        get() {
            val mode = modeValue.get()
            for (speedMode in speedModes) if (speedMode.modeName.equals(mode, ignoreCase = true)) return speedMode
            return null
        }

    private val modes: Array<String>
        get() {
            val list: MutableList<String> = ArrayList()
            for (speedMode in speedModes) list.add(speedMode.modeName)
            return list.toTypedArray()
        }
}