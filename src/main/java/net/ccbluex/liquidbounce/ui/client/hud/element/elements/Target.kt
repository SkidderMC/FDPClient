/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.config.*
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
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * A Target HUD
 */
@ElementInfo(name = "Targets")
class Targets : Element(-46.0, -40.0, 1F, Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)) {
    private val styleList = mutableListOf<TargetStyle>()

    val styleValue: ListValue
    private val onlyPlayer by boolean("Only player", false)
    private val showinchat by boolean("Show When Chat", true)
    private val resetBar by boolean("ResetBarWhenHiding", false)

    private val fadeValue by boolean("Fade", false)

    private val animationValue by boolean("Animation", false)
    private val animationSpeed by float("Animation Speed", 1F, 0.1F.. 2F) { fadeValue || animationValue }
    val globalAnimSpeed by float("Health Speed", 3F, 0.1F..5F)

    private val colorModeValue by choices("Color", arrayOf("Health", "Client"), "Client")

    private val shadowValue by boolean("Shadow", false)

    private val bgRedValue by int("Background-Red", 0, 0.. 255)
    private val bgGreenValue by int("Background-Green", 0, 0..255)
    private val bgBlueValue by int("Background-Blue", 0, 0.. 255)
    private val bgAlphaValue by int("Background-Alpha", 120, 0.. 255)

    var target = CombatManager.target
    override val values: Set<Value<*>>
        get() {
            val valueSet = mutableSetOf<Value<*>>()
            styleList.forEach { valueSet.addAll(it.values) }
            return super.values + valueSet
        }
    init {
        styleValue = choices("Style", addStyles(
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
        ).toTypedArray(), "Classic")
    }
    private var mainTarget: EntityLivingBase? = null
    private var animProgress = 0F

    var bgColor = Color(-1)
    var barColor = Color(-1)

    override fun drawElement(): Border? {

        assumeNonVolatile = true

        val mainStyle = getCurrentStyle(styleValue.get()) ?: return null
        val actualTarget = if (target != null && (!onlyPlayer || target is EntityPlayer)) target
        else if (target != null && (!onlyPlayer || target is EntityPlayer)) target
        else if ((mc.currentScreen is GuiChat && showinchat) || mc.currentScreen is GuiHudDesigner) mc.thePlayer
        else null
        if (fadeValue) {
            animProgress += (0.0075F * animationSpeed * deltaTime * if (actualTarget != null) -1F else 1F)
        } else {
            animProgress = 0F
        }
        animProgress = animProgress.coerceIn(0F, 1F)

        val preBarColor = when (colorModeValue) {
            "Health" -> if (actualTarget != null) ColorUtils.getHealthColor(
                actualTarget.health,
                actualTarget.maxHealth
            ) else Color.green

            "Client" -> getColor(1)
            else -> getColor(1)
        }

        val preBgColor = Color(bgRedValue, bgGreenValue, bgBlueValue, bgAlphaValue)

        barColor = ColorUtils.targetReAlpha(preBarColor, preBarColor.alpha / 255F * (1F - animProgress))
        bgColor = ColorUtils.targetReAlpha(preBgColor, preBgColor.alpha / 255F * (1F - animProgress))

        if (actualTarget != null || !fadeValue)
            mainTarget = actualTarget
        else if (animProgress >= 1F)
            mainTarget = null

        val returnBorder = mainStyle.getBorder(mainTarget) ?: return null
        val borderWidth = returnBorder.x2 - returnBorder.x
        val borderHeight = returnBorder.y2 - returnBorder.y

        if (mainTarget == null) {
            if (resetBar)
                mainStyle.easingHealth = 0F
            return returnBorder
        }
        val convertTarget = mainTarget!!

        val calcScaleX = animProgress * (4F / (borderWidth / 2F))
        val calcScaleY = animProgress * (4F / (borderHeight / 2F))
        val calcTranslateX = borderWidth / 2F * calcScaleX
        val calcTranslateY = borderHeight / 2F * calcScaleY

        if (shadowValue && mainStyle.shaderSupport) {
            glTranslated(-renderX, -renderY, 0.0)
            glPushMatrix()

            glPopMatrix()
            glTranslated(renderX, renderY, 0.0)
        }

        if (mainStyle is ChillTH)
            mainStyle.updateData(renderX.toFloat() + calcTranslateX, renderY.toFloat() + calcTranslateY, calcScaleX, calcScaleY)
        mainStyle.drawTarget(convertTarget)

        GlStateManager.resetColor()

        assumeNonVolatile = false

        return returnBorder
    }

    fun getFadeProgress() = animProgress

    @SafeVarargs
    fun addStyles(vararg styles: TargetStyle): List<String> {
        val nameList = mutableListOf<String>()
        styles.forEach {
            styleList.add(it)
            nameList.add(it.name)
        }
        return nameList
    }

    private fun getCurrentStyle(styleName: String): TargetStyle? = styleList.find { it.name.equals(styleName, true) }
}
