/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object AutoBow : Module("AutoBow", Category.COMBAT, subjective = true) {

    private val waitForBowAimbot by boolean("WaitForBowAimbot", true)

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer

        if (thePlayer.isUsingItem && thePlayer.heldItem?.item is ItemBow && thePlayer.itemInUseDuration > 20
            && (!waitForBowAimbot || !ProjectileAimbot.handleEvents() || ProjectileAimbot.hasTarget())
        ) {
            thePlayer.stopUsingItem()
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }
}
