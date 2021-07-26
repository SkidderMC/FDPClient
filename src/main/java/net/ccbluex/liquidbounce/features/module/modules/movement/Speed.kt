/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.redesky.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Speed", description = "Allows you to move faster.", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.FLAG)
class Speed : Module() {
    private val RedeskyModes = arrayOf(
        RedeSkyHop(),
        RedeSkyHop2(),
        RedeSkyHop3(),
        RedeSkyHopOld(),
        RedeSkyGround()
    )
    private val AACmodes = arrayOf(
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
        OldAACBHop()
    )

    private val NCPCModes = arrayOf(
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
        OnGround()
    )

    private val SpectreModes = arrayOf(
        SpartanYPort(),
        SpectreLowHop(),
        SpectreBHop(),
        SpectreOnGround()
    )

    private val ServersModes = arrayOf(
        TeleportCubeCraft(),
        HiveHop(),
        HypixelHop(),
        Matrix(),
        VerusYPort(),
        MineplexBHop()
    )

    private val OtherModes = arrayOf(
        SlowHop(),
        CustomSpeed(),
        Autojump()
    )

    val modeValue: ListValue = object : ListValue("Mode", modes, "NCPBHop") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    val customSpeedValue = FloatValue("CustomSpeed", 1.6f, 0.2f, 2f)
    val customYValue = FloatValue("CustomY", 0f, 0f, 4f)
    val customTimerValue = FloatValue("CustomTimer", 1f, 0.1f, 2f)
    val customStrafeValue = ListValue("CustomStrafe", arrayOf("Strafe","Boost","Non-Strafe"),"Boost")
    val customGroundStay = IntegerValue("CustomGroundStay",0,0,10)
    val groundResetXZValue = BoolValue("CustomGroundResetXZ", false)
    val resetXZValue = BoolValue("CustomResetXZ", false)
    val resetYValue = BoolValue("CustomResetY", false)
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
        mode?.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isSneaking || event.eventState !== EventState.PRE) return
        if (MovementUtils.isMoving()) mc.thePlayer.isSprinting = true
        mode?.onMotion()
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer.isSneaking) return
        mode?.onMove(event)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.thePlayer.isSneaking) return
        mode?.onTick()
    }

    override fun onEnable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        mode?.onEnable()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        mode?.onDisable()
    }

    override val tag: String
        get() = modeValue.get()

    private val mode: SpeedMode?
        get() {
            val mode = modeValue.get()
            for (speedMode in RedeskyModes) if (speedMode.modeName.equals(mode, ignoreCase = true)) return speedMode
            for (speedMode in AACmodes) if (speedMode.modeName.equals(mode, ignoreCase = true)) return speedMode
            for (speedMode in NCPCModes) if (speedMode.modeName.equals(mode, ignoreCase = true)) return speedMode
            for (speedMode in SpectreModes) if (speedMode.modeName.equals(mode, ignoreCase = true)) return speedMode
            for (speedMode in ServersModes) if (speedMode.modeName.equals(mode, ignoreCase = true)) return speedMode
            for (speedMode in OtherModes) if (speedMode.modeName.equals(mode, ignoreCase = true)) return speedMode
            return null
        }

    private val modes: Array<String>
        get() {
            val list: MutableList<String> = ArrayList()
            for (speedMode in RedeskyModes) list.add(speedMode.modeName)
            for (speedMode in AACmodes) list.add(speedMode.modeName)
            for (speedMode in NCPCModes) list.add(speedMode.modeName)
            for (speedMode in SpectreModes) list.add(speedMode.modeName)
            for (speedMode in ServersModes) list.add(speedMode.modeName)
            for (speedMode in OtherModes) list.add(speedMode.modeName)
            return list.toTypedArray()
        }
}
