/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import org.lwjgl.opengl.GL11
import java.awt.Color

object Chams : Module("Chams", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    val targets by boolean("Targets", true)
        .describe("Apply chams to combat targets.")
    val chests by boolean("Chests", true)
        .describe("Apply chams to chests.")
    val items by boolean("Items", true)
        .describe("Apply chams to dropped items.")
    private val handValue by boolean("Hand", false)
        .describe("Apply chams to your own hand.")

    val localPlayerValue by boolean("LocalPlayer", true)
        .describe("Apply chams to your own player.")
    val colorModeValue by choices("Color", arrayOf("Custom", "Fade"), "Custom") { !legacyMode }
        .describe("Coloring style for the chams.")

    val texturedValue by boolean("Textured", true) { !legacyMode }
        .describe("Keep entity textures under the chams.")
    val legacyMode by boolean("Legacy-Mode", false)
        .describe("Use the legacy chams rendering.")

    val behindColorModeValue by choices("Behind-Color", arrayOf("Same", "Opposite", "Custom"), "Same") { !legacyMode }
        .describe("Color used for parts hidden behind walls.")
    val color by color("Color", Color(0, 0, 120)) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
        .describe("Main chams color.")
    val behindColor by color("Behind", Color(255, 0, 0)) { !legacyMode && (colorModeValue== "Custom" || colorModeValue== "Fade") && behindColorModeValue == "Custom" }
        .describe("Custom color for parts behind walls.")

    private val applyGroup = Configurable("Apply")
    private val renderGroup = Configurable("Render")

    init {
        moveValues(applyGroup, "Targets", "Chests", "Items", "Hand", "LocalPlayer")
        moveValues(renderGroup,
            "Color", "Textured", "Legacy-Mode", "Behind-Color", "Behind")

        addValues(listOf(applyGroup, renderGroup))
    }
    fun preHandRender() {
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glDisable(2896)
        Color(255, 255, 255, 55)
    }

    fun postHandRender() {
        GL11.glEnable(2896)
        GL11.glEnable(3553)
        GL11.glDisable(3042)
    }

    fun shouldRenderHand(): Boolean {
        return handValue
    }
}
