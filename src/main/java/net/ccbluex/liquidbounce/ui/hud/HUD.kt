/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud

import me.zywl.fdpclient.FDPClient.isInDev
import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.hud.element.Element
import net.ccbluex.liquidbounce.ui.hud.element.elements.*
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

open class HUD : MinecraftInstance() {

    val elements = mutableListOf<Element>()
    val notifications = mutableListOf<Notification>()

    companion object {

        val elements = ClassUtils.resolvePackage("${HUD::class.java.`package`.name}.element.elements", Element::class.java)
            .toTypedArray()

        /**
         * Create default HUD
         */
        fun createDefault(): HUD {
            val ClientName = Text(x = 7.63, y = 5.42)
            ClientName.displayString.set("%clientName%")
            ClientName.shadow.set(true)
            ClientName.getColor(1)
            ClientName.rectValue.set("None")
            ClientName.fontValue.set(Fonts.minecraftFont)
            ClientName.scale = 1.55F

            val Clientversion = Text(x = 88.44, y = 16.24)
            Clientversion.displayString.set("%clientVersion%")
            Clientversion.shadow.set(true)
            Clientversion.getColor(1)
            Clientversion.rectValue.set("None")
            Clientversion.fontValue.set(Fonts.font32)
            Clientversion.scale = 0.95F

            val Clientcredits = Text(x = 12.11, y = 26.79)
            if (isInDev) { Clientcredits.displayString.set("Development Build") } else { Clientcredits.displayString.set("by SkidderMC Org") }
            Clientcredits.shadow.set(true)
            Clientcredits.getColor(1)
            Clientcredits.rectValue.set("None")
            Clientcredits.fontValue.set(Fonts.minecraftFont)
            Clientcredits.scale = 0.90F

            return HUD()
                .addElement(ClientName)
                .addElement(Clientversion)
                .addElement(Clientcredits)
                .addElement(Notifications())
                .addElement(Arraylist())
                .addElement(ScoreboardElement())
                .addElement(Targets())
        }
    }

    /**
     * Render all elements
     */

    fun handleDamage(ent: EntityPlayer) {
        for (element in elements) {
            if (element.info.retrieveDamage)
                element.handleDamage(ent)
        }
    }

    fun render(designer: Boolean, partialTicks: Float) {
        for (element in elements) {
            GL11.glPushMatrix()
            GL11.glScalef(element.scale, element.scale, element.scale)
            GL11.glTranslated(element.renderX, element.renderY, 0.0)

            try {
                if (element.info.blur) {
                    element.drawBoarderBlur()
                }

                element.border = element.drawElement(partialTicks)

                if (designer) {
                    element.border?.draw()
                }
            } catch (ex: Exception) {
                ClientUtils.logError("Something went wrong while drawing ${element.name} element in HUD.", ex)
            }

            GL11.glPopMatrix()
        }
    }

    /**
     * Update all elements
     */
    fun update() {
        for (element in elements)
            element.updateElement()
    }

    /**
     * Handle mouse click
     */
    fun handleMouseClick(mouseX: Int, mouseY: Int, button: Int) {
        for (element in elements)
            element.handleMouseClick((mouseX / element.scale) - element.renderX, (mouseY / element.scale) -
                    element.renderY, button)

        if (button == 0) {
            for (element in elements.reversed()) {
                if (!element.isInBorder((mouseX / element.scale) - element.renderX,
                                (mouseY / element.scale) - element.renderY)) {
                    continue
                }

                element.drag = true
                elements.remove(element)
                elements.add(element)
                break
            }
        }
    }

    /**
     * Handle released mouse key
     */
    fun handleMouseReleased() {
        for (element in elements)
            element.drag = false
    }

    /**
     * Handle mouse move
     */
    fun handleMouseMove(mouseX: Int, mouseY: Int) {
        if (mc.currentScreen !is GuiHudDesigner) {
            return
        }

        val scaledResolution = StaticStorage.scaledResolution

        for (element in elements) {
            val scaledX = mouseX / element.scale
            val scaledY = mouseY / element.scale
            val prevMouseX = element.prevMouseX
            val prevMouseY = element.prevMouseY

            element.prevMouseX = scaledX
            element.prevMouseY = scaledY

            if (element.drag) {
                val moveX = scaledX - prevMouseX
                val moveY = scaledY - prevMouseY

                if (moveX == 0F && moveY == 0F) {
                    continue
                }

                val border = element.border ?: continue

                val minX = min(border.x, border.x2) + 1
                val minY = min(border.y, border.y2) + 1

                val maxX = max(border.x, border.x2) - 1
                val maxY = max(border.y, border.y2) - 1

                val width = scaledResolution.scaledWidth / element.scale
                val height = scaledResolution.scaledHeight / element.scale

                if ((element.renderX + minX + moveX >= 0.0 || moveX > 0) && (element.renderX + maxX + moveX <= width || moveX < 0)) {
                    element.renderX = moveX.toDouble()
                }
                if ((element.renderY + minY + moveY >= 0.0 || moveY > 0) && (element.renderY + maxY + moveY <= height || moveY < 0)) {
                    element.renderY = moveY.toDouble()
                }
            }
        }
    }

    /**
     * Handle incoming key
     */
    fun handleKey(c: Char, keyCode: Int) {
        for (element in elements)
            element.handleKey(c, keyCode)
    }

    /**
     * Add [element] to HUD
     */
    fun addElement(element: Element): HUD {
        elements.add(element)
        return this
    }

    /**
     * Remove [element] from HUD
     */
    fun removeElement(element: Element): HUD {
        element.destroyElement()
        elements.remove(element)
        return this
    }

    /**
     * Clear all elements
     */
    fun clearElements() {
        for (element in elements)
            element.destroyElement()

        elements.clear()
    }

    /**
     * Add [notification]
     */
    fun addNotification(notification: Notification) = elements.any { it is Notifications } && notifications.add(notification)

    /**
     * Remove [notification]
     */
    fun removeNotification(notification: Notification) = notifications.remove(notification)
}
