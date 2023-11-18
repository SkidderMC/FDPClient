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
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura.clickedBlocks
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.tileentity.*
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "StorageESP", category = ModuleCategory.RENDER)
object StorageESP : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "ShaderOutline", "ShaderGlow", "2D", "WireFrame"), "Outline")
    private val outlineWidthValue = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val chestValue = BoolValue("Chest", true)
    private val enderChestValue = BoolValue("EnderChest", true)
    private val furnaceValue = BoolValue("Furnace", true)
    private val dispenserValue = BoolValue("Dispenser", true)
    private val hopperValue = BoolValue("Hopper", true)

    private fun getColor(tileEntity: TileEntity): Color? {
        if (chestValue.get() && tileEntity is TileEntityChest && !clickedBlocks.contains(tileEntity.getPos())) return Color(0, 66, 255)
        if (enderChestValue.get() && tileEntity is TileEntityEnderChest && !clickedBlocks.contains(tileEntity.getPos())) return Color.MAGENTA
        if (furnaceValue.get() && tileEntity is TileEntityFurnace) return Color.BLACK
        if (dispenserValue.get() && tileEntity is TileEntityDispenser) return Color.BLACK
        if (hopperValue.get() && tileEntity is TileEntityHopper) return Color.GRAY

        return null
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        try {
            val mode = modeValue.get()
            val gamma = mc.gameSettings.gammaSetting
            mc.gameSettings.gammaSetting = 100000.0f

            for (tileEntity in mc.theWorld.loadedTileEntityList) {
                val color = getColor(tileEntity) ?: continue
                when (mode.lowercase()) {
                    "otherbox", "box" -> RenderUtils.drawBlockBox(tileEntity.pos, color, !mode.equals("otherbox", ignoreCase = true), true, outlineWidthValue.get())

                    "2d" -> RenderUtils.draw2D(tileEntity.pos, color.rgb, Color.BLACK.rgb)

                    "outline" -> RenderUtils.drawBlockBox(tileEntity.pos, color, true, false, outlineWidthValue.get())

                    "wireframe" -> {
                        GL11.glPushMatrix()
                        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
                        GL11.glDisable(GL11.GL_TEXTURE_2D)
                        GL11.glDisable(GL11.GL_LIGHTING)
                        GL11.glDisable(GL11.GL_DEPTH_TEST)
                        GL11.glEnable(GL11.GL_LINE_SMOOTH)
                        GL11.glEnable(GL11.GL_BLEND)
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                        RenderUtils.glColor(color)
                        GL11.glLineWidth(1.5f)
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                        GL11.glPopAttrib()
                        GL11.glPopMatrix()
                    }
                }
            }

            GL11.glColor4f(1F, 1F, 1F, 1F)
            mc.gameSettings.gammaSetting = gamma
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val mode = modeValue.get()
        val renderManager = mc.renderManager
        val partialTicks = event.partialTicks
        val shader = when (mode) {
            "shaderoutline" -> OutlineShader.OUTLINE_SHADER
            "shaderglow" -> GlowShader.GLOW_SHADER
            else -> return
        }

        val entityMap: MutableMap<Color, ArrayList<TileEntity>> = HashMap()

        // search
        for (tileEntity in mc.theWorld.loadedTileEntityList) {
            val color = getColor(tileEntity) ?: continue
            if (!entityMap.containsKey(color)) {
                entityMap[color] = ArrayList()
            }
            entityMap[color]!!.add(tileEntity)
        }

        // draw
        for ((key, value) in entityMap) {
            shader.startDraw(partialTicks)
            for (tileEntity in value) {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(tileEntity, tileEntity.pos.x - renderManager.renderPosX, tileEntity.pos.y - renderManager.renderPosY, tileEntity.pos.z - renderManager.renderPosZ, partialTicks)
            }
            shader.stopDraw(key, if (mode.equals("shaderglow", ignoreCase = true)) 2.5f else 1.5f, 1f)
        }
    }
}