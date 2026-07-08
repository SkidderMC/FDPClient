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
import org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS
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
    private val handColor by color("HandColor", Color(255, 255, 255, 90)) { handValue }
        .describe("Color and opacity of first-person hand chams.")

    val localPlayerValue by boolean("LocalPlayer", true)
        .describe("Apply chams to your own player.")
    val colorModeValue by choices("ColorMode", arrayOf("Custom", "Fade"), "Custom") { !legacyMode }
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
        moveValues(applyGroup, "Targets", "Chests", "Items", "Hand", "HandColor", "LocalPlayer")
        moveValues(renderGroup,
            "ColorMode", "Color", "Textured", "Legacy-Mode", "Behind-Color", "Behind")

        addValues(listOf(applyGroup, renderGroup))
    }
    fun preHandRender() {
        GL11.glPushAttrib(GL_ALL_ATTRIB_BITS)
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(2896)
        GL11.glColor4f(
            handColor.red / 255f,
            handColor.green / 255f,
            handColor.blue / 255f,
            handColor.alpha / 255f,
        )
    }

    fun postHandRender() {
        GL11.glPopAttrib()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    fun shouldRenderHand(): Boolean {
        return handleEvents() && handValue
    }
}
