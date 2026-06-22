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
import net.minecraft.entity.player.EnumPlayerModelParts

object SkinDerp : Module("SkinDerp", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val sync by boolean("Sync", false)
        .describe("Toggle all parts together instead of randomly.")
    private val delay by int("Delay", 0, 0..20, "ticks")
        .describe("Ticks to wait between each part toggle.")

    private val hat by boolean("Hat", true)
        .describe("Include the hat layer in the randomization.")
    private val jacket by boolean("Jacket", true)
        .describe("Include the jacket layer in the randomization.")
    private val leftPants by boolean("LeftPants", true)
        .describe("Include the left pants leg in the randomization.")
    private val rightPants by boolean("RightPants", true)
        .describe("Include the right pants leg in the randomization.")
    private val leftSleeve by boolean("LeftSleeve", true)
        .describe("Include the left sleeve in the randomization.")
    private val rightSleeve by boolean("RightSleeve", true)
        .describe("Include the right sleeve in the randomization.")
    private val cape by boolean("Cape", true)
        .describe("Include the cape in the randomization.")

    private var savedParts = emptySet<EnumPlayerModelParts>()
    private var tickCounter = 0

    override fun onEnable() {
        savedParts = HashSet(mc.gameSettings.modelParts)
        tickCounter = 0
    }

    override fun onDisable() {
        for (part in EnumPlayerModelParts.values()) {
            mc.gameSettings.setModelPartEnabled(part, false)
        }
        for (part in savedParts) {
            mc.gameSettings.setModelPartEnabled(part, true)
        }
        super.onDisable()
    }

    val onUpdate = handler<UpdateEvent> {
        if (delay > 0) {
            tickCounter++
            if (tickCounter < delay) {
                return@handler
            }
            tickCounter = 0
        }

        for (part in selectedParts()) {
            if (sync) {
                mc.gameSettings.switchModelPartEnabled(part)
            } else {
                mc.gameSettings.setModelPartEnabled(part, Math.random() < 0.5)
            }
        }
    }

    private fun selectedParts(): List<EnumPlayerModelParts> {
        val parts = ArrayList<EnumPlayerModelParts>()
        if (hat) parts.add(EnumPlayerModelParts.HAT)
        if (jacket) parts.add(EnumPlayerModelParts.JACKET)
        if (leftPants) parts.add(EnumPlayerModelParts.LEFT_PANTS_LEG)
        if (rightPants) parts.add(EnumPlayerModelParts.RIGHT_PANTS_LEG)
        if (leftSleeve) parts.add(EnumPlayerModelParts.LEFT_SLEEVE)
        if (rightSleeve) parts.add(EnumPlayerModelParts.RIGHT_SLEEVE)
        if (cape) parts.add(EnumPlayerModelParts.CAPE)
        return parts
    }
}
