/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Armor")
class Armor(
    x: Double = -8.0, 
    y: Double = 57.0, 
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {
    
    private val modeValue = ListValue("Alignment", arrayOf("Horizontal", "Vertical"), "Horizontal")
    private val colorModeValue = ListValue("Text-Color", arrayOf("Custom", "AnotherRainbow"), "Custom")
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val redValue = IntegerValue("Text-R", 255, 0, 255)
    private val greenValue = IntegerValue("Text-G", 255, 0, 255)
    private val blueValue = IntegerValue("Text-B", 255, 0, 255)
    private val newRainbowIndex = IntegerValue("NewRainbowOffset", 1, 1, 50)
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val speed = IntegerValue("AllSpeed", 0, 0, 400)

    /**
     * Draw element
     */

    override fun drawElement(): Border? {
        var x2 = 0
        if (mc.playerController.isNotCreative) {
            GL11.glPushMatrix()

            val renderItem = mc.renderItem
            val isInsideWater = mc.thePlayer.isInsideOfMaterial(Material.water)

            var x = 1
            var i = 0
            var y = if (isInsideWater) -10 else 0
            val colorMode = colorModeValue.get()
            val color = Color(redValue.get(), greenValue.get(), blueValue.get()).rgb
            val rainbow = colorMode.equals("Rainbow", ignoreCase = true)
            for (index in 0..3) {
                if(mc.thePlayer.inventory.armorInventory[index] != null)
                    x2 += 20
            }
            RenderUtils.drawRect(-2f, -4f, 2f + x2, 29f, Color(50, 50, 50, 60))

            for (index in 3 downTo 0) {
                val colorall = when {
                    rainbow -> 0
                    colorMode.equals("AnotherRainbow", ignoreCase = true) -> AnotherRainbow(index * speed.get(), saturationValue.get(), brightnessValue.get())
                    else -> color
                }
                val stack = mc.thePlayer.inventory.armorInventory[index] ?: continue
                RenderUtils.drawGradientSidewaysV(x.toDouble(), 0.0,x.toDouble() + 18 ,17.0,colorall,Color(140,140,140,40).rgb)
                Fonts.font32.drawStringWithShadow(((stack.maxDamage - stack.itemDamage)).toString(),x.toFloat() + 4f,20f,colorall)
                RenderUtils.drawRect(x.toFloat(),25f,x.toFloat() + 18f,26f,Color(140,140,140,220).rgb)
                RenderUtils.drawRect(x.toFloat(),25f,x.toFloat() + (18f * (stack.maxDamage - stack.itemDamage) / stack.maxDamage),26f,colorall)
                renderItem.renderItemIntoGUI(stack, x + 1, y)
                x += 20
                i += 1
            }
            GlStateManager.enableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.disableLighting()
            GlStateManager.disableCull()
            GL11.glPopMatrix()
        }
        return Border(-2f, -4f, 2f + 80, 29f)

    }
}
