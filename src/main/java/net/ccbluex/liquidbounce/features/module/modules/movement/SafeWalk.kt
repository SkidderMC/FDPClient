/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.minecraft.block.BlockAir
import net.minecraft.util.BlockPos

object SafeWalk : Module("SafeWalk", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val airSafe by boolean("AirSafe", false)
    private val maxFallDistanceValue = int("MaxFallDistance", 5, 0..100)

    private var lastGroundY: Double? = null
    private var lastCollisionY: Int? = null

    val onMove = handler<MoveEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        if (player.capabilities.allowFlying || player.capabilities.isFlying
            || !mc.playerController.gameIsSurvivalOrAdventure()
        ) return@handler

        if (!maxFallDistanceValue.isMinimal() && player.onGround && BlockPos(player).down().block !is BlockAir) {
            lastGroundY = player.posY
            lastCollisionY = FallingPlayer(player, true).findCollision(60)?.pos?.y
        }

        if (airSafe || player.onGround) {
            event.isSafeWalk = maxFallDistanceValue.isMinimal()
                    || (lastGroundY != null && lastCollisionY != null
                    && lastGroundY!! - lastCollisionY!! > maxFallDistanceValue.get() + 1)
        }
    }
}
