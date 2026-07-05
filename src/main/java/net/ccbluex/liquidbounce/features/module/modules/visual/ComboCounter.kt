/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

object ComboCounter : Module("ComboCounter", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val position by choices("Position", arrayOf("CenterBelow", "TopLeft", "TopRight"), "CenterBelow")
        .describe("Where the combo counter is drawn.")
    private val resetTime by int("ResetTime", 3000, 500..10000, "ms")
        .describe("Time without a hit before the combo resets.")
    private val rainbow by boolean("Rainbow", false)
        .describe("Cycle the counter color over time.")
    private val staticColor by color("Color", Color(90, 255, 120)) { !rainbow }
        .describe("Static color of the counter.")

    private var combo = 0
    private var lastTarget: Entity? = null
    private val resetTimer = MSTimer()

    override fun onDisable() {
        combo = 0
        lastTarget = null
    }

    val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity
        if (target is EntityLivingBase) {
            if (target !== lastTarget) combo = 0
            combo++
            lastTarget = target
            resetTimer.reset()
        }
    }

    val onRender2D = handler<Render2DEvent> {
        if (resetTimer.hasTimePassed(resetTime.toLong())) combo = 0
        if (combo <= 0) return@handler

        val sr = ScaledResolution(mc)
        val font = mc.fontRendererObj
        val text = "${combo}x"
        val width = font.getStringWidth(text)

        val rgb = if (rainbow)
            Color.HSBtoRGB((System.currentTimeMillis() % 2000L) / 2000f, 0.7f, 1f)
        else staticColor.rgb

        val x: Float
        val y: Float
        when (position) {
            "TopLeft" -> {
                x = 4f
                y = 4f
            }
            "TopRight" -> {
                x = (sr.scaledWidth - width - 4).toFloat()
                y = 4f
            }
            else -> {
                x = sr.scaledWidth / 2f - width / 2f
                y = sr.scaledHeight / 2f + 12f
            }
        }

        font.drawStringWithShadow(text, x, y, rgb)
    }

    override val tag get() = "${combo}x"
}
