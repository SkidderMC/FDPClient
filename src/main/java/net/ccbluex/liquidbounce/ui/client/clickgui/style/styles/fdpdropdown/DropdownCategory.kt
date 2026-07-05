/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.clickHeight
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.scrollMode
import net.ccbluex.liquidbounce.ui.client.clickgui.ModuleSearch
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl.ModuleRect
import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction
import net.ccbluex.liquidbounce.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Screen
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.StencilUtils
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.extensions.roundToHalf
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a panel for a single [Category], containing modules in that category.
 * Each panel can be dragged and displays modules. Now includes a small outline
 * around the category's top bar if enabled in [ClickGUIModule].
 */
class DropdownCategory(
    private val category: Category,
    private val search: ModuleSearch
) : Screen {

    private val rectWidth = 110f
    private val categoryRectHeightBase = 18f
    private val categoryRectHeight: Float
        get() = categoryRectHeightBase + (ClickGUIModule.roundedRectRadius * 2)
    // The main fade/slide animation for this category panel
    var animation: Animation? = null

    // Opening animation references from outside (for sub-panels)
    var openingAnimation: Animation? = null

    // Whether this category's module list is collapsed (toggled by right-clicking the bar)
    private var collapsed = false

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
        if (collapsed) return
        // Pass key events to each ModuleRect
        visibleModuleRects().forEach { it.keyTyped(typedChar, keyCode) }
    }

    /**
     * Draws the category's top bar and then the module list (with scrolling).
     */
    override fun drawScreen(mouseX: Int, mouseY: Int) {
        val visibleModuleRects = visibleModuleRects()
        if (search.active && visibleModuleRects.isEmpty()) return

        // Convert the animation output to an alpha [0..255]
        val animClamp = max(0.0, min(255.0, 255 * (animation?.output ?: 0.0))).toFloat()
        val alphaAnimation = animClamp.toInt()

        // Category bar background color
        val categoryRectColor = if (ClickGUIModule.headerColor) {
            ClickGUIModule.generateColor(0).rgb
        } else {
            Color(29, 29, 29, alphaAnimation).rgb
        }
        // Text color
        val textColor = Color(255, 255, 255, alphaAnimation).rgb

        val x = category.drag.x
        val y = category.drag.y

        // Dragging logic on the top bar
        category.drag.onDraw(mouseX, mouseY)

        val allowedHeight = if (scrollMode == "Value") {
            clickHeight.toFloat()
        } else {
            val sr = ScaledResolution(mc)
            2 * sr.scaledHeight / 3f
        }
        Main.allowedClickGuiHeight = allowedHeight

        // Collapsed categories keep only the top bar; the module list reserves no height.
        val listHeight = if (collapsed) 0f else allowedHeight

        /**
         * 2) Draw the outline around the top bar and module list
         * This will create a single outline around (x, y) -> (x + rectWidth, y + categoryRectHeight + listHeight).
         */
        if (ClickGUIModule.categoryOutline) {
            val outlineColor = ClickGUIModule.generateColor(0)
            val cornerRadius = ClickGUIModule.roundedRectRadius
            RenderUtils.drawRoundedRect(
                x - 1,
                y - 1,
                x + rectWidth + 1,
                y + categoryRectHeight + listHeight + 1,
                cornerRadius,
                outlineColor.rgb
            )
        }

        // 1) Draw the category's top rectangle
        RenderUtils.drawGradientRect(
            x,
            y,
            (x + rectWidth).toInt(),
            (y + categoryRectHeight).toInt(),
            categoryRectColor,
            Color(categoryRectColor).darker().rgb,
            0F
        )

        Fonts.InterBold_26.drawString(
            if (collapsed) "+" else "-",
            x + 5,
            y + Fonts.InterBold_26.getMiddleOfBox(categoryRectHeight),
            textColor
        )
        Fonts.InterBold_26.drawString(
            category.name,
            x + 13,
            y + Fonts.InterBold_26.getMiddleOfBox(categoryRectHeight),
            textColor
        )

        val icon = when (category.name.lowercase()) {
            "combat" -> "D"
            "movement" -> "A"
            "player" -> "B"
            "visual" -> "C"
            "exploit" -> "G"
            "other" -> "F"
            else -> ""
        }
        Fonts.ICONFONT_20.drawString(
            icon,
            x + rectWidth.toInt() - (Fonts.ICONFONT_20.stringWidth(icon) + 5),
            y + Fonts.ICONFONT_20.getMiddleOfBox(categoryRectHeight),
            textColor
        )

        if (category.name.equals("Client", ignoreCase = true)) {
            Fonts.CheckFont_20.drawString(
                "b",
                x + rectWidth.toInt() - (Fonts.CheckFont_20.stringWidth("b") + 5),
                y + Fonts.ICONFONT_20.getMiddleOfBox(categoryRectHeight),
                textColor
            )
        }

        if (collapsed) {
            return
        }

        // We'll see if user is hovering over module list
        val hoveringMods = RenderHelper.isHovering(
            x, y + categoryRectHeight, rectWidth, allowedHeight, mouseX, mouseY
        )

        // Draw modules inside a scissored region for scrolling
        StencilUtils.initStencilToWrite()
        RenderUtils.drawGradientRect(
            (x - 100).toDouble(),
            (y + categoryRectHeight).toDouble(),
            (x - 100 + rectWidth + 150).toDouble(),
            (y + categoryRectHeight + allowedHeight).toDouble(),
            -1,
            -1,
            0F
        )
        StencilUtils.readStencilBuffer(1)

        val scroll = category.scroll.scroll.toDouble()
        var count = 0.0

        // Draw each module rect (with sub-settings)
        visibleModuleRects.forEach { moduleRect ->
            val animation = moduleAnimMap[moduleRect]
            animation?.direction = if (moduleRect.module.expanded) Direction.FORWARDS else Direction.BACKWARDS

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

        StencilUtils.uninitStencilBuffer()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (search.active && visibleModuleRects().isEmpty()) return

        // Let user drag top bar
        val canDrag = RenderHelper.isHovering(
            category.drag.x, category.drag.y, rectWidth, categoryRectHeight, mouseX, mouseY
        )

        // Right-click the category bar to collapse/expand its module list
        if (button == 1 && canDrag) {
            collapsed = !collapsed
            return
        }

        category.drag.onClick(mouseX, mouseY, button, canDrag)

        // Pass click to each module
        if (!collapsed) {
            visibleModuleRects().forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        // Stop drag
        category.drag.onRelease(state)

        // Pass release to each module
        visibleModuleRects().forEach { it.mouseReleased(mouseX, mouseY, state) }
    }

    private fun visibleModuleRects(): List<ModuleRect> {
        val modules = moduleRects.orEmpty()
        if (!search.active) return modules
        return modules.filter { search.matches(it.module) }
    }
}
