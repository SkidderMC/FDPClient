/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl.*
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushMatrix
import org.lwjgl.opengl.GL11.glTranslated
import java.awt.Color

/**
 * A Target HUD
 */
@ElementInfo(name = "Targets")
class Targets : Element("Target", -46.0, -40.0, 1F, Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)) {

    private data class TargetData(val target: EntityLivingBase, var lastHitTime: Long)

    private val targetStyles = mutableListOf<TargetStyle>()

    private val multiTarget by boolean("Multi Target", true)
    private val maxTargets by int("Max Targets", 4, 1..50) { multiTarget }
    private val padding by int("Padding", 3, 0..20) { multiTarget }

    private val freezeTargets by boolean("Freeze Targets", false) { multiTarget }

    private val onlyPlayer by boolean("Only player", false)
    private val showInChat by boolean("Show When Chat", true)

    val styleValue by choices("Style", initStyles(), "Classic")

    private val shadowValue by boolean("Shadow", false)
    private val backgroundMode by boolean("Background-Color", true)
    private val backgroundColor by color("Background", Color(0, 0, 0, 120)) { backgroundMode }

    private val colorModeValue by choices("Color", arrayOf("Health", "Client"), "Client")

    private val fadeValue by boolean("Fade", false)

    private val animation by boolean("Animation", false)
    private val animationSpeed by float("Animation Speed", 1F, 0.1F..2F) { fadeValue || animation }

    private val resetBar by boolean("ResetBarWhenHiding", false)

    val globalAnimSpeed by float("Health Speed", 3F, 0.1F..5F)

    private val mainTargets = mutableListOf<TargetData>()
    private var animProgress = 0F
    var bgColor = Color(-1)
    var barColor = Color(-1)

    val combinedValues: Set<Value<*>>
        get() = super.values.toSet() + targetStyles.flatMap { it.values }.toSet()

    private fun initStyles(): Array<String> {
        return addStyles(
            CrossSineTH(this),
            ExhibitionTH(this),
            FDPClassicTH(this),
            FDPTH(this),
            FluxTH(this),
            LiquidBounceLegacyTH(this),
            ChillTH(this),
            RemixTH(this),
            SlowlyTH(this),
            J3UltimateTH(this),
            ModernTH(this)
        ).toTypedArray()
    }

    private fun addStyles(vararg styles: TargetStyle): List<String> {
        return styles.map {
            targetStyles.add(it)
            it.name
        }
    }

    private fun getCurrentStyle(styleName: String): TargetStyle? {
        return targetStyles.find { it.name.equals(styleName, ignoreCase = true) }
    }

    override fun drawElement(): Border? {
        assumeNonVolatile = true
        val currentStyle = getCurrentStyle(styleValue) ?: return null

        val newTargets: List<EntityLivingBase> = when {
            CombatManager.target != null && (!onlyPlayer || CombatManager.target is EntityPlayer) ->
                listOfNotNull(CombatManager.target)
            (mc.currentScreen is GuiChat && showInChat) || mc.currentScreen is GuiHudDesigner ->
                listOfNotNull(mc.thePlayer)
            else -> emptyList()
        }

        if (newTargets.isNotEmpty()) {
            if (!multiTarget) {
                mainTargets.clear()
                mainTargets.add(TargetData(newTargets[0], System.currentTimeMillis()))
            } else {
                if (freezeTargets) {
                    newTargets.forEach { target ->
                        val existing = mainTargets.find { it.target === target }
                        if (existing != null) {
                            existing.lastHitTime = System.currentTimeMillis()
                        } else if (mainTargets.size < maxTargets) {
                            mainTargets.add(TargetData(target, System.currentTimeMillis()))
                        }
                    }
                } else {
                    newTargets.forEach { target ->
                        val existing = mainTargets.find { it.target === target }
                        if (existing != null) {
                            existing.lastHitTime = System.currentTimeMillis()
                        } else {
                            mainTargets.add(0, TargetData(target, System.currentTimeMillis()))
                            if (mainTargets.size > maxTargets) {
                                mainTargets.removeAt(mainTargets.size - 1)
                            }
                        }
                    }
                }
            }
        }

        mainTargets.removeIf { System.currentTimeMillis() - it.lastHitTime >= 6000 }

        if (mainTargets.isEmpty()) {
            if (resetBar) currentStyle.easingHealth = 0F
            assumeNonVolatile = false
            return currentStyle.getBorder(null)
        }

        updateAnimationProgress(showTarget = true)

        val firstTarget = mainTargets.firstOrNull()?.target
        val preBarColor = getBarColor(firstTarget)
        val preBgColor = backgroundColor

        barColor = Color(
            preBarColor.red,
            preBarColor.green,
            preBarColor.blue,
            (preBarColor.alpha / 255F * (1F - animProgress) * 255).toInt()
        )
        bgColor = Color(
            preBgColor.red,
            preBgColor.green,
            preBgColor.blue,
            (preBgColor.alpha / 255F * (1F - animProgress) * 255).toInt()
        )

        val returnBorder = currentStyle.getBorder(firstTarget) ?: return null
        val borderWidth = returnBorder.x2 - returnBorder.x
        val borderHeight = returnBorder.y2 - returnBorder.y

        val calcScaleX = animProgress * (4F / (borderWidth / 2F))
        val calcScaleY = animProgress * (4F / (borderHeight / 2F))
        val calcTranslateX = borderWidth / 2F * calcScaleX
        val calcTranslateY = borderHeight / 2F * calcScaleY

        if (shadowValue && currentStyle.shaderSupport) {
            drawShadow(renderX, renderY)
        }

        if (currentStyle is ChillTH) {
            currentStyle.updateData(
                renderX.toFloat() + calcTranslateX,
                renderY.toFloat() + calcTranslateY,
                calcScaleX,
                calcScaleY
            )
        }

        if (multiTarget) {
            val columns = 2
            for (i in mainTargets.indices) {
                val data = mainTargets[i]
                val col = i % columns
                val row = i / columns
                glPushMatrix()
                glTranslated((col * (borderWidth + padding)).toDouble(), (row * (borderHeight + padding)).toDouble(), 0.0)
                currentStyle.drawTarget(data.target)
                glPopMatrix()
            }
        } else {
            var offsetY = 0F
            for (data in mainTargets) {
                glPushMatrix()
                glTranslated(0.0, offsetY.toDouble(), 0.0)
                currentStyle.drawTarget(data.target)
                glPopMatrix()
                offsetY += borderHeight
            }
        }

        GlStateManager.resetColor()
        assumeNonVolatile = false
        return returnBorder
    }

    private fun drawShadow(renderX: Double, renderY: Double) {
        glTranslated(-renderX, -renderY, 0.0)
        glPushMatrix()
        glPopMatrix()
        glTranslated(renderX, renderY, 0.0)
    }

    private fun updateAnimationProgress(showTarget: Boolean) {
        if (fadeValue) {
            animProgress += (0.0075F * animationSpeed * deltaTime * if (showTarget) -1F else 1F)
        } else {
            animProgress = 0F
        }
        animProgress = animProgress.coerceIn(0F, 1F)
    }

    private fun getBarColor(target: EntityLivingBase?): Color {
        return when (colorModeValue) {
            "Health" -> if (target != null) ColorUtils.getHealthColor(target.health, target.maxHealth) else Color.green
            "Client" -> getColor(1)
            else -> getColor(1)
        }
    }

    fun getFadeProgress() = animProgress

    private fun onHit(entity: EntityLivingBase) {
        mainTargets.find { it.target == entity }?.let { it.lastHitTime = System.currentTimeMillis() }
    }

    init {
        CombatManager.onHitEntityListeners.add { entity ->
            onHit(entity)
        }
    }
}