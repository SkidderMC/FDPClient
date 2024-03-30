package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.potion.Potion

class LatestVerusHop : SpeedMode ("LatestVerusHop") {
    

    // Custom
    private val customSpeed = BoolValue("${valuePrefix}-CustomSpeed", false)
    private val custommovementfactorPOT = FloatValue("${valuePrefix}-JumpMovementFactorWithPotion", 0.02f, 0.01f, 0.04f).displayable { customSpeed.get() }
    private val custommovementfactorNOPOT = FloatValue("${valuePrefix}-JumpMovementFactorWithoutPotion", 0.02f, 0.01f, 0.04f).displayable { customSpeed.get() }
    private val customstrafe = FloatValue("${valuePrefix}-FrictionWithPotion", 0.48f, 0.1f, 2f).displayable { customSpeed.get() }
    private val customNOstrafe = FloatValue("${valuePrefix}-FrictionWithoutPotion", 0.48f, 0.1f, 2f).displayable { customSpeed.get() }
    private val customSpeedValue = FloatValue("${valuePrefix}-SpeedWithPotion", 2.8f, 1f, 4f).displayable { customSpeed.get() }
    private val customSpeedNoValue = FloatValue("${valuePrefix}-SpeedWhithoutPotion", 2.0f, 1f, 4f).displayable { customSpeed.get() }
    
    // Damage Boost
    private val boost = BoolValue("${valuePrefix}-Damage-Boost", false)
    private val boostvalue = FloatValue("${valuePrefix}-Boost-Speed", 1f, 0.1f,9f).displayable { boost.get() }

    private var ticks = 0

    override fun onEnable() {
        ticks = 0
    }

    override fun onUpdate() {
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && MovementUtils.isMoving()) {
            ++ticks
            mc.thePlayer.jumpMovementFactor = if (customSpeed.get()) { custommovementfactorPOT.get() } else { 0.02f }
            mc.thePlayer.speedInAir = if (customSpeed.get()) { customSpeedValue.get() / 100 } else { 0.028f }
            mc.gameSettings.keyBindJump.pressed = false
            if (boost.get() && mc.thePlayer.hurtTime == 9) {
                MovementUtils.strafe(boostvalue.get())
            }
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                ticks = 0
                mc.thePlayer.motionY = 0.41999998688697815
                MovementUtils.strafe(if (customSpeed.get()) { customstrafe.get() } else { 0.48f })
            }
            MovementUtils.strafe()
        } else {
            mc.thePlayer.jumpMovementFactor = if (customSpeed.get()) { custommovementfactorNOPOT.get() } else { 0.02f }
            if (boost.get() && mc.thePlayer.hurtTime == 9) {
                MovementUtils.strafe(boostvalue.get());
            }
            mc.thePlayer.speedInAir = if (customSpeed.get()) { customSpeedNoValue.get() / 100 } else { 0.02f }
            mc.gameSettings.keyBindJump.pressed = false
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.motionY = 0.41999998688697815
                MovementUtils.strafe(if (customSpeed.get()) { customNOstrafe.get() } else { 0.48f })
            }
            MovementUtils.strafe()
        }
    }
    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
    }
}