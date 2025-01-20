/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.config.ListValue
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

    private val targetStyles = mutableListOf<TargetStyle>()

    val styleValue: ListValue
    private val onlyPlayer by boolean("Only player", false)
    private val showInChat by boolean("Show When Chat", true)
    private val resetBar by boolean("ResetBarWhenHiding", false)
    private val fadeValue by boolean("Fade", false)
    private val animationValue by boolean("Animation", false)
    private val animationSpeed by float("Animation Speed", 1F, 0.1F..2F) { fadeValue || animationValue }
    val globalAnimSpeed by float("Health Speed", 3F, 0.1F..5F)
    private val colorModeValue by choices("Color", arrayOf("Health", "Client"), "Client")
    private val shadowValue by boolean("Shadow", false)
    private val backgroundMode by choices("Background-Color", arrayOf("Custom", "Rainbow"), "Custom")
    private val backgroundColor by color("Background", Color.BLACK) { backgroundMode == "Custom" }

    private var mainTarget: EntityLivingBase? = null
    private var animProgress = 0F
    var bgColor = Color(-1)
    var barColor = Color(-1)

    val combinedValues: Set<Value<*>>
        get() = super.values.toSet() + targetStyles.flatMap { it.values }.toSet()

    init {
        styleValue = choices("Style", initStyles(), "Classic")
    }

    private fun initStyles(): Array<String> {
        return addStyles(
            NormalTH(this),
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

    private fun getCurrentStyle(styleName: String): TargetStyle? = targetStyles.find { it.name.equals(styleName, true) }

    override fun drawElement(): Border? {
        assumeNonVolatile = true

        val currentStyle = getCurrentStyle(styleValue.get()) ?: return null

        val actualTarget = when {
            CombatManager.target != null && (!onlyPlayer || CombatManager.target is EntityPlayer) -> CombatManager.target
            (mc.currentScreen is GuiChat && showInChat) || mc.currentScreen is GuiHudDesigner -> mc.thePlayer
            else -> null
        }

        updateAnimationProgress(actualTarget != null)

        val preBarColor = getBarColor(actualTarget)
        val preBgColor = Color(backgroundColor.rgb)

        barColor = ColorUtils.targetReAlpha(preBarColor, preBarColor.alpha / 255F * (1F - animProgress))
        bgColor = ColorUtils.targetReAlpha(preBgColor, preBgColor.alpha / 255F * (1F - animProgress))

        mainTarget = if (actualTarget != null || !fadeValue) actualTarget else if (animProgress >= 1F) null else mainTarget


        val returnBorder = currentStyle.getBorder(mainTarget) ?: return null
        val borderWidth = returnBorder.x2 - returnBorder.x
        val borderHeight = returnBorder.y2 - returnBorder.y

        if (mainTarget == null) {
            if (resetBar)
                currentStyle.easingHealth = 0F
            return returnBorder
        }

        val calcScaleX = animProgress * (4F / (borderWidth / 2F))
        val calcScaleY = animProgress * (4F / (borderHeight / 2F))
        val calcTranslateX = borderWidth / 2F * calcScaleX
        val calcTranslateY = borderHeight / 2F * calcScaleY

        if (shadowValue && currentStyle.shaderSupport) {
            drawShadow(renderX, renderY)
        }

        if (currentStyle is ChillTH) {
            currentStyle.updateData(renderX.toFloat() + calcTranslateX, renderY.toFloat() + calcTranslateY, calcScaleX, calcScaleY)
        }

        mainTarget?.let { currentStyle.drawTarget(it) }

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
}