/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorder
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.withClipping
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import kotlin.math.abs

@ElementInfo(name = "TabGUI")
class TabGUI(x: Double = 16.0, y: Double = 43.0) : Element("TabGUI", x = x, y = y) {

    private val rectColor = color("RectangleColor", Color(0, 148, 255, 140))

    private val rectRainbow
        get() = rectColor.rainbow && rectColor.isSupported()

    private val roundedRectRadius by float("Rounded-Radius", 3F, 0F..5F)

    private val bgColor by color("BackgroundColor", Color.BLACK.withAlpha(150))

    private val borderValue by boolean("Border", false)
    private val borderStrength by float("Border-Strength", 2F, 1F..5F) { borderValue }

    private val borderColor = color("BorderColor", Color.BLACK.withAlpha(150)) { borderValue }

    private val borderRainbow
        get() = borderColor.rainbow && borderColor.isSupported()

    private val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { rectRainbow || (borderValue && borderRainbow) }
    private val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { rectRainbow || (borderValue && borderRainbow) }

    // Icons
    private val displayIcons by boolean("DisplayIcons", true)
    private val iconRectColor by color("IconRectColor", Color.BLACK.withAlpha(200)) { displayIcons }
    private val useRectangleColorForChosenIconColor by boolean(
        "UseRectangleColorForChosenIconColor", true
    ) { displayIcons }
    private val iconCategoryChosenColor by color(
        "IconChosenCategoryColor", rectColor.selectedColor()
    ) { displayIcons && !useRectangleColorForChosenIconColor }
    private val iconNonChosenCategoryColor by color("IconNonChosenCategoryColor", Color.WHITE) { displayIcons }
    private val iconShadows by boolean("IconShadows", true) { displayIcons }
    private val xDistance by float("ShadowXDistance", 1.0F, -2F..2F) { iconShadows }
    private val yDistance by float("ShadowYDistance", 1.0F, -2F..2F) { iconShadows }
    private val shadowColor by color("ShadowColor", Color.BLACK.withAlpha(128)) { iconShadows }

    private val arrows by boolean("Arrows", false)
    private val font by font("Font", Fonts.fontSemibold35)
    private val textShadow by boolean("TextShadow", false)
    private val textFade by boolean("TextFade", true)
    private val textPositionY by float("TextPosition-Y", 2F, 0F..5F)
    private val width by float("Width", 60F, 55F..100F)
    private val tabHeight by float("TabHeight", 13F, 10F..15F)
    private val upperCase by boolean("UpperCase", false)

    private val tabs = Array(Category.entries.size) {
        val category = Category.entries[it]
        Tab(category, category.displayName)
    }

    private var categoryMenu = true
    private var selectedCategory = 0
        set(value) {
            field = when {
                value < 0 -> tabs.lastIndex
                value > tabs.lastIndex -> 0
                else -> value
            }
        }
    private var selectedModule = 0
        set(value) {
            field = when {
                value < 0 -> tabs[selectedCategory].modules.lastIndex
                value > tabs[selectedCategory].modules.lastIndex -> 0
                else -> value
            }
        }

    private var tabY = 0F
    private var itemY = 0F

    private val corners = arrayOf(RenderUtils.RoundedCorners.RIGHT_ONLY, RenderUtils.RoundedCorners.LEFT_ONLY)

    override fun drawElement(): Border {
        updateAnimation()

        val borderColor = if (borderRainbow) Color.black else borderColor.selectedColor()

        // Draw
        val guiHeight = tabs.size * tabHeight

        // 4F = tab slide
        val arrowPadding = if (arrows) 4F else 0F
        val iconPadding = if (displayIcons) 17F else 0F

        val widthWithPadding = maxOf(font.getStringWidth("Movement").toFloat() + arrowPadding, width + arrowPadding)
        val xWithPadding = 2F - iconPadding

        val iconSideX = if (side.horizontal == Side.Horizontal.RIGHT) {
            widthWithPadding + abs(xWithPadding) to widthWithPadding
        } else {
            xWithPadding to 2f
        }

        val (borderX1, borderX2) = if (displayIcons) {
            iconSideX.first to if (side.horizontal != Side.Horizontal.RIGHT) widthWithPadding else 2F
        } else {
            xWithPadding to widthWithPadding
        }

        AWTFontRenderer.assumeNonVolatile {
            val rectColor = if (rectRainbow) Color.black else rectColor.selectedColor()

            withClipping(main = {
                drawRoundedRect(
                    2F, 0F, widthWithPadding, guiHeight, bgColor.rgb, roundedRectRadius, if (displayIcons) {
                        corners[if (side.horizontal != Side.Horizontal.RIGHT) 0 else 1]
                    } else {
                        RenderUtils.RoundedCorners.ALL
                    }
                )
                if (displayIcons) {
                    drawRoundedRect(
                        iconSideX.first,
                        0F,
                        iconSideX.second,
                        guiHeight,
                        iconRectColor.rgb,
                        roundedRectRadius,
                        corners[if (side.horizontal != Side.Horizontal.RIGHT) 1 else 0]
                    )
                }
            }, toClip = {
                RainbowShader.begin(
                    rectRainbow,
                    if (rainbowX == 0f) 0f else 1f / rainbowX,
                    if (rainbowY == 0f) 0f else 1f / rainbowY,
                    System.currentTimeMillis() % 10000 / 10000F
                ).use {
                    drawRect(2F, 1 + tabY - 1, widthWithPadding, tabY + tabHeight, rectColor)
                }
            })

            glColor4f(1f, 1f, 1f, 1f)

            var y = 1F

            tabs.forEachIndexed { index, tab ->
                val tabName = tab.tabName.let { if (upperCase) it.uppercase() else it }

                val textX = if (side.horizontal == Side.Horizontal.RIGHT) {
                    widthWithPadding - font.getStringWidth(tabName) - tab.textFade - 3
                } else {
                    tab.textFade + 5
                }

                val textY = y + textPositionY

                val textColor = if (selectedCategory == index) 0xffffff else Color(210, 210, 210).rgb

                font.drawString(tabName, textX, textY, textColor, textShadow)

                if (arrows) {
                    if (side.horizontal == Side.Horizontal.RIGHT) {
                        font.drawString(
                            if (!categoryMenu && selectedCategory == index) {
                                ">"
                            } else {
                                "<"
                            }, 3F, y + 2F, 0xffffff, textShadow
                        )
                    } else {
                        font.drawString(
                            if (!categoryMenu && selectedCategory == index) "<" else ">",
                            widthWithPadding - arrowPadding - 2F,
                            y + 2F,
                            0xffffff,
                            textShadow
                        )
                    }
                }

                if (index == selectedCategory && !categoryMenu) {
                    val tabX = if (side.horizontal == Side.Horizontal.RIGHT) {
                        1F - tab.menuWidth
                    } else {
                        widthWithPadding + 5
                    }

                    tab.drawTab(
                        tabX,
                        y,
                        rectColor.rgb,
                        bgColor.rgb,
                        borderColor.rgb,
                        borderStrength,
                        font,
                        borderRainbow,
                        rectRainbow
                    )
                }

                if (borderValue) {
                    RainbowShader.begin(
                        borderRainbow,
                        if (rainbowX == 0f) 0f else 1f / rainbowX,
                        if (rainbowY == 0f) 0f else 1f / rainbowY,
                        System.currentTimeMillis() % 10000 / 10000F
                    ).use {
                        drawRoundedBorder(
                            borderX1, 0F, borderX2, guiHeight, borderStrength, borderColor.rgb, roundedRectRadius
                        )
                    }
                }

                if (displayIcons) {
                    val iconX = if (side.horizontal == Side.Horizontal.RIGHT) {
                        iconSideX.second + 1
                    } else {
                        iconSideX.first + 2
                    }

                    val resource = tab.category.iconResourceLocation

                    val iconY = y - 1

                    if (iconShadows) {
                        drawImage(resource, iconX + xDistance, iconY + yDistance, 12, 12, shadowColor)
                    }

                    val colorToUse = if (index == selectedCategory) {
                        iconCategoryChosenColor
                    } else {
                        iconNonChosenCategoryColor
                    }

                    drawImage(resource, iconX, iconY, 12, 12, colorToUse)
                }

                y += tabHeight
            }
        }

        return Border(borderX1, 0F, borderX2, guiHeight)
    }

    override fun handleKey(c: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_UP -> parseAction(Action.UP)
            Keyboard.KEY_DOWN -> parseAction(Action.DOWN)
            Keyboard.KEY_RIGHT -> parseAction(if (side.horizontal == Side.Horizontal.RIGHT) Action.LEFT else Action.RIGHT)
            Keyboard.KEY_LEFT -> parseAction(if (side.horizontal == Side.Horizontal.RIGHT) Action.RIGHT else Action.LEFT)
            Keyboard.KEY_RETURN -> parseAction(Action.TOGGLE)
        }
    }

    private fun updateAnimation() {
        val xPos = tabHeight * selectedCategory
        if (tabY.toInt() != xPos.toInt()) {
            if (xPos > tabY) tabY += 0.1F * deltaTime
            else tabY -= 0.1F * deltaTime
        } else tabY = xPos
        val xPos2 = tabHeight * selectedModule

        if (itemY.toInt() != xPos2.toInt()) {
            if (xPos2 > itemY) itemY += 0.1F * deltaTime
            else itemY -= 0.1F * deltaTime
        } else itemY = xPos2

        if (categoryMenu) itemY = 0F

        if (textFade) {
            tabs.forEachIndexed { index, tab ->
                if (index == selectedCategory) tab.textFade += 0.05F * deltaTime
                else tab.textFade -= 0.05F * deltaTime
            }
        } else {
            for (tab in tabs) tab.textFade -= 0.05F * deltaTime
        }
    }

    private fun parseAction(action: Action) {
        var toggle = false

        when (action) {
            Action.UP -> if (categoryMenu) {
                --selectedCategory
                tabY = tabHeight * selectedCategory
            } else {
                --selectedModule
                itemY = tabHeight * selectedModule
            }

            Action.DOWN -> if (categoryMenu) {
                ++selectedCategory
                tabY = tabHeight * selectedCategory
            } else {
                ++selectedModule
                itemY = tabHeight * selectedModule
            }

            Action.LEFT -> if (!categoryMenu) categoryMenu = true

            Action.RIGHT -> if (!categoryMenu) {
                toggle = true
            } else {
                categoryMenu = false
                selectedModule = 0
            }


            Action.TOGGLE -> if (!categoryMenu) toggle = true
        }

        if (toggle) {
            val sel = selectedModule
            tabs[selectedCategory].modules[sel].toggle()
        }
    }

    fun getDisplayName(module: Module) = if (upperCase) module.getName().uppercase() else module.getName()

    /**
     * TabGUI Tab
     */
    private inner class Tab(val category: Category, val tabName: String) {

        val modules = ModuleManager[category]
        var menuWidth = 0
        var textFade = 0F
            set(value) {
                field = value.coerceIn(0f, 4f)
            }

        fun drawTab(
            x: Float,
            y: Float,
            color: Int,
            backgroundColor: Int,
            borderColor: Int,
            borderStrength: Float,
            fontRenderer: FontRenderer,
            borderRainbow: Boolean,
            rectRainbow: Boolean
        ) {
            var maxWidth = 0

            for (module in modules) {
                val width = fontRenderer.getStringWidth(getDisplayName(module))
                if (width + 4 > maxWidth) maxWidth = width + 7
            }

            menuWidth = maxWidth

            val menuHeight = modules.size * tabHeight

            withClipping(main = {
                drawRoundedRect(
                    x - 1F, y - 1F, x + menuWidth - 2F, y + menuHeight - 1F, backgroundColor, roundedRectRadius
                )
            }, toClip = {
                RainbowShader.begin(
                    rectRainbow,
                    if (rainbowX == 0f) 0f else 1f / rainbowX,
                    if (rainbowY == 0f) 0f else 1f / rainbowY,
                    System.currentTimeMillis() % 10000 / 10000F
                ).use {
                    drawRect(
                        x - if (borderValue) 0 else 1,
                        y + itemY - 1,
                        x + menuWidth - 2F,
                        y + itemY + tabHeight - 1,
                        color
                    )
                }
            })

            glColor4f(1f, 1f, 1f, 1f)

            modules.forEachIndexed { index, module ->
                val moduleColor = if (module.state) 0xffffff else Color(205, 205, 205).rgb

                fontRenderer.drawString(
                    getDisplayName(module), x + 2F, y + tabHeight * index + textPositionY, moduleColor, textShadow
                )
            }

            if (borderValue) {
                RainbowShader.begin(
                    borderRainbow,
                    if (rainbowX == 0f) 0f else 1f / rainbowX,
                    if (rainbowY == 0f) 0f else 1f / rainbowY,
                    System.currentTimeMillis() % 10000 / 10000F
                ).use {
                    drawRoundedBorder(
                        x,
                        y - 1F,
                        x + menuWidth - 2F,
                        y + menuHeight - 1F,
                        borderStrength,
                        borderColor,
                        roundedRectRadius
                    )
                }
            }
        }

    }

    /**
     * TabGUI Action
     */
    enum class Action { UP, DOWN, LEFT, RIGHT, TOGGLE }
}