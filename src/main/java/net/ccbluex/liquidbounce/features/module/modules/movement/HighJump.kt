/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockPane
import net.minecraft.util.BlockPos

@ModuleInfo(name = "HighJump", category = ModuleCategory.MOVEMENT)
class HighJump : Module() {
    private val heightValue = FloatValue("Height", 2f, 1.1f, 7f)
    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "StableMotion", "Damage", "AACv3", "DAC", "Mineplex"), "Vanilla")
    private val glassValue = BoolValue("OnlyGlassPane", false)
    private val stableMotionValue = FloatValue("StableMotion", 0.42f, 0.1f, 1f)
    private var jumpY = 114514.0

    override fun onEnable() {
        jumpY = 114514.0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (glassValue.get() && getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) !is BlockPane) return

        when (modeValue.get().toLowerCase()) {
            "damage" -> {
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) mc.thePlayer.motionY += (0.42f * heightValue.get()).toDouble()
            }

            "aacv3" -> {
                if (!mc.thePlayer.onGround) mc.thePlayer.motionY += 0.059
            }

            "dac" -> {
                if (!mc.thePlayer.onGround) mc.thePlayer.motionY += 0.049999
            }

            "mineplex" -> {
                if (!mc.thePlayer.onGround) MovementUtils.strafe(0.35f)
            }

            "stablemotion" -> {
                if (jumpY != 114514.0) {
                    if (jumpY + heightValue.get() - 1 > mc.thePlayer.posY) {
                        mc.thePlayer.motionY = stableMotionValue.get().toDouble()
                    } else {
                        jumpY = 114514.0
                    }
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (glassValue.get() && getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) !is BlockPane) return

        if (!mc.thePlayer.onGround) {
            if ("mineplex" == modeValue.get().toLowerCase()) {
                mc.thePlayer.motionY += if (mc.thePlayer.fallDistance == 0f) 0.0499 else 0.05
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (glassValue.get() && getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) !is BlockPane) return

        when (modeValue.get().toLowerCase()) {
            "vanilla" -> {
                event.motion = event.motion * heightValue.get()
            }

            "mineplex" -> {
                event.motion = 0.47f
            }

            "stablemotion" -> {
                jumpY = mc.thePlayer.posY
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}