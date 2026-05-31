/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.other.Fucker
import net.ccbluex.liquidbounce.features.module.modules.other.Nuker
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition

object FastBreak : Module("FastBreak", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val mode by choices("Mode", arrayOf("Simple", "Tick"), "Simple")

    // Simple mode
    private val breakDamage by float("BreakDamage", 0.8F, 0.1F..1F) { mode == "Simple" }

    // Tick mode no longer has a range parameter — works on any block you are aiming at while mining

    private var tickMiningPos: BlockPos? = null
    private var tickMiningFacing: EnumFacing? = null

    override fun onDisable() {
        tickMiningPos = null
        tickMiningFacing = null
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler
        val controller = mc.playerController ?: return@handler

        if (player.isDead) return@handler

        when (mode.lowercase()) {
            "simple" -> {
                controller.blockHitDelay = 0
                if (controller.curBlockDamageMP > breakDamage)
                    controller.curBlockDamageMP = 1F
                if (Fucker.currentDamage > breakDamage)
                    Fucker.currentDamage = 1F
                if (Nuker.currentDamage > breakDamage)
                    Nuker.currentDamage = 1F
            }

            "tick" -> {
                val objectMouseOver = mc.objectMouseOver
                val digging = mc.gameSettings.keyBindAttack.pressed

                // Same requirement as vanilla mining: must hold attack key and aim at a block
                if (digging && objectMouseOver != null &&
                    objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                ) {
                    val pos = objectMouseOver.blockPos
                    val facing = objectMouseOver.sideHit
                    if (pos != null && facing != null) {
                        tickMiningPos = pos
                        tickMiningFacing = facing
                    } else {
                        tickMiningPos = null
                        tickMiningFacing = null
                    }
                } else {
                    tickMiningPos = null
                    tickMiningFacing = null
                }

                val pos = tickMiningPos ?: return@handler
                val facing = tickMiningFacing ?: return@handler

                // Send instant break packets without any distance check
                sendPackets(
                    C07PacketPlayerDigging(START_DESTROY_BLOCK, pos, facing),
                    C07PacketPlayerDigging(STOP_DESTROY_BLOCK, pos, facing)
                )

                controller.clickBlock(pos, facing)

                // Stop if the block is already air
                if (world.isAirBlock(pos)) {
                    tickMiningPos = null
                    tickMiningFacing = null
                }
            }
        }
    }
}