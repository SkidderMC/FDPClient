/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.block.Block
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "BlockOverlay", category = ModuleCategory.RENDER)
class BlockOverlay : Module() {
    private val colorRedValue = IntegerValue("Red", 68, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorGreenValue = IntegerValue("Green", 117, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorAlphaValue = IntegerValue("Alpha", 100, 0, 255)
    private val colorRainbowValue = BoolValue("Rainbow", false)
    private val colorWidthValue = FloatValue("LineWidth", 2.0F, 0.0F, 10.0F)
    private val infoValue = BoolValue("Info", false)

    private val currentBlock: BlockPos?
        get() {
            val blockPos = mc.objectMouseOver?.blockPos ?: return null

            if (canBeClicked(blockPos) && mc.theWorld.worldBorder.contains(blockPos)) {
                return blockPos
            }

            return null
        }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val blockPos = currentBlock ?: return
        val block = mc.theWorld.getBlockState(blockPos).block ?: return
        val partialTicks = event.partialTicks
        val color = if (colorRainbowValue.get()) ColorUtils.rainbowWithAlpha(colorAlphaValue.get()) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())

        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        RenderUtils.glColor(color)
        GL11.glLineWidth(colorWidthValue.get())
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)

        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)

        val x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks
        val y = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks
        val z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks

        val axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-x, -y, -z)

        RenderUtils.drawSelectionBoundingBox(axisAlignedBB)
        RenderUtils.drawFilledBox(axisAlignedBB)
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (infoValue.get()) {
            val blockPos = currentBlock ?: return
            val block = getBlock(blockPos) ?: return

            val info = "${block.localizedName} §7ID: ${Block.getIdFromBlock(block)}"

            RenderUtils.drawBorderedRect(
                event.scaledResolution.scaledWidth / 2 - 2F,
                event.scaledResolution.scaledHeight / 2 + 5F,
                event.scaledResolution.scaledWidth / 2 + Fonts.font40.getStringWidth(info) + 2F,
                event.scaledResolution.scaledHeight / 2 + 16F,
                    3F, Color.BLACK.rgb, Color.BLACK.rgb
            )
            GlStateManager.resetColor()
            Fonts.font40.drawString(info, event.scaledResolution.scaledWidth / 2, event.scaledResolution.scaledHeight / 2 + 7,
                    Color.WHITE.rgb)
        }
    }
}
