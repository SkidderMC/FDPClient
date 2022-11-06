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
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "ItemESP", category = ModuleCategory.RENDER)
class ItemESP : Module() {
    private val entityConvertedPointsMap: MutableMap<EntityItem, DoubleArray> = HashMap()
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "Exhibition", "LightBox", "ShaderOutline", "ShaderGlow"), "Box")
    private val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val colorRedValue = IntegerValue("R", 0, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorGreenValue = IntegerValue("G", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorBlueValue = IntegerValue("B", 0, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorRainbowValue = BoolValue("Rainbow", true)

    private fun getColor(): Color {
        return if (colorRainbowValue.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val color = getColor()
        for (entity in mc.theWorld.loadedEntityList) {
            if (!(entity is EntityItem || entity is EntityArrow)) continue
            when (modeValue.get().lowercase()) {
                "box" -> RenderUtils.drawEntityBox(entity, color, true, true, outlineWidth.get())
                "otherbox" -> RenderUtils.drawEntityBox(entity, color, false, true, outlineWidth.get())
                "outline" -> RenderUtils.drawEntityBox(entity, color, true, false, outlineWidth.get())
            }
        }

        if (modeValue.get().equals("LightBox", ignoreCase = true)) {
            for (o in mc.theWorld.loadedEntityList) {
                if (o !is EntityItem) continue
                val item = o
                val x = item.posX - mc.renderManager.renderPosX
                val y = item.posY + 0.5 - mc.renderManager.renderPosY
                val z = item.posZ - mc.renderManager.renderPosZ
                GL11.glEnable(3042)
                GL11.glLineWidth(2.0f)
                GL11.glColor4f(1f, 1f, 1f, .75f)
                GL11.glDisable(3553)
                GL11.glDisable(2929)
                GL11.glDepthMask(false)
                RenderUtils.drawOutlinedBoundingBox(AxisAlignedBB(x - .2, y - 0.3, z - .2, x + .2, y - 0.4, z + .2))
                GL11.glColor4f(1f, 1f, 1f, 0.15f)
                RenderUtils.drawBoundingBox(AxisAlignedBB(x - .2, y - 0.3, z - .2, x + .2, y - 0.4, z + .2))
                GL11.glEnable(3553)
                GL11.glEnable(2929)
                GL11.glDepthMask(true)
                GL11.glDisable(3042)
            }
        }
        if (modeValue.get().equals("Exhibition", ignoreCase = true)) {
            entityConvertedPointsMap.clear()
            val pTicks = mc.timer.renderPartialTicks
            for (e2 in mc.theWorld.getLoadedEntityList()) {
                if (e2 is EntityItem) {
                    val ent = e2
                    var x = ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks
                    -mc.renderManager.viewerPosX + 0.36
                    var y = (ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * pTicks
                            - mc.renderManager.viewerPosY)
                    var z = ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks
                    -mc.renderManager.viewerPosZ + 0.36
                    val topY: Double
                    y = y + (ent.height + 0.15).also { topY = it }
                    val convertedPoints = RenderUtils.convertTo2D(x, y, z)
                    val convertedPoints2 = RenderUtils.convertTo2D(x - 0.36, y, z - 0.36)
                    val xd = 0.0
                    assert(convertedPoints2 != null)
                    if (convertedPoints2!![2] < 0.0 || convertedPoints2[2] >= 1.0) continue
                    x = (ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks - mc.renderManager.viewerPosX
                            - 0.36)
                    z = (ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks - mc.renderManager.viewerPosZ
                            - 0.36)
                    val convertedPointsBottom = RenderUtils.convertTo2D(x, y, z)
                    y = (ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * pTicks - mc.renderManager.viewerPosY
                            - 0.05)
                    val convertedPointsx = RenderUtils.convertTo2D(x, y, z)
                    x = (ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks - mc.renderManager.viewerPosX
                            - 0.36)
                    z = (ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks - mc.renderManager.viewerPosZ
                            + 0.36)
                    val convertedPointsTop1 = RenderUtils.convertTo2D(x, topY, z)
                    val convertedPointsx2 = RenderUtils.convertTo2D(x, y, z)
                    x = (ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks - mc.renderManager.viewerPosX
                            + 0.36)
                    z = (ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks - mc.renderManager.viewerPosZ
                            + 0.36)
                    val convertedPointsz = RenderUtils.convertTo2D(x, y, z)
                    x = (ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks - mc.renderManager.viewerPosX
                            + 0.36)
                    z = (ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks - mc.renderManager.viewerPosZ
                            - 0.36)
                    val convertedPointsTop2 = RenderUtils.convertTo2D(x, topY, z)
                    val convertedPointsz2 = RenderUtils.convertTo2D(x, y, z)
                    assert(convertedPoints != null)
                    assert(convertedPointsx != null)
                    assert(convertedPointsTop1 != null)
                    assert(convertedPointsTop2 != null)
                    assert(convertedPointsz2 != null)
                    assert(convertedPointsz != null)
                    assert(convertedPointsx2 != null)
                    assert(convertedPointsBottom != null)
                    entityConvertedPointsMap[ent] = doubleArrayOf(
                        convertedPoints!![0],
                        convertedPoints[1], xd,
                        convertedPoints[2],
                        convertedPointsBottom!![0],
                        convertedPointsBottom[1],
                        convertedPointsBottom[2],
                        convertedPointsx!![0],
                        convertedPointsx[1],
                        convertedPointsx[2],
                        convertedPointsx2!![0],
                        convertedPointsx2[1],
                        convertedPointsx2[2],
                        convertedPointsz!![0],
                        convertedPointsz[1],
                        convertedPointsz[2], convertedPointsz2!![0], convertedPointsz2[1], convertedPointsz2[2],
                        convertedPointsTop1!![0], convertedPointsTop1[1], convertedPointsTop1[2],
                        convertedPointsTop2!![0], convertedPointsTop2[1], convertedPointsTop2[2]
                    )
                }
            }
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (modeValue.get().equals("Exhibition", ignoreCase = true)) {
            GlStateManager.pushMatrix()
            for (ent in entityConvertedPointsMap.keys) {
                val renderPositions = entityConvertedPointsMap[ent]
                val renderPositionsBottom = doubleArrayOf(
                    renderPositions!![4], renderPositions[5],
                    renderPositions[6]
                )
                val renderPositionsX = doubleArrayOf(
                    renderPositions[7], renderPositions[8],
                    renderPositions[9]
                )
                val renderPositionsX2 = doubleArrayOf(
                    renderPositions[10], renderPositions[11],
                    renderPositions[12]
                )
                val renderPositionsZ = doubleArrayOf(
                    renderPositions[13], renderPositions[14],
                    renderPositions[15]
                )
                val renderPositionsZ2 = doubleArrayOf(
                    renderPositions[16], renderPositions[17],
                    renderPositions[18]
                )
                val renderPositionsTop1 = doubleArrayOf(
                    renderPositions[19], renderPositions[20],
                    renderPositions[21]
                )
                val renderPositionsTop2 = doubleArrayOf(
                    renderPositions[22], renderPositions[23],
                    renderPositions[24]
                )
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.5, 0.5, 0.5)
                if (mc.theWorld.loadedEntityList.contains(ent)) {
                    try {
                        val xValues = doubleArrayOf(
                            renderPositions[0], renderPositionsBottom[0], renderPositionsX[0],
                            renderPositionsX2[0], renderPositionsZ[0], renderPositionsZ2[0], renderPositionsTop1[0],
                            renderPositionsTop2[0]
                        )
                        val yValues = doubleArrayOf(
                            renderPositions[1], renderPositionsBottom[1], renderPositionsX[1],
                            renderPositionsX2[1], renderPositionsZ[1], renderPositionsZ2[1], renderPositionsTop1[1],
                            renderPositionsTop2[1]
                        )
                        var x = renderPositions[0]
                        var y = renderPositions[1]
                        var endx = renderPositionsBottom[0]
                        var endy = renderPositionsBottom[1]
                        var array: DoubleArray
                        val length = xValues.also { array = it }.size
                        var j = 0
                        while (j < length) {
                            val bdubs = array[j]
                            if (bdubs < x) {
                                x = bdubs
                            }
                            ++j
                        }
                        var array2: DoubleArray
                        val length2 = xValues.also { array2 = it }.size
                        var k = 0
                        while (k < length2) {
                            val bdubs = array2[k]
                            if (bdubs > endx) {
                                endx = bdubs
                            }
                            ++k
                        }
                        var array3: DoubleArray
                        val length3 = yValues.also { array3 = it }.size
                        var l = 0
                        while (l < length3) {
                            val bdubs = array3[l]
                            if (bdubs < y) {
                                y = bdubs
                            }
                            ++l
                        }
                        var array4: DoubleArray
                        val length4 = yValues.also { array4 = it }.size
                        var n = 0
                        while (n < length4) {
                            val bdubs = array4[n]
                            if (bdubs > endy) {
                                endy = bdubs
                            }
                            ++n
                        }
                        RenderUtils.rectangleBordered(
                            x + 0.5,
                            y + 0.5,
                            endx - 0.5,
                            endy - 0.5,
                            1.0,
                            Colors.getColor(0, 0, 0, 0),
                            Color(255, 255, 255).rgb
                        )
                        RenderUtils.rectangleBordered(
                            x - 0.5,
                            y - 0.5,
                            endx + 0.5,
                            endy + 0.5,
                            1.0,
                            Colors.getColor(0, 0),
                            Colors.getColor(0, 150)
                        )
                        RenderUtils.rectangleBordered(
                            x + 1.5,
                            y + 1.5,
                            endx - 1.5,
                            endy - 1.5,
                            1.0,
                            Colors.getColor(0, 0),
                            Colors.getColor(0, 150)
                        )
                        val health = 20f
                        val progress = health / 20f
                        val difference = y - endy + 0.5
                        RenderUtils.rectangleBordered(
                            x - 6.5, y - 0.5, x - 2.5,
                            endy, 1.0, Color(30, 255, 30).rgb,
                            Colors.getColor(0, 150)
                        )
                        //RenderUtils.rectangle((x - 5.5), (endy - 1.0), (x - 3.5),
                        //         healthLocation,  customColor.getRGB());
                        RenderUtils.rectangle(x - 5.5, endy - 1.0, x - 3.5, endy + difference, Color(30, 255, 30).rgb)
                        if (-difference > 50.0) {
                            for (i in 1..9) {
                                val dThing = difference / 10.0 * i
                                RenderUtils.rectangle(
                                    x - 6.5, endy - 0.5 + dThing,
                                    x - 2.5, endy - 0.5 + dThing - 1.0,
                                    Colors.getColor(0)
                                )
                            }
                        }
                        if (getIncremental((progress * 100.0f).toDouble(), 1.0).toInt() <= 40) {
                            GlStateManager.pushMatrix()
                            GlStateManager.scale(2.0f, 2.0f, 2.0f)
                            GlStateManager.popMatrix()
                        }
                    } catch (ignored: Exception) {
                    }
                }
                GlStateManager.popMatrix()
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            }
            GL11.glScalef(1.0f, 1.0f, 1.0f)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GlStateManager.popMatrix()
            RenderUtils.rectangle(0.0, 0.0, 0.0, 0.0, -1)
        }
        if (modeValue.get().equals("ShaderOutline", ignoreCase = true)) {
            OutlineShader.OUTLINE_SHADER.startDraw(event.partialTicks)
            try {
                for (entity in mc.theWorld.loadedEntityList) {
                    if (!(entity is EntityItem || entity is EntityArrow)) continue
                    mc.renderManager.renderEntityStatic(entity, event.partialTicks, true)
                }
            } catch (ex: Exception) {
                alert("An error occurred while rendering all item entities for shader esp")
            }
            OutlineShader.OUTLINE_SHADER.stopDraw(
                if (colorRainbowValue.get()) rainbow() else Color(
                    colorRedValue.get(),
                    colorGreenValue.get(),
                    colorBlueValue.get()
                ), 1f, 1f
            )
        }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val shader = (if (modeValue.equals("shaderoutline")) OutlineShader.OUTLINE_SHADER else if (modeValue.equals("shaderglow")) GlowShader.GLOW_SHADER else null)
            ?: return
        val partialTicks = event.partialTicks

        shader.startDraw(partialTicks)

        for (entity in mc.theWorld.loadedEntityList) {
            if (!(entity is EntityItem || entity is EntityArrow)) continue
            mc.renderManager.renderEntityStatic(entity, event.partialTicks, true)
        }

        shader.stopDraw(getColor(), outlineWidth.get(), 1f)
    }
    }

    fun getIncremental(`val`: Double, inc: Double): Double {
        val one = 1.0 / inc
        return (`val` * one).roundToInt() / one
    }
}