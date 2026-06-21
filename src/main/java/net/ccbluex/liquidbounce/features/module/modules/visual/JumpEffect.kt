/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.util.EnumParticleTypes
import kotlin.random.Random

object JumpEffect : Module("JumpEffect", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val particleType by choices("Particle", arrayOf("Cloud", "Explosion", "Crit", "Slime", "Lava", "Heart"), "Cloud")
    private val amount by int("Amount", 12, 1..50)

    val onJump = handler<JumpEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        val type = when (particleType) {
            "Explosion" -> EnumParticleTypes.EXPLOSION_NORMAL
            "Crit" -> EnumParticleTypes.CRIT
            "Slime" -> EnumParticleTypes.SLIME
            "Lava" -> EnumParticleTypes.LAVA
            "Heart" -> EnumParticleTypes.HEART
            else -> EnumParticleTypes.CLOUD
        }

        repeat(amount) {
            world.spawnParticle(
                type,
                player.posX + (Random.nextDouble() - 0.5) * 0.6,
                player.posY + 0.05,
                player.posZ + (Random.nextDouble() - 0.5) * 0.6,
                (Random.nextDouble() - 0.5) * 0.2,
                0.0,
                (Random.nextDouble() - 0.5) * 0.2
            )
        }
    }
}
