/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements

import me.zywl.fdpclient.FDPClient
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.Element
import net.ccbluex.liquidbounce.ui.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.hud.element.Side
import net.ccbluex.liquidbounce.ui.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(
    x: Double = 5.0,
    y: Double = 3.0,
    scale: Float = 1F,
    side: Side = Side(Horizontal.RIGHT, Vertical.UP)
) : Element(x, y, scale, side) {

    private val colorDisplay = BoolValue("Color Options:", true)
    val colorRedValue = IntegerValue("Text-R", 0, 0, 255).displayable { colorDisplay.get() }
    val colorGreenValue = IntegerValue("Text-G", 0, 0, 255).displayable { colorDisplay.get() }
    val colorBlueValue = IntegerValue("Text-B", 0, 0, 255).displayable { colorDisplay.get() }

    // Tag settings
    private val tagValue = BoolValue("Tags", true)
    private val tagsStyleValue = ListValue("TagsStyle", arrayOf("-", "|", "()", "[]", "<>", "->", "Space"), "[]").displayable { tagValue.get() }

    // Options Text
    private val orderValue = ListValue("Order", arrayOf("ABC", "Distance"), "Distance")
    private val shadowValue = BoolValue("ShadowText", true)
    private val split = BoolValue("SplitName", true)
    private val noRenderModules = BoolValue("NoRenderModules", false)
    private val caseValue = ListValue("Case", arrayOf("Upper", "Normal", "Lower"), "Normal")
    private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    private val textYValue = FloatValue("TextY", 1F, 0F, 20F)

    // Background color settings
    private val backgroundValue = IntegerValue("Background", 104, 0, 255)

    // React settings
    private val rectDisplay = BoolValue("Rect Options:", true)
    private val rectRightValue = ListValue("Rect-Right", arrayOf("None", "Left", "Right", "Outline", "Special", "Top"), "None").displayable { rectDisplay.get() }
    private val rectLeftValue = ListValue("Rect-Left", arrayOf("None", "Left", "Right"), "None").displayable { rectDisplay.get() }
    private val roundStrength = FloatValue("Rounded-Strength", 0.57F, 0F, 2F).displayable { rectDisplay.get() }

    // Shadow Options
    private val shadowShaderValue = BoolValue("Shadow", false)
    private val shadowNoCutValue = BoolValue("Shadow-NoCut", false).displayable { shadowShaderValue.get() }
    private val shadowStrength = IntegerValue("Shadow-Strength", 30, 1, 40).displayable { shadowShaderValue.get() }
    private val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Text", "Custom"), "Text").displayable { shadowShaderValue.get() }
    private val shadowColorRedValue = IntegerValue("Shadow-Red", 0, 0, 255).displayable{ shadowShaderValue.get() && shadowColorMode.get().equals("custom", true) }
    private val shadowColorGreenValue = IntegerValue("Shadow-Green", 111, 0, 255).displayable{ shadowShaderValue.get() && shadowColorMode.get().equals("custom", true) }
    private val shadowColorBlueValue = IntegerValue("Shadow-Blue", 255, 0, 255).displayable{ shadowShaderValue.get() && shadowColorMode.get().equals("custom", true) }

    private val horizontalAnimation = ListValue("Horizontal-Animation", arrayOf("Default", "None", "Slide", "Fast"), "None")
    private val verticalAnimation = ListValue("Vertical-Animation", arrayOf("None", "Delta", "Slide", "Low", "Fast"), "None")
    private val animationSpeed = FloatValue("Animation-Speed", 0.25F, 0.01F, 1F)
    companion object {
        val fontValue = FontValue("Font", Fonts.minecraftFont)
    }
    private var x2 = 0
    private var y2 = 0F

    private var modules = emptyList<Module>()
    private var sortedModules = emptyList<Module>()

    val delay = intArrayOf(0)
    private fun getModuleName(module: Module): String {
        var displayName : String = (if (split.get()) { module.splicedName } else module.localizedName) + getModTag(module)

        when (caseValue.get().lowercase()) {
            "lower" -> displayName = displayName.lowercase()
            "upper" -> displayName = displayName.uppercase()
        }

        return displayName
    }

    override fun drawElement(partialTicks: Float): Border? {
        val fontRenderer = fontValue.get()
        val counter = intArrayOf(0)

        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = RenderUtils.deltaTime

        // Draw arraylist
        val space = spaceValue.get()
        val textHeight = textHeightValue.get()
        val textY = textYValue.get()
        val textSpacer = textHeight + space

        var inx = 0
        for (module in sortedModules) {
            // update slide x
            if (module.array && !shouldExpect(module) && (module.state || module.slide != 0F)) {
                val displayString = getModuleName(module)

                val width = fontRenderer.getStringWidth(displayString)

                when (horizontalAnimation.get()) {
                    "Fast" -> {
                        if (module.state) {
                            if (module.slide < width) {
                                module.slide += animationSpeed.get() * delta
                                module.slideStep = delta / 1F
                            }
                        } else if (module.slide > 0) {
                            module.slide -= animationSpeed.get() * delta
                            module.slideStep = 0F
                        }

                        if (module.slide > width) module.slide = width.toFloat()
                    }
                    "Slide" -> {
                        if (module.state) {
                            if (module.slide < width) {
                                module.slide = AnimationUtils.animate(width.toDouble(), module.slide.toDouble(), animationSpeed.get().toDouble() * 0.025 * delta.toDouble()).toFloat()
                                module.slideStep = delta / 1F
                            }
                        } else if (module.slide > 0) {
                            module.slide = AnimationUtils.animate(-width.toDouble(), module.slide.toDouble(), animationSpeed.get().toDouble() * 0.025 * delta.toDouble()).toFloat()
                            module.slideStep = 0F
                        }
                    }
                    "Default" -> {
                        if (module.state) {
                            if (module.slide < width) {
                                module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                                module.slideStep += delta / 4F
                            }
                        } else if (module.slide > 0) {
                            module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                            module.slideStep -= delta / 4F
                        }
                    }
                    else -> {
                        module.slide = if (module.state) width.toFloat() else 0f
                        module.slideStep += (if (module.state) delta else -delta).toFloat()
                    }
                }

                module.slide = module.slide.coerceIn(0F, width.toFloat())
                module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
            }

            // update slide y
            var yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                    if (side.vertical == Vertical.DOWN) inx + 1 else inx

            if (module.array && module.slide > 0F) {
                if (verticalAnimation.get().equals("Low", ignoreCase = true) && !module.state)
                    yPos = -fontRenderer.FONT_HEIGHT - textY

                val size = modules.size * 2.0E-2f

                when (verticalAnimation.get()) {
                    "Delta" -> {
                        if (module.state) {
                            if (module.yPos < yPos) {
                                module.yPos += (size -
                                        (module.yPos * 0.002f).coerceAtMost(size - (module.yPos * 0.0001f))) * delta
                                module.yPos = yPos.coerceAtMost(module.yPos)
                            } else {
                                module.yPos -= (size -
                                        (module.yPos * 0.002f).coerceAtMost(size - (module.yPos * 0.0001f))) * delta
                                module.yPos = module.yPos.coerceAtLeast(yPos)
                            }
                        }
                    }
                    "Slide", "Low" -> module.yPos = AnimationUtils.animate(yPos.toDouble(), module.yPos.toDouble(), animationSpeed.get().toDouble() * 0.025 * delta.toDouble()).toFloat()
                    "Fast" -> {
                        if (module.yPos < yPos) {
                            module.yPos += animationSpeed.get() / 2F * delta
                            module.yPos = yPos.coerceAtMost(module.yPos)
                        } else {
                            module.yPos -= animationSpeed.get() / 2F * delta
                            module.yPos = module.yPos.coerceAtLeast(yPos)
                        }
                    }
                    else -> module.yPos = yPos
                }
                inx++
            } else if (!verticalAnimation.get().equals("low", true)) //instant update
                module.yPos = yPos
        }
        when (side.horizontal) {
            Horizontal.RIGHT, Horizontal.MIDDLE -> {
                if (shadowShaderValue.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    ShadowUtils.shadow(shadowStrength.get().toFloat(), {
                        GL11.glPushMatrix()
                        GL11.glTranslated(renderX, renderY, 0.0)
                        modules.forEachIndexed { index, module ->
                            val xPos = -module.slide - 2
                            RenderUtils.newDrawRect(
                                xPos - if (rectRightValue.get().equals("right", true)) 3 else 2,
                                module.yPos,
                                if (rectRightValue.get().equals("right", true)) -1F else 0F,
                                module.yPos + textHeight,
                                when (shadowColorMode.get().lowercase()) {
                                    "background" -> Color(0,0,0).rgb
                                    "text" -> getColor(index).rgb
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
                            modules.forEachIndexed { _, module ->
                                val xPos = -module.slide - 2
                                RenderUtils.quickDrawRect(
                                    xPos - if (rectRightValue.get().equals("right", true)) 3 else 2,
                                    module.yPos,
                                    if (rectRightValue.get().equals("right", true)) -1F else 0F,
                                    module.yPos + textHeight
                                )
                            }
                            GL11.glPopMatrix()
                        }
                    })
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }

                modules.forEachIndexed { index, module ->
                    val displayString = getModuleName(module)

                    val xPos = -module.slide - 2

                    RenderUtils.customRounded(
                        xPos - if (rectRightValue.get().equals("right", true)) 3 else 2,
                        module.yPos,
                        if (rectRightValue.get().equals("right", true)) -1F else 0F,
                        module.yPos + textHeight, 0F, 0F, 0F, roundStrength.get(), Color(0,0,0,backgroundValue.get()).rgb
                    )

                    fontRenderer.drawString(displayString, xPos - if (rectRightValue.get().equals("right", true)) 1 else 0, module.yPos + textY, getColor(index).rgb, shadowValue.get())


                    if (!rectRightValue.get().equals("none", true)) {
                        val rectColor = getColor(index).rgb

                        when {
                            rectRightValue.get().equals("left", true) -> RenderUtils.drawRect(xPos - 3, module.yPos, xPos - 2, module.yPos + textHeight,
                                rectColor)
                            rectRightValue.get().equals("right", true) -> RenderUtils.drawRect(-1F, module.yPos, 0F,
                                module.yPos + textHeight, rectColor)
                            rectRightValue.get().equals("outline", true) -> {
                                RenderUtils.drawRect(-1F, module.yPos - 1F, 0F,
                                    module.yPos + textHeight, rectColor)
                                RenderUtils.drawRect(xPos - 3, module.yPos, xPos - 2, module.yPos + textHeight,
                                    rectColor)
                                if (module != modules[0]) {
                                    val displayStrings = getModuleName(modules[index - 1])

                                    RenderUtils.drawRect(xPos - 3 - (fontRenderer.getStringWidth(displayStrings) - fontRenderer.getStringWidth(displayString)), module.yPos, xPos - 2, module.yPos + 1,
                                        rectColor)
                                    if (module == modules[modules.size - 1]) {
                                        RenderUtils.drawRect(xPos - 3, module.yPos + textHeight, 0.0F, module.yPos + textHeight + 1,
                                            rectColor)
                                    }
                                } else {
                                    RenderUtils.drawRect(xPos - 3, module.yPos, 0F, module.yPos - 1, rectColor)
                                }
                            }
                            rectRightValue.get().equals("special", true) -> {
                                if (module == modules[0]) {
                                    RenderUtils.drawRect(xPos - 2, module.yPos, 0F, module.yPos - 1, rectColor)
                                }
                                if (module == modules[modules.size - 1]) {
                                    RenderUtils.drawRect(xPos - 2, module.yPos + textHeight, 0F, module.yPos + textHeight + 1, rectColor)
                                }
                            }
                            rectRightValue.get().equals("top", true) -> {
                                if (module == modules[0]) {
                                    RenderUtils.drawRect(xPos - 2, module.yPos, 0F, module.yPos - 1, rectColor)
                                }
                            }
                        }
                    }
                }
            }

            Horizontal.LEFT -> {
                if (shadowShaderValue.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    ShadowUtils.shadow(shadowStrength.get().toFloat(), {
                        GL11.glPushMatrix()
                        GL11.glTranslated(renderX, renderY, 0.0)
                        modules.forEachIndexed { index, module ->
                            val displayString = getModuleName(module)
                            val width = fontRenderer.getStringWidth(displayString)
                            val xPos = -(width - module.slide) + if (rectLeftValue.get().equals("left", true)) 3 else 2

                            RenderUtils.newDrawRect(
                                0F,
                                module.yPos,
                                xPos + width + if (rectLeftValue.get().equals("right", true)) 3F else 2F,
                                module.yPos + textHeight,
                                when (shadowColorMode.get().lowercase()) {
                                    "background" -> Color(0,0,0).rgb
                                    "text" -> getColor(index).rgb
                                    else -> Color(shadowColorRedValue.get(), shadowColorGreenValue.get(), shadowColorBlueValue.get()).rgb
                                }
                            )
                        }
                        GL11.glPopMatrix()
                    }, {
                        if (!shadowNoCutValue.get()) {
                            GL11.glPushMatrix()
                            GL11.glTranslated(renderX, renderY, 0.0)
                            modules.forEachIndexed { _, module ->
                                val displayString = getModuleName(module)
                                val width = fontRenderer.getStringWidth(displayString)
                                val xPos = -(width - module.slide) + if (rectLeftValue.get().equals("left", true)) 3 else 2

                                RenderUtils.quickDrawRect(
                                    0F,
                                    module.yPos,
                                    xPos + width + if (rectLeftValue.get().equals("right", true)) 3 else 2,
                                    module.yPos + textHeight
                                )
                            }
                            GL11.glPopMatrix()
                        }
                    })
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }

                modules.forEachIndexed { index, module ->
                    val displayString = getModuleName(module)

                    val width = fontRenderer.getStringWidth(displayString)
                    val xPos = -(width - module.slide) + if (rectLeftValue.get().equals("left", true)) 3 else 2


                    RenderUtils.customRounded(
                        xPos - if (rectRightValue.get().equals("right", true)) 3 else 2,
                        module.yPos,
                        if (rectRightValue.get().equals("right", true)) -1F else 0F,
                        module.yPos + textHeight, 0F, 0F, roundStrength.get(), 0F, Color(0,0,0,backgroundValue.get()).rgb
                    )

                    fontRenderer.drawString(displayString, xPos, module.yPos + textY, getColor(index).rgb, shadowValue.get())

                    if (!rectLeftValue.get().equals("none", true)) {
                        val rectColor = getColor(index).rgb

                        when {
                            rectLeftValue.get().equals("left", true) -> RenderUtils.drawRect(0F,
                                module.yPos - 1, 1F, module.yPos + textHeight, rectColor)
                            rectLeftValue.get().equals("right", true) ->
                                RenderUtils.drawRect(xPos + width + 2, module.yPos, xPos + width + 2 + 1,
                                    module.yPos + textHeight, rectColor)
                        }
                    }

                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
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

        AWTFontRenderer.assumeNonVolatile = false
        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = if (orderValue.equals("ABC")) FDPClient.moduleManager.modules
            .filter {
                it.array && !shouldExpect(it) && (if (horizontalAnimation.get()
                        .equals("none", ignoreCase = true)
                ) it.state else it.slide > 0)
            }
        else FDPClient.moduleManager.modules
            .filter {
                it.array && !shouldExpect(it) && (if (horizontalAnimation.get()
                        .equals("none", ignoreCase = true)
                ) it.state else it.slide > 0)
            }
            .sortedBy { -fontValue.get().getStringWidth(getModuleName(it)) }
        sortedModules = if (orderValue.equals("ABC")) FDPClient.moduleManager.modules.toList()
        else FDPClient.moduleManager.modules.sortedBy { -fontValue.get().getStringWidth(getModuleName(it)) }.toList()
    }

    private fun getModTag(m: Module): String {
        if (!tagValue.get() || m.tag == null) return ""

        var returnTag = " §7"

        // tag prefix, ignore default value
        if (!tagsStyleValue.get().equals("space", true))
            returnTag +=
                tagsStyleValue.get()[0].toString() + if (tagsStyleValue.get().equals("-", true) || tagsStyleValue.get().equals("|", true)) " " else ""

        // main tag value
        returnTag += m.tag

        // tag suffix, ignore default, -, | values
        if (!tagsStyleValue.get().equals("space", true)
            && !tagsStyleValue.get().equals("-", true)
            && !tagsStyleValue.get().equals("|", true)
            && !tagsStyleValue.get().equals("->", true)
        )
            returnTag += if (tagsStyleValue.equals("->")) {
                tagsStyleValue.get()
            } else {
                tagsStyleValue.get()[1].toString()
            }
        return returnTag
    }

    private fun shouldExpect(module: Module): Boolean {
        return noRenderModules.get() && module.category == ModuleCategory.VISUAL
    }

    fun getColor(index : Int) : Color {
        return ClientTheme.getColor(index)
    }
}