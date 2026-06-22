/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.util.EnumParticleTypes
import kotlin.random.Random

object Vomit : Module("Vomit", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val amount by int("Amount", 8, 1..40)
        .describe("Number of slime particles spawned per tick.")
    private val onlySneaking by boolean("OnlySneaking", false)
        .describe("Only spawn particles while sneaking.")

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (onlySneaking && !player.isSneaking) {
            return@handler
        }

        val look = player.getLook(1f)
        val baseX = player.posX + look.xCoord * 0.5
        val baseY = player.posY + player.getEyeHeight() - 0.25
        val baseZ = player.posZ + look.zCoord * 0.5

        repeat(amount) {
            world.spawnParticle(
                EnumParticleTypes.SLIME,
                baseX + (Random.nextDouble() - 0.5) * 0.3,
                baseY + (Random.nextDouble() - 0.5) * 0.2,
                baseZ + (Random.nextDouble() - 0.5) * 0.3,
                look.xCoord * 0.35,
                -0.15,
                look.zCoord * 0.35
            )
        }
    }
}
