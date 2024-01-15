/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.ChestAura
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.tileentity.*
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "Chams", category = ModuleCategory.VISUAL)
object Chams : Module() {

    val targetsValue = BoolValue("Targets", true)
    val chestsValue = BoolValue("Chests", true)
    val itemsValue = BoolValue("Items", true)

    val localPlayerValue = BoolValue("LocalPlayer", true)
    val legacyMode = BoolValue("Legacy-Mode", false)
    val texturedValue = BoolValue("Textured", false).displayable { legacyMode.get() }
    val colorModeValue = ListValue("Color", arrayOf("Custom", "Slowly", "Fade"), "Custom").displayable { legacyMode.get() }
    val behindColorModeValue = ListValue("Behind-Color", arrayOf("Same", "Opposite", "Red"), "Red").displayable { legacyMode.get() }
    val redValue = IntegerValue("Red", 0, 0, 255).displayable { legacyMode.get() && (colorModeValue.equals("Custom") || colorModeValue.equals("Fade")) }
    val greenValue = IntegerValue("Green", 200, 0, 255).displayable { legacyMode.get() && (colorModeValue.equals("Custom") || colorModeValue.equals("Fade")) }
    val blueValue = IntegerValue("Blue", 0, 0, 255).displayable { legacyMode.get() && (colorModeValue.equals("Custom") || colorModeValue.equals("Fade")) }
    val alphaValue = IntegerValue("Alpha", 255, 0, 255).displayable { legacyMode.get() }
    val saturationValue = FloatValue("Saturation", 1F, 0F, 1F).displayable { legacyMode.get() && colorModeValue.equals("Slowly") }
    val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F).displayable { legacyMode.get() && colorModeValue.equals("Slowly") }

    private val storageESP = BoolValue("StorageESP", false)
    private val modeValue = ListValue("Storage-Mode", arrayOf("Box", "OtherBox", "Outline", "ShaderOutline", "ShaderGlow", "2D", "WireFrame"), "Outline").displayable  { storageESP.get() }
    private val outlineWidthValue = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val chestValue = BoolValue("Chest", false).displayable  { storageESP.get() }
    private val enderChestValue = BoolValue("EnderChest", false).displayable  { storageESP.get() }
    private val furnaceValue = BoolValue("Furnace", false).displayable  { storageESP.get() }
    private val dispenserValue = BoolValue("Dispenser", false).displayable  { storageESP.get() }
    private val hopperValue = BoolValue("Hopper", false).displayable  { storageESP.get() }

    private fun getColor(tileEntity: TileEntity): Color? {
        if (chestValue.get() && tileEntity is TileEntityChest && !ChestAura.clickedBlocks.contains(tileEntity.getPos())) return Color(0, 66, 255)
        if (enderChestValue.get() && tileEntity is TileEntityEnderChest && !ChestAura.clickedBlocks.contains(tileEntity.getPos())) return Color.MAGENTA
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