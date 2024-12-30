/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.clickHeight
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.scrollMode
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl.ModuleRect
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Screen
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.StencilUtil
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.Fonts
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.extensions.roundToHalf
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedOutline
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a panel for a single [Category], containing modules in that category.
 * Each panel can be dragged and displays modules. Now includes a small outline
 * around the category's top bar if enabled in [ClickGUIModule].
 */
class MainScreen(private val category: Category) : Screen {

    private val rectWidth = 110f
    private val categoryRectHeight = 18f

    // The main fade/slide animation for this category panel
    var animation: Animation? = null

    // Opening animation references from outside (for sub-panels)
    var openingAnimation: Animation? = null

    private var moduleAnimMap = HashMap<ModuleRect, Animation>()
    private var moduleRects: MutableList<ModuleRect>? = null

    override fun initGui() {
        // Create moduleRects if null, or refresh if needed
        if (moduleRects == null) {
            moduleRects = mutableListOf<ModuleRect>().apply {
                Main.getModulesInCategory(category)
                    .sortedBy { it.name }
                    .forEach { module ->
                        val moduleRect = ModuleRect(module)
                        add(moduleRect)
                        // Each moduleRect has its own expand/collapse animation
                        moduleAnimMap[moduleRect] = DecelerateAnimation(250, 1.0)
                    }
            }
        }
        // Init each module rect
        moduleRects?.forEach { it.initGui() }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // Pass key events to each ModuleRect
        moduleRects?.forEach { it.keyTyped(typedChar, keyCode) }
    }

    /**
     * Draws the category's top bar and then the module list (with scrolling).
     */
    override fun drawScreen(mouseX: Int, mouseY: Int) {
        // Convert the animation output to an alpha [0..255]
        val animClamp = max(0.0, min(255.0, 255 * (animation?.output ?: 0.0))).toFloat()
        val alphaAnimation = animClamp.toInt()

        // Category bar background color
        val categoryRectColor = Color(29, 29, 29, alphaAnimation).rgb
        // Text color
        val textColor = Color(255, 255, 255, alphaAnimation).rgb

        val x = category.drag.x
        val y = category.drag.y

        // Dragging logic on the top bar
        category.drag.onDraw(mouseX, mouseY)

        // 1) Draw the category's top rectangle
        DrRenderUtils.drawRect2(
            x.toDouble(),
            y.toDouble(),
            rectWidth.toDouble(),
            categoryRectHeight.toDouble(),
            categoryRectColor
        )

        /**
         * 2) Draw the outline around the top bar if ClickGUIModule says so.
         *    This will create a small 1px outline around (x, y) -> (x + rectWidth, y + categoryRectHeight).
         */
        if (ClickGUIModule.categoryOutline) {
            // Example: use generateColor(0) or any other color
            val outlineColor = ClickGUIModule.generateColor(0)
            val outlineThickness = 1f
            val cornerRadius = 4f // small rounding

            drawRoundedOutline(
                x,
                y,
                x + rectWidth,
                y + categoryRectHeight,
                cornerRadius,
                outlineThickness,
                outlineColor.rgb
            )
        }

        // 3) Draw the category's name
        Fonts.SFBOLD.SFBOLD_26.drawString(
            category.name,
            x + 5,
            y + Fonts.SFBOLD.SFBOLD_26.getMiddleOfBox(categoryRectHeight),
            textColor
        )

        // Category icon logic
        val icon = when (category.name.lowercase()) {
            "combat"   -> "D"
            "movement" -> "A"
            "player"   -> "B"
            "visual"   -> "C"
            "exploit"  -> "G"
            "other"    -> "F"
            else       -> ""
        }
        Fonts.ICONFONT.ICONFONT_20.drawString(
            icon,
            x + rectWidth - (Fonts.ICONFONT.ICONFONT_20.stringWidth(icon) + 5),
            y + Fonts.ICONFONT.ICONFONT_20.getMiddleOfBox(categoryRectHeight),
            textColor
        )

        // If category name is "Client", draw something else (example from your code):
        if (category.name.equals("Client", ignoreCase = true)) {
            Fonts.CheckFont.CheckFont_20.drawString(
                "b",
                x + rectWidth - (Fonts.CheckFont.CheckFont_20.stringWidth("b") + 5),
                y + Fonts.ICONFONT.ICONFONT_20.getMiddleOfBox(categoryRectHeight),
                textColor
            )
        }

        // Determine how tall we can draw the module list
        val allowedHeight = if (scrollMode == "Value") {
            clickHeight.toFloat()
        } else {
            val sr = ScaledResolution(mc)
            2 * sr.scaledHeight / 3f
        }
        Main.allowedClickGuiHeight = allowedHeight

        // We'll see if user is hovering over module list
        val hoveringMods = DrRenderUtils.isHovering(
            x, y + categoryRectHeight, rectWidth, allowedHeight, mouseX, mouseY
        )

        // Draw modules inside a scissored region for scrolling
        StencilUtil.initStencilToWrite()
        DrRenderUtils.drawRect2(
            (x - 100).toDouble(),
            (y + categoryRectHeight).toDouble(),
            (rectWidth + 150).toDouble(),
            allowedHeight.toDouble(),
            -1
        )
        StencilUtil.readStencilBuffer(1)

        val scroll = category.scroll.scroll.toDouble()
        var count = 0.0

        // Draw each module rect (with sub-settings)
        moduleRects?.forEach { moduleRect ->
            val animation = moduleAnimMap[moduleRect]
            animation?.setDirection(
                if (moduleRect.module.expanded) Direction.FORWARDS else Direction.BACKWARDS
            )
            moduleRect.settingAnimation = animation
            moduleRect.alphaAnimation = alphaAnimation
            moduleRect.x = x
            moduleRect.height = 17f
            moduleRect.panelLimitY = y
            moduleRect.openingAnimation = openingAnimation
            moduleRect.y = (y + categoryRectHeight + (count * 17) + roundToHalf(scroll)).toFloat()
            moduleRect.width = rectWidth

            moduleRect.drawScreen(mouseX, mouseY)
            count += 1 + moduleRect.settingSize
        }

        // If we're hovering modules, allow scrolling
        if (hoveringMods) {
            category.scroll.onScroll(30)
            val hiddenHeight = ((count * 17) - allowedHeight).toFloat()
            category.scroll.maxScroll = max(0.0, hiddenHeight.toDouble()).toFloat()
        }

        StencilUtil.uninitStencilBuffer()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        // Let user drag top bar
        val canDrag = DrRenderUtils.isHovering(
            category.drag.x, category.drag.y, rectWidth, categoryRectHeight, mouseX, mouseY
        )
        category.drag.onClick(mouseX, mouseY, button, canDrag)

        // Pass click to each module
        moduleRects?.forEach { it.mouseClicked(mouseX, mouseY, button) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        // Stop drag
        category.drag.onRelease(state)

        // Pass release to each module
        moduleRects?.forEach { it.mouseReleased(mouseX, mouseY, state) }
    }
}
