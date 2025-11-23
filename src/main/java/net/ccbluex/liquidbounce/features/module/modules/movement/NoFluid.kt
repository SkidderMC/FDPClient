/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.init.Blocks.lava
import net.minecraft.init.Blocks.water
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action
import net.minecraft.util.EnumFacing

object NoFluid : Module("NoFluid", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    val waterValue by boolean("Water", true)
    val lavaValue by boolean("Lava", true)
    private val oldGrim by boolean("OldGrim", false)

    val onUpdate = handler<UpdateEvent> {
        if ((waterValue || lavaValue) && oldGrim) {
            BlockUtils.searchBlocks(2, setOf(water, lava)).keys.forEach {
                // TODO:only do this for blocks that player touched
                sendPacket(C07PacketPlayerDigging(Action.STOP_DESTROY_BLOCK, it, EnumFacing.DOWN))
            }
        }
    }
}