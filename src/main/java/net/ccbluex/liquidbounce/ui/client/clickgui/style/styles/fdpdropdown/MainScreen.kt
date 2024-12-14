/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown

import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.features.module.Category
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
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class MainScreen(private val category: Category) : Screen {

    private val rectWidth = 110f
    private val categoryRectHeight = 18f
    var animation: Animation? = null
    private var moduleAnimMap = HashMap<ModuleRect, Animation>()
    var openingAnimation: Animation? = null
    private var moduleRects: MutableList<ModuleRect>? = null

    override fun initGui() {
        if (moduleRects == null) {
            moduleRects = mutableListOf<ModuleRect>().apply {
                Main.getModulesInCategory(category, moduleManager)
                    .sortedBy { it.name }
                    .forEach { module ->
                        val moduleRect = ModuleRect(module)
                        add(moduleRect)
                        moduleAnimMap[moduleRect] = DecelerateAnimation(250, 1.0)
                    }
            }
        }
        moduleRects?.forEach { it.initGui() }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        moduleRects?.forEach { it.keyTyped(typedChar, keyCode) }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int) {
        val animClamp = max(0.0, min(255.0, 255 * (animation?.output ?: 0.0))).toFloat()
        val alphaAnimation = animClamp.toInt()
        val categoryRectColor = Color(29, 29, 29, alphaAnimation).rgb
        val textColor = Color(255, 255, 255, alphaAnimation).rgb

        val x = category.drag.x
        val y = category.drag.y

        category.drag.onDraw(mouseX, mouseY)

        DrRenderUtils.drawRect2(x.toDouble(), y.toDouble(), rectWidth.toDouble(), categoryRectHeight.toDouble(), categoryRectColor)
        DrRenderUtils.setAlphaLimit(0f)
        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
            category.name,
            x + 5,
            y + Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.getMiddleOfBox(categoryRectHeight),
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

        Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.drawString(
            icon,
            x + rectWidth - (Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.stringWidth(icon) + 5),
            y + Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.getMiddleOfBox(categoryRectHeight),
            textColor
        )

        if (category.name.equals("Client", ignoreCase = true)) {
            Fonts.CheckFont.CheckFont_20.CheckFont_20.drawString(
                "b",
                x + rectWidth - (Fonts.CheckFont.CheckFont_20.CheckFont_20.stringWidth("b") + 5),
                y + Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.getMiddleOfBox(categoryRectHeight),
                textColor
            )
        }

        val allowedHeight = if (scrollMode == "Value") {
            clickHeight.toFloat()
        } else {
            val sr = ScaledResolution(mc)
            2 * sr.scaledHeight / 3f
        }
        Main.allowedClickGuiHeight = allowedHeight

        val hoveringMods = DrRenderUtils.isHovering(x, y + categoryRectHeight, rectWidth, allowedHeight, mouseX, mouseY)

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

        moduleRects?.forEach { moduleRect ->
            val animation = moduleAnimMap[moduleRect]
            animation?.setDirection(if (moduleRect.module.expanded) Direction.FORWARDS else Direction.BACKWARDS)

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

        if (hoveringMods) {
            category.scroll.onScroll(30)
            val hiddenHeight = ((count * 17) - allowedHeight).toFloat()
            category.scroll.maxScroll = max(0.0, hiddenHeight.toDouble()).toFloat()
        }

        StencilUtil.uninitStencilBuffer()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        val canDrag = DrRenderUtils.isHovering(category.drag.x, category.drag.y, rectWidth, categoryRectHeight, mouseX, mouseY)
        category.drag.onClick(mouseX, mouseY, button, canDrag)
        moduleRects?.forEach { it.mouseClicked(mouseX, mouseY, button) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        category.drag.onRelease(state)
        moduleRects?.forEach { it.mouseReleased(mouseX, mouseY, state) }
    }
}