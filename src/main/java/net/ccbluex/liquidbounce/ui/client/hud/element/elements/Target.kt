/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl.*
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.value.*
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
    private val onlyPlayer by BoolValue("Only player", false)
    private val showinchat by BoolValue("Show When Chat", true)
    private val resetBar by BoolValue("ResetBarWhenHiding", false)

    private val fadeValue by BoolValue("Fade", false)

    private val animationValue by BoolValue("Animation", false)
    private val animationSpeed by FloatValue("Animation Speed", 1F, 0.1F.. 2F) { fadeValue || animationValue }
    val globalAnimSpeed by FloatValue("Health Speed", 3F, 0.1F..5F)

    private val colorModeValue by ListValue("Color", arrayOf("Health", "Client"), "Client")

    private val shadowValue by BoolValue("Shadow", false)

    private val bgRedValue by IntegerValue("Background-Red", 0, 0.. 255)
    private val bgGreenValue by IntegerValue("Background-Green", 0, 0..255)
    private val bgBlueValue by IntegerValue("Background-Blue", 0, 0.. 255)
    private val bgAlphaValue by IntegerValue("Background-Alpha", 120, 0.. 255)

    var target = FDPClient.combatManager.target
    override val values: List<Value<*>>
        get() {
            val valueList = mutableListOf<Value<*>>()
            styleList.forEach { valueList.addAll(it.values) }
            return super.values.toMutableList() + valueList
        }
    init {
        styleValue = ListValue("Style", addStyles(
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
        val mainStyle = getCurrentStyle(styleValue.get()) ?: return null
        val actualTarget = if (FDPClient.combatManager.target != null && (!onlyPlayer || FDPClient.combatManager.target is EntityPlayer)) FDPClient.combatManager.target
        else if (FDPClient.combatManager.target != null && (!onlyPlayer || FDPClient.combatManager.target is EntityPlayer)) FDPClient.combatManager.target
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
