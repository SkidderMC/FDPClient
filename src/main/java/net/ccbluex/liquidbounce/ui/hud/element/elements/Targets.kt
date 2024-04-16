/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.animations.impl.EaseBackIn
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.Element
import net.ccbluex.liquidbounce.ui.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.hud.element.Side
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.impl.*
import net.ccbluex.liquidbounce.utils.render.*
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
    val resetBar = BoolValue("ResetBarWhenHiding", false)

    val fadeValue = BoolValue("Fade", false)
    val fadeSpeed = FloatValue("Fade-Speed", 1F, 0F, 5F).displayable { fadeValue.get() }
    val animation = EaseBackIn(350 * this.fadeSpeed.get().toInt(),1.0,2f)

    val animationValue = BoolValue("Animation", false)
    val animationSpeed = FloatValue("Animation Speed", 1F, 0.1F, 2F).displayable { fadeValue.get() || animationValue.get() }
    val globalAnimSpeed = FloatValue("Health Speed", 3F, 0.1F, 5F)

    val colorModeValue = ListValue("Color", arrayOf("Health", "Client"), "Client")

    val shadowValue = BoolValue("Shadow", false)
    val shadowStrength = FloatValue("Shadow-Strength", 1F, 0.01F, 40F).displayable { shadowValue.get() }


    val applyBlurValue = BoolValue("Blur", false)
    val blurStrength = FloatValue("Blur-Strength", 1F, 0.01F, 40F).displayable { applyBlurValue.get() }

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
            NormalTH(this),
            SimpleTH(this),
            CrossSineTH(this),
            ExhibitionTH(this),
            AstolfoTH(this),
            FDPTH(this),
            VapeTH(this),
            FluxTH(this),
            LiquidTH(this),
            ChillTH(this),
            RemixTH(this),
            SlowlyTH(this),
            SimplicityTH(this),
            J3UltimateTH(this),
        ).toTypedArray(), "Simple")
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

        if (actualTarget != null || !fadeValue.get())
            mainTarget = actualTarget
        else if (animProgress >= 1F)
            mainTarget = null

        val returnBorder = mainStyle.getBorder(mainTarget) ?: return null
        val borderWidth = returnBorder.x2 - returnBorder.x
        val borderHeight = returnBorder.y2 - returnBorder.y

        if (mainTarget == null) {
            if (resetBar.get())
                mainStyle.easingHealth = 0F
            return returnBorder
        }
        val convertTarget = mainTarget!!

        val calcScaleX = animProgress * (4F / (borderWidth / 2F))
        val calcScaleY = animProgress * (4F / (borderHeight / 2F))
        val calcTranslateX = borderWidth / 2F * calcScaleX
        val calcTranslateY = borderHeight / 2F * calcScaleY

        val idkWidth = returnBorder.x2
        val idkHeight = returnBorder.y2

        if (shadowValue.get() && mainStyle.shaderSupport) {
            val floatX = renderX.toFloat()
            val floatY = renderY.toFloat()

            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()

            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
                }
                mainStyle.handleShadow(convertTarget)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
                }
                mainStyle.handleShadowCut(convertTarget)
                GL11.glPopMatrix()
            })

            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        if (applyBlurValue.get() && mainStyle.shaderSupport) {
            val floatX = renderX.toFloat()
            val floatY = renderY.toFloat()

            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            BlurUtils.blur(floatX + returnBorder.x, floatY + returnBorder.y, floatX + returnBorder.x2, floatY + returnBorder.y2, blurStrength.get() * (1F - animProgress), false) {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
                }
                mainStyle.handleBlur(convertTarget)
                GL11.glPopMatrix()
            }
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        if (fadeValue.get()) {
            RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
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
