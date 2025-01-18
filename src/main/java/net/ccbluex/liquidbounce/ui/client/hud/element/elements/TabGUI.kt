/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.color
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.font
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorder
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect2
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.minecraft.client.gui.FontRenderer
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

@ElementInfo(name = "TabGUI")
class TabGUI(x: Double = 2.0, y: Double = 31.0) : Element(x = x, y = y) {

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

    private val arrows by boolean("Arrows", true)
    private val font by font("Font", Fonts.font35)
    private val textShadow by boolean("TextShadow", false)
    private val textFade by boolean("TextFade", true)
    private val textPositionY by float("TextPosition-Y", 2F, 0F..5F)
    private val width by float("Width", 60F, 55F..100F)
    private val tabHeight by float("TabHeight", 12F, 10F..15F)
    private val upperCase by boolean("UpperCase", false)

    private val tabs = mutableListOf<Tab>()

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

    init {
        for (category in Category.values()) {
            val tab = Tab(category.displayName)

            moduleManager.forEach { module ->
                if (category == module.category) {
                    tab.modules += module
                }
            }

            tabs += tab
        }
    }

    override fun drawElement(): Border {
        updateAnimation()

        val borderColor = if (borderRainbow) Color.black else borderColor.selectedColor()

        // Draw
        val guiHeight = tabs.size * tabHeight

        AWTFontRenderer.assumeNonVolatile {
            drawRoundedRect(1F, 0F, width, guiHeight, bgColor.rgb, roundedRectRadius)

            val rectColor = if (rectRainbow) Color.black else rectColor.selectedColor()

            RainbowShader.begin(
                rectRainbow,
                if (rainbowX == 0f) 0f else 1f / rainbowX,
                if (rainbowY == 0f) 0f else 1f / rainbowY,
                System.currentTimeMillis() % 10000 / 10000F
            ).use {
                val cornerToRound = when (selectedCategory) {
                    0 -> RenderUtils.RoundedCorners.TOP_ONLY
                    tabs.lastIndex -> RenderUtils.RoundedCorners.BOTTOM_ONLY
                    else -> RenderUtils.RoundedCorners.NONE
                }

                drawRoundedRect2(1F, 1 + tabY - 1, width, tabY + tabHeight, rectColor, roundedRectRadius, cornerToRound)
            }

            if (borderValue) {
                RainbowShader.begin(
                    borderRainbow,
                    if (rainbowX == 0f) 0f else 1f / rainbowX,
                    if (rainbowY == 0f) 0f else 1f / rainbowY,
                    System.currentTimeMillis() % 10000 / 10000F
                ).use {
                    drawRoundedBorder(1F, 0F, width, guiHeight, borderStrength, borderColor.rgb, roundedRectRadius)
                }
            }

            glColor4f(1f, 1f, 1f, 1f)

            var y = 1F
            tabs.forEachIndexed { index, tab ->
                val tabName = if (upperCase) tab.tabName.uppercase()
                else tab.tabName

                val textX =
                    if (side.horizontal == Side.Horizontal.RIGHT) width - font.getStringWidth(tabName) - tab.textFade - 3
                    else tab.textFade + 5
                val textY = y + textPositionY

                val textColor = if (selectedCategory == index) 0xffffff else Color(210, 210, 210).rgb

                font.drawString(tabName, textX, textY, textColor, textShadow)

                if (arrows) {
                    if (side.horizontal == Side.Horizontal.RIGHT) font.drawString(
                        if (!categoryMenu && selectedCategory == index) ">" else "<", 3F, y + 2F, 0xffffff, textShadow
                    )
                    else font.drawString(
                        if (!categoryMenu && selectedCategory == index) "<" else ">",
                        width - 8F,
                        y + 2F,
                        0xffffff,
                        textShadow
                    )
                }

                if (index == selectedCategory && !categoryMenu) {
                    val tabX = if (side.horizontal == Side.Horizontal.RIGHT) 1F - tab.menuWidth
                    else width + 5

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
                y += tabHeight
            }
        }

        return Border(1F, 0F, width, guiHeight)
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
    private inner class Tab(val tabName: String) {

        val modules = mutableListOf<Module>()
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

            drawRoundedRect(x - 1F, y - 1F, x + menuWidth - 2F, y + menuHeight - 1F, backgroundColor, roundedRectRadius)

            RainbowShader.begin(
                rectRainbow,
                if (rainbowX == 0f) 0f else 1f / rainbowX,
                if (rainbowY == 0f) 0f else 1f / rainbowY,
                System.currentTimeMillis() % 10000 / 10000F
            ).use {
                val cornerToRound = when (selectedModule) {
                    0 -> RenderUtils.RoundedCorners.TOP_ONLY
                    tabs[selectedCategory].modules.lastIndex -> RenderUtils.RoundedCorners.BOTTOM_ONLY
                    else -> RenderUtils.RoundedCorners.NONE
                }

                drawRoundedRect(
                    x - if (borderValue) 0 else 1,
                    y + itemY - 1,
                    x + menuWidth - 2F,
                    y + itemY + tabHeight - 1,
                    color,
                    roundedRectRadius,
                    cornerToRound
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

            glColor4f(1f, 1f, 1f, 1f)

            modules.forEachIndexed { index, module ->
                val moduleColor = if (module.state) 0xffffff else Color(205, 205, 205).rgb

                fontRenderer.drawString(
                    getDisplayName(module), x + 2F, y + tabHeight * index + textPositionY, moduleColor, textShadow
                )
            }
        }

    }

    /**
     * TabGUI Action
     */
    enum class Action { UP, DOWN, LEFT, RIGHT, TOGGLE }
}