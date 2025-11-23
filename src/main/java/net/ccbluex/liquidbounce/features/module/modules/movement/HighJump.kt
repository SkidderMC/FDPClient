/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.minecraft.block.BlockPane
import net.minecraft.util.BlockPos

object HighJump : Module("HighJump", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {
    private val mode by choices("Mode", arrayOf("Vanilla", "Damage", "AACv3", "DAC", "Mineplex"), "Vanilla")
    private val height by float("Height", 2f, 1.1f..5f) { mode in arrayOf("Vanilla", "Damage") }

    private val glass by boolean("OnlyGlassPane", false)

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer

        if (glass && BlockPos(thePlayer).block !is BlockPane)
            return@handler

        when (mode.lowercase()) {
            "damage" -> if (thePlayer.hurtTime > 0 && thePlayer.onGround) thePlayer.motionY += 0.42f * height
            "aacv3" -> if (!thePlayer.onGround) thePlayer.motionY += 0.059
            "dac" -> if (!thePlayer.onGround) thePlayer.motionY += 0.049999
            "mineplex" -> if (!thePlayer.onGround) strafe(0.35f)
        }
    }

    val onMove = handler<MoveEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (glass && BlockPos(thePlayer).block !is BlockPane)
            return@handler
        if (!thePlayer.onGround) {
            if ("mineplex" == mode.lowercase()) {
                thePlayer.motionY += if (thePlayer.fallDistance == 0f) 0.0499 else 0.05
            }
        }
    }

    val onJump = handler<JumpEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        if (glass && BlockPos(thePlayer).block !is BlockPane)
            return@handler
        when (mode.lowercase()) {
            "vanilla" -> event.motion *= height
            "mineplex" -> event.motion = 0.47f
        }
    }

    override val tag
        get() = mode
}