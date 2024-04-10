/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.Element
import net.ccbluex.liquidbounce.ui.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.hud.element.Side
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.impl.*
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "Targets")
class Targets : Element(-46.0, -40.0, 1F, Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)){
    private val styleList = mutableListOf<TargetStyle>()

    val styleValue: ListValue
    val onlyPlayer = BoolValue("Only player", false)
    val showinchat = BoolValue("Show When Chat", true)
    val fadeValue = BoolValue("Fade", false)
    val fadeSpeed = FloatValue("Fade-Speed", 1F, 0F, 5F).displayable { fadeValue.get() }
    val animationValue = BoolValue("Animation", false)
    val animationSpeed = FloatValue("Animation Speed", 1F, 0.1F, 2F).displayable { fadeValue.get() || animationValue.get() }
    val globalAnimSpeed = FloatValue("Health Speed", 3F, 0.1F, 5F)

    val colorModeValue = ListValue("Color", arrayOf("Health", "Client"), "Client")

    val bgRedValue = IntegerValue("Background-Red", 0, 0, 255)
    val bgGreenValue = IntegerValue("Background-Green", 0, 0, 255)
    val bgBlueValue = IntegerValue("Background-Blue", 0, 0, 255)
    val bgAlphaValue = IntegerValue("Background-Alpha", 120, 0, 255)

    var target = FDPClient.combatManager.target
    override val values: List<Value<*>>
        get() {
            val valueList = mutableListOf<Value<*>>()
            styleList.forEach { valueList.addAll(it.values) }
            return super.values.toMutableList() + valueList
        }
    init {
        styleValue = ListValue("Style", addStyles(
            RavenB4TH(this),
            Tenacity5TH(this),
            NormalTH(this),
            SimpleTH(this),
            CrossSineTH(this),
            FDPTH(this),
            VapeTH(this),
            FluxTH(this),
            LiquidTH(this),
            AstolfoTH(this),
            ExhibitionTH(this),
            ChillTH(this),
            J3UltimateTH(this),
            RemixTH(this),
            SlowlyTH(this),
            SimplicityTH(this),
            RiseTH(this),
        ).toTypedArray(), "Rise")
    }
    var mainTarget: EntityLivingBase? = null
    var animProgress = 0F

    var bgColor = Color(-1)
    var barColor = Color(-1)

    override fun drawElement(partialTicks: Float): Border? {
        val mainStyle = getCurrentStyle(styleValue.get()) ?: return null
        val actualTarget = if (FDPClient.combatManager.target != null && (!onlyPlayer.get() || FDPClient.combatManager.target is EntityPlayer)) FDPClient.combatManager.target
        else if (FDPClient.combatManager.target != null && (!onlyPlayer.get() || FDPClient.combatManager.target is EntityPlayer)) FDPClient.combatManager.target
        else if ((mc.currentScreen is GuiChat && showinchat.get()) || mc.currentScreen is GuiHudDesigner) mc.thePlayer
        else null
        if (fadeValue.get()) {
            animProgress += (0.0075F * animationSpeed.get() * RenderUtils.deltaTime * if (actualTarget != null) -1F else 1F)
        } else {
            animProgress = 0F
        }
        animProgress = animProgress.coerceIn(0F, 1F)

        val preBarColor = when (colorModeValue.get()) {
            "Health" -> if (actualTarget != null) BlendUtils.getHealthColor(
                actualTarget.health,
                actualTarget.maxHealth
            ) else Color.green

            "Client" -> ClientTheme.getColor(1)
            else -> ClientTheme.getColor(1)
        }

        val preBgColor = Color(bgRedValue.get(), bgGreenValue.get(), bgBlueValue.get(), bgAlphaValue.get())

        barColor = ColorUtils.reAlpha(preBarColor, preBarColor.alpha / 255F * (1F - animProgress))
        bgColor = ColorUtils.reAlpha(preBgColor, preBgColor.alpha / 255F * (1F - animProgress))

        if (actualTarget != null || !fadeValue.get()) {
            mainTarget = actualTarget
        }
        else if (animProgress >= 1F)
            mainTarget = null

        val returnBorder = mainStyle.getBorder(mainTarget) ?: return null
        animProgress * (4F / ((returnBorder.x2 - returnBorder.x) / 2F))
        val scaleY = animProgress * (4F / ((returnBorder.y2 - returnBorder.y) / 2F))
        val tranY = (returnBorder.y2 - returnBorder.y) / 2F * scaleY
        if (mainTarget == null) {
            mainStyle.easingHealth = 0F
            return returnBorder
        }
        val convertTarget = mainTarget!!
        if (animationValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslatef(tranY, tranY, tranY)
            GL11.glScalef(1F - scaleY, 1F - scaleY, 1F - scaleY)
        }
        mainStyle.drawTarget(convertTarget)
        if (animationValue.get()) {
            GL11.glPopMatrix()
        }
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
