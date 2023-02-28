/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

import org.lwjgl.opengl.GL11

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", blur = true)
class Arraylist(
    x: Double = 1.0,
    y: Double = 2.0,
    scale: Float = 1F,
    side: Side = Side(Horizontal.RIGHT, Vertical.UP)
) : Element(x, y, scale, side) {

    private val colorModeValue = ListValue("Text-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "Slowly", "SkyRainbow", "Static"), "Slowly")
    private val colorRedValue = IntegerValue("Text-R", 255, 255, 255)
    private val colorGreenValue = IntegerValue("Text-G", 255, 255, 255)
    private val colorBlueValue = IntegerValue("Text-B", 255, 255, 255)
    private val tagColorModeValue = ListValue("Tag-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "Slowly", "SkyRainbow", "Static"), "Custom")
    private val tagColorRedValue = IntegerValue("Tag-R", 195, 0, 255)
    private val tagColorGreenValue = IntegerValue("Tag-G", 195, 0, 255)
    private val tagColorBlueValue = IntegerValue("Tag-B", 195, 0, 255)
    private val speed = IntegerValue("AllSpeed", 0, 0, 400)
    private val rectColorModeValue = ListValue("Rect-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "Slowly", "SkyRainbow", "Static"), "AnotherRainbow")
    private val rectColorRedValue = IntegerValue("Rect-R", 255, 0, 255)
    private val rectColorGreenValue = IntegerValue("Rect-G", 255, 0, 255)
    private val rectColorBlueValue = IntegerValue("Rect-B", 255, 0, 255)
    private val rectColorBlueAlpha = IntegerValue("Rect-Alpha", 255, 0, 255)
    private val saturationValue = FloatValue("Random-Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Random-Brightness", 1f, 0f, 1f)
    private val tagsValue = ListValue("TagsStyle", arrayOf("-", "|", "()", "[]", "<>", "Space", "None"), "Space")
    private val shadow = BoolValue("ShadowText", true)
    private val split = BoolValue("SplitName", false)
    private val slideInAnimation = BoolValue("SlideInAnimation", true)
    private val noRenderModules = BoolValue("NoRenderModules", false)
    private val backgroundColorModeValue = ListValue("Background-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "Slowly", "SkyRainbow", "Static"), "Custom")
    private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
    private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
    private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 0, 0, 255)
    private val backgroundExpand = IntegerValue("Background-Expand", 2, 0, 10)
    private val rainbowSpeed = IntegerValue("RainbowSpeed", 1, 1, 10)
    private val rectValue = ListValue("Rect", arrayOf("None", "Left", "Right", "Outline", "Special", "Top"), "None")
    private val caseValue = ListValue("Case", arrayOf("Upper", "Normal", "Lower"), "Normal")
    private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    private val textYValue = FloatValue("TextY", 1F, 0F, 20F)
    private val fontValue = FontValue("Font", Fonts.font35)
    private val fontAlphaValue = IntegerValue("TextAlpha", 255, 0, 255)
    private val shadowShaderValue = BoolValue("Shadow", false)
    private val shadowNoCutValue = BoolValue("Shadow-NoCut", false)
    private val shadowStrength = IntegerValue("Shadow-Strength", 1, 1, 30)
    private val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Text", "Custom"), "Background")
    private val shadowColorRedValue = IntegerValue("Shadow-Red", 0, 0, 255)
    private val shadowColorGreenValue = IntegerValue("Shadow-Green", 111, 0, 255)
    private val shadowColorBlueValue = IntegerValue("Shadow-Blue", 255, 0, 255)
    private val cRainbowSecValue = IntegerValue("CRainbow-Seconds", 2, 1, 10)
    private val cRainbowDistValue = IntegerValue("CRainbow-Distance", 2, 1, 6)
    private var x2 = 0
    private var y2 = 0F
    val counter = intArrayOf(0)

    private var modules = emptyList<Module>()

    val delay = intArrayOf(0)

    private fun shouldExpect(module: Module): Boolean {
        return noRenderModules.get() && module.category == ModuleCategory.RENDER
    }

    private fun changeCase(inStr: String): String {
        val str = LanguageManager.replace(inStr)
        return when (caseValue.get().lowercase()) {
            "upper" -> str.uppercase()
            "lower" -> str.lowercase()
            else -> str
        }
    }

    private fun getModuleTag(module: Module): String {
        module.tag ?: return ""
        return when (tagsValue.get().lowercase()) {
            "-" -> " - ${module.tag}"
            "|" -> "|${module.tag}"
            "()" -> " (${module.tag})"
            "[]" -> " [${module.tag}]"
            "<>" -> " <${module.tag}>"
            "space" -> " ${module.tag}"
            else -> ""
        }
    }

    private fun getModuleName(module: Module) = if (split.get()) { module.splicedName } else { module.localizedName }

    override fun drawElement(partialTicks: Float): Border? {
        val fontRenderer = fontValue.get()

        for (module in LiquidBounce.moduleManager.modules) {
            if (!module.array || shouldExpect(module) || (!module.state && module.slide == 0F && (module.yPosAnimation == null || module.yPosAnimation!!.state == Animation.EnumAnimationState.STOPPED))) continue

            module.width = fontRenderer.getStringWidth(changeCase(getModuleName(module) + getModuleTag(module)))

            val targetSlide = if (module.state) { module.width.toFloat() } else { 0f }
            if (module.slide != targetSlide) {
                module.slide = targetSlide
            }
        }

        // Draw arraylist
        val colorMode = colorModeValue.get()
        val rectColorMode = rectColorModeValue.get()
        val backgroundColorMode = backgroundColorModeValue.get()
        val customColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), fontAlphaValue.get())
        val tagCustomColor = Color(tagColorRedValue.get(), tagColorGreenValue.get(), tagColorBlueValue.get(), fontAlphaValue.get())
        val rectCustomColor = Color(rectColorRedValue.get(), rectColorGreenValue.get(), rectColorBlueValue.get(), rectColorBlueAlpha.get())
        val space = spaceValue.get()
        val textHeight = textHeightValue.get()
        val textY = textYValue.get()
        val rectMode = rectValue.get()
        val backgroundCustomColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get(), backgroundColorAlphaValue.get())
        val textShadow = shadow.get()
        val textSpacer = textHeight + space
        val saturation = saturationValue.get()
        val brightness = brightnessValue.get()
        when (side.horizontal) {
            Horizontal.RIGHT, Horizontal.MIDDLE -> {
               modules.forEachIndexed { index, module ->
                    val xPos = -module.slide - 2
                    val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -textHeight } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *if (side.vertical == Vertical.DOWN) index + 1 else index }
                    val yPos = module.yPos
                    if (yPos != realYPos) { module.yPos = realYPos }
                    val rectX = xPos - if (rectMode.equals("right", true)) 5 else 2
                    blur(rectX - backgroundExpand.get(), yPos, if (rectMode.equals("right", true)) -3F else 0F, yPos + textHeight)
                }
                    if (shadowShaderValue.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    ShadowUtils.shadow(shadowStrength.get().toFloat(), {
                        GL11.glPushMatrix()
                        GL11.glTranslated(renderX, renderY, 0.0)
                        modules.forEachIndexed { index, module ->
                            val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -textHeight } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) index + 1 else index }
                            val yPos = module.yPos
                            if (yPos != realYPos) { module.yPos = realYPos }
                            var arrayY = yPos.toDouble()
                            val xPos = -module.slide - 2
                            RenderUtils.newDrawRect(
                                    xPos.toDouble() - if (rectValue.get().equals("right", true)) 3 else 2,
                                    arrayY,
                                    if (rectValue.get().equals("right")) -1.toDouble() else 0.toDouble(),
                                    arrayY + textHeight, when (shadowColorMode.get()){
                                        "Background" -> Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get()).rgb
                                        "Text" -> {
                                            when {
                                                colorModeValue.equals("Random") -> Color.getHSBColor(module.hue, saturation, brightness).rgb
                                                colorModeValue.equals("Rainbow") -> ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()).rgb
                                                colorModeValue.equals("SkyRainbow") -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                                            //    colorModeValue.equals("Astolfo") -> RenderUtils.Astolfo(index * speed.get(), saturationValue.get(), brightnessValue.get())
                                                colorModeValue.equals("Static") -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1).rgb
                                                colorModeValue.equals("Slowly") -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get()).rgb
                                                colorModeValue.equals("AnotherRainbow") -> ColorUtils.fade(customColor, 100, index + 1).rgb
                                                else -> customColor.rgb
                                            }
                                        }
                                        else -> Color(shadowColorRedValue.get(), shadowColorGreenValue.get(), shadowColorBlueValue.get()).rgb
                                    }
                            )
                        }
                        GL11.glPopMatrix()
                        counter[0] = 0
                    }, {
                        if (!shadowNoCutValue.get()) {
                            GL11.glPushMatrix()
                            GL11.glTranslated(renderX, renderY, 0.0)
                            modules.forEachIndexed { index, module ->
                                val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -textHeight } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                                if (side.vertical == Vertical.DOWN) index + 1 else index }
                                val yPos = module.yPos
                                if (yPos != realYPos) { module.yPos = realYPos }
                                var arrayY = yPos
                                val xPos = -module.slide - 2
                                RenderUtils.quickDrawRect(xPos - if (rectValue.get().equals("right", true)) 3 else 2,arrayY, if (rectValue.get().equals("right", true)) -1F else 0F,arrayY + textHeight)
                            }
                            GL11.glPopMatrix()
                        }
                    })
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }
                modules.forEachIndexed { index, module ->
                    var CRainbow: Int
                    CRainbow = RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter[0] * (50 * cRainbowDistValue.get()))
                    val xPos = -module.slide - 2
                    val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -textHeight } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) index + 1 else index }
                    val yPos = module.yPos
                    if (yPos != realYPos) { module.yPos = realYPos }

                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

                    val rectX = xPos - if (rectMode.equals("right", true)) 5 else 2
                    blur(rectX - backgroundExpand.get(), yPos, if (rectMode.equals("right", true)) -3F else 0F, yPos + textHeight)
                    RenderUtils.drawRect(
                        rectX - backgroundExpand.get(),
                        yPos,
                        if (rectMode.equals("right", true)) -3F else 0F,
                        yPos + textHeight,
                        when (backgroundColorMode.lowercase()) {
                            "rainbow" -> ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()).rgb
                            "random" -> moduleColor
                            "CRainbow" -> CRainbow
                            "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "Astolfo" -> RenderUtils.Astolfo(index * speed.get(), saturationValue.get(), brightnessValue.get())
                            "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(backgroundCustomColor, 100, index + 1).rgb
                            else -> backgroundCustomColor.rgb
                        }
                    )
                    val mName = changeCase(getModuleName(module))
                    val mTag = changeCase(getModuleTag(module))
                    fontRenderer.drawString(mName, xPos - if (rectMode.equals("right", true)) 3 else 0, yPos + textY,
                        when (colorMode.lowercase()) {
                            "rainbow" -> ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()).rgb
                            "random" -> moduleColor
                            "CRainbow" -> CRainbow
                            "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "Astolfo" -> RenderUtils.Astolfo(index * speed.get(), saturationValue.get(), brightnessValue.get())
                            "Static" -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1).rgb
                            "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(customColor, 100, index + 1).rgb
                            else -> customColor.rgb
                        }, textShadow)

                    fontRenderer.drawString(mTag, xPos - (if (rectMode.equals("right", true)) 3 else 0) + fontRenderer.getStringWidth(mName), yPos + textY,
                        ColorUtils.reverseColor(when (tagColorModeValue.get().lowercase()) {
                            "rainbow" -> ColorUtils.reverseColor(ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()))
                            "random" -> Color(moduleColor)
                            "crainbow" -> Color(RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter[0] * (50 * cRainbowDistValue.get())))
                            "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble())
                            "Static" -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1)
                            "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get())
                            "anotherrainbow" -> ColorUtils.reverseColor(ColorUtils.fade(tagCustomColor, 100, index + 1))
                            else -> ColorUtils.reverseColor(tagCustomColor)
                        }).rgb, textShadow)

                    if (!rectMode.equals("none", true)) {
                        val rectColor = when (rectColorMode.lowercase()) {
                            "rainbow" -> ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()).rgb
                            "random" -> moduleColor
                            "CRainbow" -> CRainbow
                            "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "Astolfo" -> RenderUtils.Astolfo(speed.get(), saturationValue.get(), brightnessValue.get())
                            "Static" -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1).rgb
                            "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(rectCustomColor, 100, index + 1).rgb
                            else -> rectCustomColor.rgb
                        }

                        when (rectMode.lowercase()) {
                            "left" -> RenderUtils.drawRect(xPos - 5, yPos, xPos - 2, yPos + textHeight,
                                rectColor)
                            "right" -> RenderUtils.drawRect(-3F, yPos, 0F,
                                yPos + textHeight, rectColor)
                            "outline" -> {
                                RenderUtils.drawRect(-1F, yPos - 1F, 0F,
                                    yPos + textHeight, rectColor)
                                RenderUtils.drawRect(xPos - 3, yPos, xPos - 2, yPos + textHeight,
                                    rectColor)
                                if (module != modules[0]) {
                                    RenderUtils.drawRect(xPos - 3 - (modules[index - 1].width - module.width), yPos, xPos - 2, yPos + 1,
                                        rectColor)
                                    if (module == modules[modules.size - 1]) {
                                        RenderUtils.drawRect(xPos - 3, yPos + textHeight, 0.0F, yPos + textHeight + 1,
                                            rectColor)
                                    }
                                }
                            }
                            "special" -> {
                                if (module == modules[0]) {
                                    RenderUtils.drawRect(xPos - 2, yPos, 0F, yPos - 1, rectColor)
                                }
                                if (module == modules[modules.size - 1]) {
                                    RenderUtils.drawRect(xPos - 2, yPos + textHeight, 0F, yPos + textHeight + 1, rectColor)
                                }
                            }
                            "top" -> {
                                if (module == modules[0]) {
                                    RenderUtils.drawRect(xPos - 2, yPos, 0F, yPos - 1, rectColor)
                                }
                            }
                        }
                    }
                }
            }

            Horizontal.LEFT -> {
                modules.forEachIndexed { index, module ->
                    val xPos = -(module.width - module.slide) + if (rectMode.equals("left", true)) 5 else 2
                    val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -textHeight } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *if (side.vertical == Vertical.DOWN) index + 1 else index }
                    val yPos = module.yPos
                    if (yPos != realYPos) {module.yPos = realYPos}
                    blur(0F, yPos, xPos + module.width + if (rectMode.equals("right", true)) 5 else 2 + backgroundExpand.get(), yPos + textHeight)
                }
                modules.forEachIndexed { index, module ->
                    var CRainbow: Int
                    CRainbow = RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter[0] * (50 * cRainbowDistValue.get()))
                    val xPos = -(module.width - module.slide) + if (rectMode.equals("left", true)) 5 else 2
                    val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -textHeight } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) index + 1 else index }
                    val yPos = module.yPos
                    if (yPos != realYPos) {
                        module.yPos = realYPos
                    }
                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

                    blur(0F, yPos, xPos + module.width + if (rectMode.equals("right", true)) 5 else 2 + backgroundExpand.get(), yPos + textHeight)
                    RenderUtils.drawRect(
                        0F,
                        yPos,
                        xPos + module.width + if (rectMode.equals("right", true)) 5 else 2 + backgroundExpand.get(),
                        yPos + textHeight,
                        when (backgroundColorMode.lowercase()) {
                            "rainbow" -> ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()).rgb
                            "random" -> moduleColor
                            "CRainbow" -> CRainbow
                            "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "Astolfo" -> RenderUtils.Astolfo(index * speed.get(), saturationValue.get(), brightnessValue.get())
                            "Static" -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1).rgb
                            "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(backgroundCustomColor, 100, index + 1).rgb
                            else -> backgroundCustomColor.rgb
                        }
                    )

                    val mName = changeCase(getModuleName(module))
                    val mTag = changeCase(getModuleTag(module))
                    fontRenderer.drawString(mName, xPos, yPos + textY, when (colorMode.lowercase()) {
                        "rainbow" -> ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()).rgb
                        "random" -> moduleColor
                        "CRainbow" -> CRainbow
                        "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                        "Static" -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1).rgb
                        "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get()).rgb
                        "anotherrainbow" -> ColorUtils.fade(customColor, 100, index + 1).rgb
                        else -> customColor.rgb
                    }, textShadow)

                    fontRenderer.drawString(mTag, xPos + fontRenderer.getStringWidth(mName), yPos + textY,
                        ColorUtils.reverseColor(when (tagColorModeValue.get().lowercase()) {
                            "rainbow" -> ColorUtils.reverseColor(ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()))
                            "random" -> Color(moduleColor)
                            "crainbow" -> Color(RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter[0] * (50 * cRainbowDistValue.get())))
                            "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble())
                            "Astolfo" -> Color(RenderUtils.Astolfo(index * speed.get(), saturationValue.get(), brightnessValue.get()))
                            "Static" -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1)
                            "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get())
                            "anotherrainbow" -> ColorUtils.reverseColor(ColorUtils.fade(tagCustomColor, 100, index + 1))
                            else -> ColorUtils.reverseColor(tagCustomColor)
                        }).rgb, textShadow)

                    if (!rectMode.equals("none", true)) {
                        val rectColor = when (rectColorMode.lowercase()) {
                            "rainbow" -> ColorUtils.hslRainbow(index + 1, indexOffset = 100 * rainbowSpeed.get()).rgb
                            "random" -> moduleColor
                            "CRainbow" -> CRainbow
                            "skyrainbow" -> ColorUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "Astolfo" -> RenderUtils.Astolfo(index * speed.get(), saturationValue.get(), brightnessValue.get())
                            "Static" -> ColorUtils.StaticRainbow(rainbowSpeed.get(), index + 1).rgb
                            "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index * 30 * rainbowSpeed.get(), saturationValue.get(), brightnessValue.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(rectCustomColor, 100, index + 1).rgb
                            else -> rectCustomColor.rgb
                        }

                        when {
                            rectMode.equals("left", true) -> RenderUtils.drawRect(0F,
                                yPos - 1, 3F, yPos + textHeight, rectColor)
                            rectMode.equals("right", true) ->
                                RenderUtils.drawRect(xPos + module.width + 2, yPos, xPos + module.width + 2 + 3,
                                    yPos + textHeight, rectColor)
                        }
                    }
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT) {
                    Border(0F, -1F, 20F, 20F)
                } else {
                    Border(0F, -1F, -20F, 20F)
                }
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }
                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }
            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = LiquidBounce.moduleManager.modules
            .filter { it.array && !shouldExpect(it) && (it.state || it.slide > 0 || !(it.yPosAnimation==null || it.yPosAnimation!!.state==Animation.EnumAnimationState.STOPPED)) }
            .sortedBy { -it.width }
    }

    override fun drawBoarderBlur(blurRadius: Float) {}
}
