/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.ColorUtils.skyRainbow
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiIngame
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.text.SimpleDateFormat
import kotlin.math.max

@ModuleInfo(name = "Hotbar", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
object HotbarSettings : Module() {
    val hotbarValue = ListValue("HotbarMode", arrayOf("Minecraft", "Rounded", "Full", "LB", "Rise", "Gradient", "Overflow", "Glow", "Glowing", "Dock", "Exhi", "BlueIce", "Win11", "Bread"), "Rounded")
    val hotbarAlphaValue = IntegerValue("HotbarAlpha", 70, 0, 255)
    val hotbarEaseValue = BoolValue("HotbarEase", true)
    private val BlurValue = BoolValue("Blur", false)
    private val BlurAmount = FloatValue("BlurAmount", 10F, 1F, 100F).displayable { BlurValue.get() }
    private val ItemCountValue = BoolValue("ItemColorCount", false)
    val ItemFontValue = ListValue("ItemFont", arrayOf("MiSans", "Minecraft"), "Minecraft")
    private val hotbarAnimSpeedValue = IntegerValue("HotbarAnimSpeed", 10, 5, 20).displayable { hotbarEaseValue.get() }
    private val hotbarAnimTypeValue = EaseUtils.getEnumEasingList("HotbarAnimType").displayable { hotbarEaseValue.get() }
    private val hotbarAnimOrderValue = EaseUtils.getEnumEasingOrderList("HotbarAnimOrder").displayable { hotbarEaseValue.get() }
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = event.scaledResolution
        val i = sr.getScaledWidth() / 2
        val entityplayer = mc.renderViewEntity as EntityPlayer
        val itemX = sr.scaledWidth / 2 - 91 + getHotbarEasePos(entityplayer.inventory.currentItem * 20)
        val posInv = (91 - i + itemX).toFloat()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        when {
            hotbarValue.get() == "Win11" -> {
                if(BlurValue.get()) { BlurUtils.draw(0F, (sr.scaledHeight - 24).toFloat(), sr.scaledWidth.toFloat(), 24F, 100F) }
                // render the hotbar bg and items
                RenderUtils.drawRect(0f, (sr.scaledHeight - 24).toFloat(), sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, 160))
                RenderHelper.enableGUIStandardItemLighting(); for (item in 0..8) { HotbarItems(item, sr.scaledWidth / 2 - 90 + item * 20 + 13, sr.scaledHeight - 20); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + item * 20 + 13, sr.scaledHeight - 20, null as String?, item) }; RenderHelper.disableStandardItemLighting()
                RenderHelper.disableStandardItemLighting()
                // render windows logo and item pos indicator
                RenderUtils.originalRoundedRect(itemX + 19F, sr.scaledHeight - 3F, itemX + 25F,sr.scaledHeight - 1F, 1F,  Color(68, 129, 230).rgb)
                RenderUtils.drawImage( ResourceLocation("fdpclient/ui/hotbar/win11.png"), sr.scaledWidth / 2 - 95, sr.scaledHeight - 19, 14, 14)
                // date and time
                val dateFormat = SimpleDateFormat("dd/MM/yy")
                val date = dateFormat.format(System.currentTimeMillis())
                FontLoaders.F14.drawString(date, sr.scaledWidth - FontLoaders.F14.getStringWidth(date) - 4F,sr.scaledHeight - 9F,  Color(255, 255, 255).rgb)
                val hourFormat = SimpleDateFormat("HH:mm")
                val time = hourFormat.format(System.currentTimeMillis())
                FontLoaders.F14.drawString(time, sr.scaledWidth - FontLoaders.F14.getStringWidth(time) - 4F,sr.scaledHeight - 18F,  Color(255, 255, 255).rgb)
                // get distance date takes up
                val padding = max(FontLoaders.F14.getStringWidth(time), FontLoaders.F14.getStringWidth(date)) + 10
                // fake icons
                RenderUtils.drawImage(ResourceLocation("fdpclient/ui/hotbar/1.png"), (sr.scaledWidth - padding) - 10, sr.scaledHeight - 17, 10, 10)
                RenderUtils.drawImage(ResourceLocation("fdpclient/ui/hotbar/2.png"), (sr.scaledWidth - padding) - 28, sr.scaledHeight - 17, 10, 10)
                RenderUtils.drawImage(ResourceLocation("fdpclient/ui/hotbar/3.png"), (sr.scaledWidth - padding) - 46, sr.scaledHeight - 17, 10, 10)
                // lang idicator
                val loccode = mc.gameSettings.language.uppercase()
                val lang = loccode.substringBefore("_", "null")
                val region = loccode.substringAfter("_", "null")
                FontLoaders.F14.drawString(lang, (sr.scaledWidth - padding) - 62F,sr.scaledHeight - 17F,  Color(255, 255, 255).rgb)
                FontLoaders.F14.drawString(region, (sr.scaledWidth - padding) - 62F,sr.scaledHeight - 10F,  Color(255, 255, 255).rgb)
                // fake expand tray icon
                val paddingAfter = (max(
                    FontLoaders.F14.getStringWidth(time),
                    FontLoaders.F14.getStringWidth(date)
                ) + 10) + (max(FontLoaders.F14.getStringWidth(lang), FontLoaders.F14.getStringWidth(region)) + 2)
                RenderUtils.drawImage(ResourceLocation("fdpclient/ui/hotbar/up.png"), (sr.scaledWidth - paddingAfter) - 68, sr.scaledHeight - 17, 10, 10)
            }
            hotbarValue.get() ==  "Rise" -> {
                if(BlurValue.get() && BlurAmount.get() > 1F ) {
                    BlurUtils.draw((i - 91).toFloat(), (sr.scaledHeight - 22).toFloat(), 182F, 22F, BlurAmount.get())
                }
                RenderUtils.drawRect((i - 91).toFloat(), (sr.scaledHeight - 22).toFloat(), (i + 91).toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, hotbarAlphaValue.get()))
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 22).toFloat(), (itemX + 22).toFloat(), (sr.scaledHeight - 21).toFloat(), rainbow())
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 21).toFloat(), (itemX + 22).toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, hotbarAlphaValue.get()))
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "Full" -> {
                if(BlurValue.get() && BlurAmount.get() > 1F ) { BlurUtils.draw(0F, (sr.scaledHeight - 23).toFloat(), sr.scaledWidth.toFloat(), 23F, BlurAmount.get()) }
                RenderUtils.drawRect(0f, (sr.scaledHeight - 23).toFloat(), sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, hotbarAlphaValue.get()))
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 23).toFloat(), (itemX + 22).toFloat(), (sr.scaledHeight - 21).toFloat(), rainbow())
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 21).toFloat(), (itemX + 22).toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, hotbarAlphaValue.get()))
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "LB" -> {
                if(BlurValue.get() && BlurAmount.get() > 1F ) { BlurUtils.draw((i - 91).toFloat(), (sr.scaledHeight - 24).toFloat(), 182F, 24F, BlurAmount.get()) }
                RenderUtils.drawRect((i - 91).toFloat(), (sr.scaledHeight - 24).toFloat(), (i + 90).toFloat(), sr.scaledHeight.toFloat(), Int.MIN_VALUE)
                RenderUtils.drawRect(i - 91 - 1 + posInv + 1, (sr.scaledHeight - 24).toFloat(), i - 91 - 1 + posInv + 22, (sr.scaledHeight - 22 - 1 + 24).toFloat(), Int.MAX_VALUE)
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index)}
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "Overflow" -> {
                if(BlurValue.get() && BlurAmount.get() > 1F ) { BlurUtils.draw((i - 91).toFloat(), (sr.scaledHeight - 23).toFloat(), 182F, 23F, BlurAmount.get()) }
                GuiIngame.drawRect(i - 91, sr.scaledHeight - 23, i + 91, sr.scaledHeight - 1, Int.MIN_VALUE)
                GuiIngame.drawRect(itemX, sr.scaledHeight - 24, itemX + 22, sr.scaledHeight - 23, Color(90, 120, 255).rgb)
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "Gradient" -> {
                if(BlurValue.get() && BlurAmount.get() > 1F ) { BlurUtils.draw((i - 91).toFloat(), (sr.scaledHeight - 24).toFloat(), 182F, 28F, BlurAmount.get()) }
                RenderUtils.drawGradientSidewaysV((i - 91).toDouble(), (sr.scaledHeight - 24).toDouble(), (i + 91).toDouble(), sr.scaledHeight.toDouble(), Color(0, 0, 0).rgb, Color(0, 0, 0, 20).rgb)
                RenderUtils.drawGradientSidewaysV(itemX.toDouble(), (sr.scaledHeight - 24).toDouble(), (itemX + 22).toDouble(), sr.scaledHeight.toDouble(), Color(255, 255, 255, 190).rgb, Color(0, 0, 0, 20).rgb)
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "Glow" -> {
                if(BlurValue.get() && BlurAmount.get() > 1F ) { BlurUtils.draw((i - 91).toFloat(), (sr.scaledHeight - 22).toFloat(), 182F, 22F, BlurAmount.get()) }
                GuiIngame.drawRect(i - 91, sr.scaledHeight - 1, i + 91, sr.scaledHeight, Int.MAX_VALUE)
                RenderUtils.drawGradientSidewaysV(itemX.toDouble(), (sr.scaledHeight - 20).toDouble(), (itemX + 22).toDouble(), sr.scaledHeight.toDouble(), Color(255, 255, 255, 190).rgb, Color(0, 0, 0, 0).rgb)
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "BlueIce" -> {
                GuiIngame.drawRect(i - 91, sr.scaledHeight - 25, i + 91, sr.scaledHeight, Color(0, 0, 0, 230).rgb)
                RenderUtils.drawGradientSidewaysV(itemX.toDouble(), (sr.scaledHeight - 24).toDouble(), (itemX + 22).toDouble(), sr.scaledHeight.toDouble(), Color(0, 170, 255, 220).rgb, Color(0, 0, 0, 0).rgb)
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "Exhi" -> {
                RenderUtils.drawExhiRect((i - 91).toFloat(), (sr.scaledHeight - 22).toFloat(), (i + 91).toFloat(), sr.scaledHeight.toFloat(), 1f)
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 22).toFloat(), (itemX + 22).toFloat(), (sr.scaledHeight - 21).toFloat(), skyRainbow(1, 1f, 1f, 1.0))
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 21).toFloat(), (itemX + 22).toFloat(), sr.scaledHeight.toFloat(), Color(35, 35, 35))
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index)}
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() ==  "Bread" -> {
                RenderUtils.customRounded((i - 93).toFloat(), sr.scaledHeight.toFloat(), (i + 93).toFloat(), (sr.scaledHeight - 23).toFloat(), 5f, 5f, 0f, 0f, Int.MIN_VALUE)
                RenderUtils.originalRoundedRect((itemX + 6).toFloat(), (sr.scaledHeight - 2).toFloat(), (itemX + 16).toFloat(), (sr.scaledHeight - 0).toFloat(), 1f, Color(38, 139, 240).rgb)
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19, null as String?, Index) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "Rounded" -> {
                RenderUtils.originalRoundedRect((i - 91).toFloat(), (sr.scaledHeight - 2).toFloat(), (i + 91).toFloat(), (sr.scaledHeight - 22).toFloat(), 3f, Int.MIN_VALUE)
                RenderUtils.originalRoundedRect(i - 91 + posInv, (sr.scaledHeight - 2).toFloat(), i - 91 + posInv + 22, (sr.scaledHeight - 22).toFloat(), 3f, Int.MAX_VALUE)
                RenderHelper.enableGUIStandardItemLighting()
                for (j in 0..8) { HotbarItems(j, sr.scaledWidth / 2 - 90 + j * 20 + 2, sr.scaledHeight - 20); HotbarTextOverlay(sr.scaledWidth / 2 - 90 + j * 20 + 2, sr.scaledHeight - 20, null as String?, j) }
                RenderHelper.disableStandardItemLighting()
            }
            hotbarValue.get() == "Dock" -> {
                RenderUtils.originalRoundedRect((i - 91).toFloat(), (sr.scaledHeight - 1).toFloat(), (i + 91).toFloat(), (sr.scaledHeight - 21).toFloat(), 3f, Int.MIN_VALUE)
                RenderUtils.drawFilledCircle((itemX + 12).toDouble(), (sr.scaledHeight - 3).toDouble(), 1.4, Color(255, 255, 255).rgb, 255)
                RenderHelper.enableGUIStandardItemLighting()
                GlStateManager.pushMatrix()
                for (item in 0..8) {
                    var height = sr.scaledHeight - 19
                    if (item == entityplayer.inventory.currentItem) {
                        height = sr.scaledHeight - 23
                    } else if (item == entityplayer.inventory.currentItem + 1 || item == entityplayer.inventory.currentItem - 1) {
                        height = sr.scaledHeight - 21
                    } else if (item == entityplayer.inventory.currentItem + 2 || item == entityplayer.inventory.currentItem - 2) {
                        height = sr.scaledHeight - 20
                    }
                    HotbarItems(item, sr.scaledWidth / 2 - 90 + item * 20 + 2, height)
                    HotbarTextOverlay(sr.scaledWidth / 2 - 90 + item * 20 + 2, height, null as String?, item)
                }
                RenderHelper.disableStandardItemLighting()
                GlStateManager.popMatrix()
            }
            hotbarValue.get() ==  "Glowing" -> {
                // im bouta make your fps 0, thank me later!
                RenderUtils.drawRect((i - 91).toFloat(), (sr.scaledHeight - 22).toFloat(), (i + 91).toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, hotbarAlphaValue.get()))
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 21).toFloat(), (itemX + 22).toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, hotbarAlphaValue.get()))
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19) }
                RenderHelper.disableStandardItemLighting()
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 22).toFloat(), (itemX + 22).toFloat(), (sr.scaledHeight - 21).toFloat(), rainbow())
                BlurUtils.draw((i - 91).toFloat(), (sr.scaledHeight - 22).toFloat(), 182F, 22F, 20F)
                RenderUtils.drawRect(itemX.toFloat(), (sr.scaledHeight - 22).toFloat(), (itemX + 22).toFloat(), (sr.scaledHeight - 21).toFloat(), rainbow())
                RenderHelper.enableGUIStandardItemLighting()
                for (Index in 0..8) { HotbarItems(Index, sr.scaledWidth / 2 - 90 + Index * 20 + 2, sr.scaledHeight - 19) }
                RenderHelper.disableStandardItemLighting()
            }
        }
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
    }


    // Mojang ©2009-2022
    fun HotbarItems(index: Int, xPos: Int, yPos: Int) {
        val entityplayer = mc.renderViewEntity as EntityPlayer
        val itemstack = entityplayer.inventory.mainInventory[index]
        if (itemstack != null) {
            val f = itemstack.animationsToGo - mc.timer.renderPartialTicks
            if (f > 0.0f) {
                GlStateManager.pushMatrix()
                val f1 = 1.0f + f / 5.0f
                GlStateManager.translate((xPos + 8).toFloat(), (yPos + 12).toFloat(), 0.0f)
                GlStateManager.scale(1.0f / f1, (f1 + 1.0f) / 2.0f, 1.0f)
                GlStateManager.translate(-(xPos + 8).toFloat(), -(yPos + 12).toFloat(), 0.0f)
            }
            // render item
            mc.renderItem.renderItemAndEffectIntoGUI(itemstack, xPos, yPos)
            if (f > 0.0f) { GlStateManager.popMatrix() }
            // render overlay
            //mc.renderItem.renderItemOverlays(Fonts.font35, itemstack, xPos, yPos) // old method
            HotbarDurabilityOverlay(itemstack, xPos, yPos)
            var fontVal = mc.fontRendererObj
            when {
                ItemFontValue.get() == "MiSans" -> {
                    fontVal = Fonts.font35
                }
                ItemFontValue.get() == "Minecraft" -> {
                    fontVal = mc.fontRendererObj
                }
            }
            HotbarTextOverlay(xPos, yPos, null as String?, index)
        }
    }

    fun HotbarDurabilityOverlay(stack: ItemStack?, xPosition: Int, yPosition: Int) {
        if (stack != null) {
            if (stack.item.showDurabilityBar(stack)) {
                GlStateManager.disableTexture2D()
                val tessellator = Tessellator.getInstance()
                val worldrenderer = tessellator.worldRenderer
                barDraw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255)
                barDraw(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - Math.round(255.0 - stack.item.getDurabilityForDisplay(stack) * 255.0).toInt()) / 4, 64, 0, 255)
                barDraw(worldrenderer, xPosition + 2, yPosition + 13, Math.round(13.0 - stack.item.getDurabilityForDisplay(stack) * 13.0).toInt(), 1, 255 - Math.round(255.0 - stack.item.getDurabilityForDisplay(stack) * 255.0).toInt(), Math.round(255.0 - stack.item.getDurabilityForDisplay(stack) * 255.0).toInt(), 0, 255)
                GlStateManager.enableTexture2D()
            }
        }
    }

    fun HotbarTextOverlay(xPosition: Int, yPosition: Int, text: String?, index: Int) {
        val entityplayer = mc.renderViewEntity as EntityPlayer
        val stack = entityplayer.inventory.mainInventory[index]
        if (stack != null) {
            if (stack.stackSize != 1 || text != null) {
                var colour: Color? = null
                var s = text ?: stack.stackSize.toString()
                if(ItemCountValue.get()){
                    if (text == null && stack.stackSize < 1) { s = stack.stackSize.toString() }
                    if (stack.stackSize >= 46) { colour = Color.green } else if (stack.stackSize <= 45 && stack.stackSize > 20) { colour = Color.orange } else if (stack.stackSize <= 20) { colour = Color.red }
                } else {
                    if (text == null && stack.stackSize < 1) { s = EnumChatFormatting.RED.toString() + stack.stackSize.toString() }
                    colour = Color.white
                }
                var fontVal = mc.fontRendererObj
                when {
                    ItemFontValue.get() == "MiSans" -> {
                        fontVal = Fonts.font35
                    }
                    ItemFontValue.get() == "Minecraft" -> {
                        fontVal = mc.fontRendererObj
                    }
                }
                GlStateManager.disableLighting()
                GlStateManager.disableDepth()
                GlStateManager.disableBlend()
                fontVal.drawStringWithShadow(s, (xPosition + 19 - 2 - fontVal.getStringWidth(s)).toFloat(), (yPosition + 6 + 3).toFloat(), colour!!.rgb)
                GlStateManager.enableLighting()
                GlStateManager.enableDepth()
            }
        }
    }

    private fun barDraw(renderer: WorldRenderer, x: Int, y: Int, width: Int, height: Int, red: Int, green: Int, blue: Int, alpha: Int) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        renderer.pos((x + 0).toDouble(), (y + 0).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + 0).toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), (y + 0).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        Tessellator.getInstance().draw()
    }
    // end of mojang code

    // rise
    private var easeAnimation: Animation? = null
    private var easingValue = 0
        get() {
            if (easeAnimation != null) {
                field = easeAnimation!!.value.toInt()
                if (easeAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    easeAnimation = null
                }
            }
            return field
        }
        set(value) {
            var hotbarSpeed = hotbarAnimSpeedValue.get()
            if(hotbarValue.get() == "Dock"){ hotbarSpeed = 4}
            if (easeAnimation == null || (easeAnimation != null && easeAnimation!!.to != value.toDouble())) {
                easeAnimation = Animation(
                    EaseUtils.EnumEasingType.valueOf(hotbarAnimTypeValue.get()),
                    EaseUtils.EnumEasingOrder.valueOf(hotbarAnimOrderValue.get()),
                    field.toDouble(),
                    value.toDouble(),
                    hotbarSpeed * 30L
                ).start()
            }
        }

    fun getHotbarEasePos(x: Int): Int {
        if (!hotbarEaseValue.get()) return x
        easingValue = x
        return easingValue


    }
}