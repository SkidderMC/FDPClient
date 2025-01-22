/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects

class Drag(var x: Float, var y: Float) {
    private var startX = 0f
    private var startY = 0f
    private var dragging = false

    fun onDraw(mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX - startX
            y = mouseY - startY
        }
    }

    fun onClick(mouseX: Int, mouseY: Int, button: Int, canDrag: Boolean) {
        if (button == 0 && canDrag) {
            dragging = true
            startX = (mouseX - x).toInt().toFloat()
            startY = (mouseY - y).toInt().toFloat()
        }
    }

    fun onRelease(button: Int) {
        if (button == 0) dragging = false
    }
}