/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.isNearEdge
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.BlockPos

object SafeWalk : Module("SafeWalk", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val airSafe by boolean("AirSafe", false)
    private val maxFallDistanceValue = int("MaxFallDistance", 5, 0..100)

    private val edgeSneak by boolean("EdgeSneak", false)
    private val edgeSneakDistance by float("EdgeSneakDistance", 0.7F, 0.1F..3F) { edgeSneak }
    private val edgeSneakMode by choices("EdgeSneakMode", arrayOf("Normal", "Packet"), "Normal") { edgeSneak }

    private var lastGroundY: Double? = null
    private var lastCollisionY: Int? = null

    private var edgeSneaking = false

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

    val onUpdate = handler<UpdateEvent> {
        if (!edgeSneak) return@handler

        val player = mc.thePlayer ?: return@handler
        if (player.capabilities.allowFlying || player.capabilities.isFlying
            || !mc.playerController.gameIsSurvivalOrAdventure()
        ) {
            releaseEdgeSneak()
            return@handler
        }

        val shouldSneak = player.onGround && player.isNearEdge(edgeSneakDistance)

        if (shouldSneak) {
            if (!edgeSneaking) {
                edgeSneaking = true
                if (edgeSneakMode == "Packet") {
                    sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SNEAKING))
                }
            }

            if (edgeSneakMode == "Normal") {
                mc.gameSettings.keyBindSneak.pressed = true
            }
        } else {
            releaseEdgeSneak()
        }
    }

    override fun onDisable() {
        releaseEdgeSneak()
    }

    private fun releaseEdgeSneak() {
        if (!edgeSneaking) return
        edgeSneaking = false

        val player = mc.thePlayer
        if (player != null && edgeSneakMode == "Packet") {
            sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
    }
}
