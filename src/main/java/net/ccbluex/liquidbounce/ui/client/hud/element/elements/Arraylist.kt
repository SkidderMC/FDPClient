/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.GameDetector
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.safeDiv
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.util.ResourceLocation
import java.awt.Color
import kotlin.Pair

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(
    x: Double = 1.0, y: Double = 2.0, scale: Float = 1F,
    side: Side = Side(Horizontal.RIGHT, Vertical.UP),
) : Element("Arraylist", x, y, scale, side) {

    private val textColorMode by choices(
        "Text-Mode", arrayOf("Custom", "Fade", "Theme", "Random", "Rainbow", "Gradient"), "Theme"
    )
    private val textColors = ColorSettingsInteger(this, "TextColor") { textColorMode == "Custom" }.with(0, 111, 255)
    private val textFadeColors = ColorSettingsInteger(this, "Text-Fade") { textColorMode == "Fade" }.with(0, 111, 255)

    private val textFadeDistance by int("Text-Fade-Distance", 50, 0..100) { textColorMode == "Fade" }

    private val gradientTextSpeed by float("Text-Gradient-Speed", 1f, 0.5f..10f) { textColorMode == "Gradient" }

    private val maxTextGradientColors by int(
        "Max-Text-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS
    ) { textColorMode == "Gradient" }
    private val textGradColors =
        ColorSettingsFloat.create(this, "Text-Gradient") { textColorMode == "Gradient" && it <= maxTextGradientColors }

    private val rectMode by choices("Rect-Mode", arrayOf("None", "Left", "Right", "Outline", "Special", "Top"), "Right")
    private val roundedRectRadius by float("RoundedRect-Radius", 2F, 0F..2F) { rectMode !in setOf("None", "Outline") }
    private val rectColorMode by choices(
        "Rect-ColorMode", arrayOf("Custom", "Fade", "Theme", "Random", "Rainbow", "Gradient"), "Theme"
    ) { rectMode != "None" }
    private val rectColors =
        ColorSettingsInteger(this, "RectColor", applyMax = true) { isCustomRectSupported }
    private val rectFadeColors = ColorSettingsInteger(this, "Rect-Fade", applyMax = true) { rectColorMode == "Fade" }

    private val rectFadeDistance by int("Rect-Fade-Distance", 50, 0..100) { rectColorMode == "Fade" }

    private val gradientRectSpeed by float("Rect-Gradient-Speed", 1f, 0.5f..10f) { isCustomRectGradientSupported }

    private val maxRectGradientColors by int(
        "Max-Rect-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS
    ) { isCustomRectGradientSupported }
    private val rectGradColors = ColorSettingsFloat.create(
        this, "Rect-Gradient"
    ) { isCustomRectGradientSupported && it <= maxRectGradientColors }

    private val roundedBackgroundRadius by float("RoundedBackGround-Radius", 0F, 0F..5F) { bgColors.color().alpha > 0 }

    private val backgroundMode by choices(
        "Background-Mode", arrayOf("Custom", "Fade", "Theme", "Random", "Rainbow", "Gradient"), "Custom"
    )
    private val bgColors =
        ColorSettingsInteger(this, "BackgroundColor") { backgroundMode == "Custom" }.with(Color.BLACK.withAlpha(155))
    private val bgFadeColors = ColorSettingsInteger(this, "Background-Fade") { backgroundMode == "Fade" }

    private val bgFadeDistance by int("Background-Fade-Distance", 50, 0..100) { backgroundMode == "Fade" }

    private val gradientBackgroundSpeed by float(
        "Background-Gradient-Speed", 1f, 0.5f..10f
    ) { backgroundMode == "Gradient" }

    private val maxBackgroundGradientColors by int(
        "Max-Background-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS
    ) { backgroundMode == "Gradient" }
    private val bgGradColors = ColorSettingsFloat.create(
        this, "Background-Gradient"
    ) { backgroundMode == "Gradient" && it <= maxBackgroundGradientColors }

    // Icons
    private val displayIcons by boolean("DisplayIcons", true)
    private val iconShadows by boolean("IconShadows", true) { displayIcons }
    private val xDistance by float("ShadowXDistance", 0F, -2F..2F) { iconShadows }
    private val yDistance by float("ShadowYDistance", 0F, -2F..2F) { iconShadows }
    private val shadowColor by color("ShadowColor", Color.BLACK.withAlpha(128), rainbow = true) { iconShadows }

    private val iconColorMode by choices(
        "IconColorMode", arrayOf("Custom", "Fade", "Theme",), "Theme"
    ) { displayIcons }
    private val iconColor by color("IconColor", Color.WHITE) { iconColorMode == "Custom" && displayIcons }
    private val iconFadeColor by color("IconFadeColor", Color.WHITE) { iconColorMode == "Fade" && displayIcons }
    private val iconFadeDistance by int("IconFadeDistance", 50, 0..100) { iconColorMode == "Fade" && displayIcons }

    private fun isColorModeUsed(value: String) = value in listOf(textColorMode, rectMode, backgroundMode, iconColorMode)

    private val saturation by float("Random-Saturation", 0.9f, 0f..1f) { isColorModeUsed("Random") }
    private val brightness by float("Random-Brightness", 1f, 0f..1f) { isColorModeUsed("Random") }
    private val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val gradientX by float("Gradient-X", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }
    private val gradientY by float("Gradient-Y", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }

    private val tags by boolean("Tags", true)
    private val tagsStyle by choices("TagsStyle", arrayOf("[]", "()", "<>", "-", "|", "Space"), "Space") {
        tags
    }.onChanged { updateTagDetails() }
    private val tagsCase by choices("TagsCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal") { tags }
    private val tagsArrayColor by boolean("TagsArrayColor", false) {
        tags
    }.onChanged { updateTagDetails() }

    private val font by font("Font", Fonts.fontSemibold40)
    private val textShadow by boolean("ShadowText", true)
    private val moduleCase by choices("ModuleCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal")
    private val space by float("Space", 1F, 0F..5F)
    private val textHeight by float("TextHeight", 11F, 1F..20F)
    private val textY by float("TextY", 3.25F, 0F..20F)

    private val animation by choices("Animation", arrayOf("Slide", "Smooth"), "Smooth") { tags }
    private val animationSpeed by float("AnimationSpeed", 0.2F, 0.01F..1F) { animation == "Smooth" }

    companion object : Configurable("StandaloneArraylist") {
        val spacedModulesValue = boolean("SpacedModules", false)
    }

    private val spacedModules: Boolean by +spacedModulesValue

    private val inactiveStyle by choices(
        "InactiveModulesStyle", arrayOf("Normal", "Color", "Hide"), "Color"
    ) { GameDetector.state }

    private var x2 = 0
    private var y2 = 0F

    private lateinit var tagPrefix: String

    private lateinit var tagSuffix: String

    private var modules = emptyList<Module>()

    private val inactiveColor = Color(255, 255, 255, 100).rgb

    private val isCustomRectSupported
        get() = rectMode != "None" && rectColorMode == "Custom"

    private val isCustomRectGradientSupported
        get() = rectMode != "None" && rectColorMode == "Gradient"

    init {
        updateTagDetails()
    }

    fun updateTagDetails() {
        val pair: Pair<String, String> = when (tagsStyle) {
            "[]", "()", "<>" -> tagsStyle[0].toString() to tagsStyle[1].toString()
            "-", "|" -> tagsStyle[0] + " " to ""
            else -> "" to ""
        }

        tagPrefix = (if (tagsArrayColor) " " else " §7") + pair.first
        tagSuffix = pair.second
    }

    private fun getDisplayString(module: Module): String {
        val moduleName = when (moduleCase) {
            "Uppercase" -> module.getName().uppercase()
            "Lowercase" -> module.getName().lowercase()
            else -> module.getName()
        }

        var tag = module.tag ?: ""

        tag = when (tagsCase) {
            "Uppercase" -> tag.uppercase()
            "Lowercase" -> tag.lowercase()
            else -> tag
        }

        val moduleTag = if (tags && !module.tag.isNullOrEmpty()) tagPrefix + tag + tagSuffix else ""

        return moduleName + moduleTag
    }

    override fun drawElement(): Border? {
        assumeNonVolatile {
            // Slide animation - update every render
            val delta = deltaTime

            val padding = if (displayIcons) 15 else 0

            for (module in moduleManager) {
                val shouldShow = (!module.isHidden && module.state && (inactiveStyle != "Hide" || module.isActive))

                if (!shouldShow && module.slide <= 0f) continue

                val displayString = getDisplayString(module)

                val width = font.getStringWidth(displayString) + padding

                when (animation) {
                    "Slide" -> {
                        // If modules become inactive because they only work when in game, animate them as if they got disabled
                        module.slideStep += if (shouldShow) delta / 4F else -delta / 4F
                        if (shouldShow) {
                            if (module.slide < width) {
                                module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                            }
                        } else {
                            module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                        }

                        module.slide = module.slide.coerceIn(0F, width.toFloat())
                        module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
                    }

                    "Smooth" -> {
                        val target = if (shouldShow) width.toDouble() else -width / 5.0
                        module.slide =
                            AnimationUtil.base(module.slide.toDouble(), target, animationSpeed.toDouble()).toFloat()
                    }
                }
            }
            // Draw arraylist
            val textCustomColor = textColors.color().rgb
            val rectCustomColor = rectColors.color().rgb
            val backgroundCustomColor = bgColors.color().rgb
            val textSpacer = textHeight + space

            val rainbowOffset = System.currentTimeMillis() % 10000 / 10000F
            val rainbowX = 1f safeDiv rainbowX
            val rainbowY = 1f safeDiv rainbowY

            val gradientOffset = System.currentTimeMillis() % 10000 / 10000F
            val gradientX = 1f safeDiv gradientX
            val gradientY = 1f safeDiv gradientY

            modules.forEachIndexed { index, module ->
                val themeColor = getColor(index).rgb
                var yPos =
                    (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * if (side.vertical == Vertical.DOWN) index + 1 else index
                if (animation == "Smooth") {
                    module.yAnim = AnimationUtil.base(module.yAnim.toDouble(), yPos.toDouble(), 0.2).toFloat()
                    yPos = module.yAnim
                }
                val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

                val textFadeColor = fade(textFadeColors, index * textFadeDistance, 100).rgb
                val bgFadeColor = fade(bgFadeColors, index * bgFadeDistance, 100).rgb
                val rectFadeColor = fade(rectFadeColors, index * rectFadeDistance, 100).rgb
                val iconFadeColor = fade(iconFadeColor, index * iconFadeDistance, 100).rgb

                val markAsInactive = inactiveStyle == "Color" && !module.isActive

                val displayString = getDisplayString(module)
                val displayStringWidth = font.getStringWidth(displayString)

                val previousDisplayString = getDisplayString(modules[(if (index > 0) index else 1) - 1])
                val previousDisplayStringWidth = font.getStringWidth(previousDisplayString)

                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide - if (displayIcons) 2 else 3

                        GradientShader.begin(
                            !markAsInactive && backgroundMode == "Gradient",
                            gradientX,
                            gradientY,
                            bgGradColors.toColorArray(maxBackgroundGradientColors),
                            gradientBackgroundSpeed,
                            gradientOffset
                        ).use {
                            RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                                drawRoundedRect(
                                    xPos - if (rectMode == "Right") 5 else 2,
                                    yPos,
                                    if (rectMode == "Right") -3F else -1F,
                                    yPos + textSpacer,
                                    when (backgroundMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> bgFadeColor
                                        "Theme" -> themeColor
                                        else -> backgroundCustomColor
                                    },
                                    roundedBackgroundRadius,
                                    if (rectMode == "Left") {
                                        RenderUtils.RoundedCorners.NONE
                                    } else {
                                        RenderUtils.RoundedCorners.LEFT_ONLY
                                    }
                                )
                            }
                        }

                        GradientFontShader.begin(
                            !markAsInactive && textColorMode == "Gradient",
                            gradientX,
                            gradientY,
                            textGradColors.toColorArray(maxTextGradientColors),
                            gradientTextSpeed,
                            gradientOffset
                        ).use {
                            RainbowFontShader.begin(
                                !markAsInactive && textColorMode == "Rainbow", rainbowX, rainbowY, rainbowOffset
                            ).use {
                                font.drawString(
                                    displayString,
                                    xPos + 1 - if (rectMode == "Right") 3 else 0,
                                    yPos + textY,
                                    if (markAsInactive) inactiveColor
                                    else when (textColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> textFadeColor
                                        "Theme" -> themeColor
                                        else -> textCustomColor
                                    },
                                    textShadow,
                                )
                            }
                        }

                        GradientShader.begin(
                            !markAsInactive && isCustomRectGradientSupported,
                            gradientX,
                            gradientY,
                            rectGradColors.toColorArray(maxRectGradientColors),
                            gradientRectSpeed,
                            gradientOffset
                        ).use {
                            if (rectMode != "None") {
                                RainbowShader.begin(
                                    !markAsInactive && rectColorMode == "Rainbow", rainbowX, rainbowY, rainbowOffset
                                ).use {
                                    val rectColor = if (markAsInactive) inactiveColor
                                    else when (rectColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> rectFadeColor
                                        "Theme" -> themeColor
                                        else -> rectCustomColor
                                    }

                                    when (rectMode) {
                                        "Left" -> drawRoundedRect(
                                            xPos - 5,
                                            yPos,
                                            xPos - 2,
                                            yPos + textSpacer,
                                            rectColor,
                                            roundedRectRadius,
                                            RenderUtils.RoundedCorners.LEFT_ONLY
                                        )

                                        "Right" -> drawRoundedRect(
                                            -3F,
                                            yPos,
                                            0F,
                                            yPos + textSpacer,
                                            rectColor,
                                            roundedRectRadius,
                                            if (modules.lastIndex == 0) {
                                                RenderUtils.RoundedCorners.RIGHT_ONLY
                                            } else when (module) {
                                                modules.first() -> RenderUtils.RoundedCorners.TOP_RIGHT_ONLY
                                                modules.last() -> RenderUtils.RoundedCorners.BOTTOM_RIGHT_ONLY
                                                else -> RenderUtils.RoundedCorners.NONE
                                            }
                                        )

                                        "Outline" -> {
                                            drawRect(-1F, yPos - 1F, 0F, yPos + textSpacer, rectColor)
                                            drawRect(xPos - 3, yPos, xPos - 2, yPos + textSpacer, rectColor)

                                            if (module == modules.first()) {
                                                drawRect(xPos - 3, yPos - 1F, 0F, yPos, rectColor)
                                            }

                                            drawRect(
                                                xPos - 3 - (previousDisplayStringWidth - displayStringWidth),
                                                yPos,
                                                xPos - 2,
                                                yPos + 1,
                                                rectColor
                                            )

                                            if (module == modules.last()) {
                                                drawRect(
                                                    xPos - 3, yPos + textSpacer, 0F, yPos + textSpacer + 1, rectColor
                                                )
                                            }
                                        }

                                        "Special" -> {
                                            if (module == modules[0]) {
                                                drawRoundedRect(xPos - 2, yPos, 0F, yPos - 1, rectColor, roundedRectRadius)
                                            }
                                            if (module == modules[modules.size - 1]) {
                                                drawRoundedRect(xPos - 2, yPos + textHeight, 0F, yPos + textHeight + 1, rectColor, roundedRectRadius)
                                            }
                                        }

                                        "Top" -> {
                                            if (module == modules[0]) {
                                                drawRoundedRect(xPos - 2, yPos, 0F, yPos - 1, rectColor, roundedRectRadius)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Horizontal.LEFT -> {
                        val width = font.getStringWidth(displayString)
                        val xPos = -(width - module.slide) + if (rectMode == "Left") 6 else 3

                        GradientShader.begin(
                            !markAsInactive && backgroundMode == "Gradient",
                            gradientX,
                            gradientY,
                            bgGradColors.toColorArray(maxBackgroundGradientColors),
                            gradientBackgroundSpeed,
                            gradientOffset
                        ).use {
                            RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                                drawRoundedRect(
                                    if (rectMode == "Left") 1f else 0f,
                                    yPos,
                                    xPos + width + if (rectMode == "Right") 4 else 1,
                                    yPos + textSpacer,
                                    when (backgroundMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> bgFadeColor
                                        "Theme" -> themeColor
                                        else -> backgroundCustomColor
                                    },
                                    roundedBackgroundRadius,
                                    if (rectMode == "Right") {
                                        RenderUtils.RoundedCorners.NONE
                                    } else {
                                        RenderUtils.RoundedCorners.RIGHT_ONLY
                                    }
                                )
                            }
                        }

                        GradientFontShader.begin(
                            !markAsInactive && textColorMode == "Gradient",
                            gradientX,
                            gradientY,
                            textGradColors.toColorArray(maxTextGradientColors),
                            gradientTextSpeed,
                            gradientOffset
                        ).use {
                            RainbowFontShader.begin(
                                !markAsInactive && textColorMode == "Rainbow", rainbowX, rainbowY, rainbowOffset
                            ).use {
                                font.drawString(
                                        displayString, xPos - 1, yPos + textY, if (markAsInactive) inactiveColor
                                        else when (textColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> textFadeColor
                                        "Theme" -> themeColor
                                        else -> textCustomColor
                                    }, textShadow
                                )
                            }
                        }

                        GradientShader.begin(
                            !markAsInactive && isCustomRectGradientSupported,
                            gradientX,
                            gradientY,
                            rectGradColors.toColorArray(maxRectGradientColors),
                            gradientRectSpeed,
                            gradientOffset
                        ).use {
                            RainbowShader.begin(
                                !markAsInactive && rectColorMode == "Rainbow", rainbowX, rainbowY, rainbowOffset
                            ).use {
                                if (rectMode != "None") {
                                    val rectColor = if (markAsInactive) inactiveColor
                                    else when (rectColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> rectFadeColor
                                        "Theme" -> themeColor
                                        else -> rectCustomColor
                                    }

                                    when (rectMode) {
                                        "Left" -> drawRoundedRect(
                                            0F,
                                            yPos,
                                            3F,
                                            yPos + textSpacer,
                                            rectColor,
                                            roundedRectRadius,
                                            if (modules.lastIndex == 0) {
                                                RenderUtils.RoundedCorners.LEFT_ONLY
                                            } else when (module) {
                                                modules.first() -> RenderUtils.RoundedCorners.TOP_LEFT_ONLY
                                                modules.last() -> RenderUtils.RoundedCorners.BOTTOM_LEFT_ONLY
                                                else -> RenderUtils.RoundedCorners.NONE
                                            }
                                        )

                                        "Right" -> drawRoundedRect(
                                            xPos + width + 2,
                                            yPos,
                                            xPos + width + 2 + 3,
                                            yPos + textSpacer,
                                            rectColor,
                                            roundedRectRadius,
                                            RenderUtils.RoundedCorners.RIGHT_ONLY
                                        )

                                        "Outline" -> {
                                            drawRect(-1F, yPos - 1F, 0F, yPos + textSpacer, rectColor)
                                            drawRect(
                                                xPos + width + 1,
                                                yPos - 1F,
                                                xPos + width + 2,
                                                yPos + textSpacer,
                                                rectColor
                                            )

                                            if (module == modules.first()) {
                                                drawRect(xPos + width + 2, yPos - 1, xPos + width + 2, yPos, rectColor)
                                                drawRect(-1F, yPos - 1, xPos + width + 2, yPos, rectColor)
                                            }


                                            drawRect(
                                                xPos + width + 1,
                                                yPos - 1,
                                                xPos + width + 2 + (previousDisplayStringWidth - displayStringWidth),
                                                yPos,
                                                rectColor
                                            )

                                            if (module == modules.last()) {
                                                drawRect(
                                                    xPos + width + 1,
                                                    yPos + textSpacer,
                                                    xPos + width + 2,
                                                    yPos + textSpacer + 1,
                                                    rectColor
                                                )
                                                drawRect(
                                                    -1F,
                                                    yPos + textSpacer,
                                                    xPos + width + 2,
                                                    yPos + textSpacer + 1,
                                                    rectColor
                                                )
                                            }
                                        }

                                        "Special" -> {
                                            if (module == modules.first()) {
                                                drawRoundedRect(0F, yPos, 3F, yPos - 1, rectColor, roundedRectRadius)
                                            }
                                            if (module == modules.last()) {
                                                drawRoundedRect(0F, yPos + textSpacer, 3F, yPos + textSpacer + 1, rectColor, roundedRectRadius)
                                            }
                                        }

                                        "Top" -> {
                                            if (module == modules.first()) {
                                                drawRoundedRect(0F, yPos, 3F, yPos - 1, rectColor, roundedRectRadius)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (displayIcons) {
                    val width = font.getStringWidth(displayString)

                    val side = if (side.horizontal == Side.Horizontal.LEFT) {
                        (-width + module.slide) / 6 + if (rectMode == "Left") 3 else 0
                    } else {
                        -module.slide - 2 + width + if (rectMode == "Right") 0 else 2
                    }

                    val resource = module.category.iconResourceLocation

                    if (iconShadows) {
                        drawImage(resource, side + xDistance, yPos + yDistance, 12, 12, shadowColor)
                    }

                    val iconColor = if (markAsInactive) {
                        inactiveColor
                    } else when (iconColorMode) {
                        "Gradient" -> 0
                        "Rainbow" -> 0
                        "Fade" -> iconFadeColor
                        "Theme" -> themeColor
                        else -> this.iconColor.rgb
                    }

                    drawImage(resource, side, yPos, 12, 12, Color(iconColor, true))
                }
            }

            // Draw border
            if (mc.currentScreen is GuiHudDesigner) {
                x2 = Int.MIN_VALUE

                if (modules.isEmpty()) {
                    return if (side.horizontal == Horizontal.LEFT) Border(0F, -1F, 20F, 20F)
                    else Border(0F, -1F, -20F, 20F)
                }

                for (module in modules) {
                    when (side.horizontal) {
                        Horizontal.RIGHT, Horizontal.MIDDLE -> {
                            val xPos = -module.slide.toInt() - 2
                            if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                        }

                        Horizontal.LEFT -> {
                            val xPos = module.slide.toInt() + 16
                            if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                        }
                    }
                }

                y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

                return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
            }
        }

        resetColor()
        return null
    }

    override fun updateElement() {
        modules = moduleManager.filter { it.slide > 0 && !it.isHidden }
            .sortedBy { -font.getStringWidth(getDisplayString(it)) }
    }
}