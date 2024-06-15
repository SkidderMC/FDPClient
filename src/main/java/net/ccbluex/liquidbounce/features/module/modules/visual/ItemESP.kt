/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.Render2DEvent
import me.zywl.fdpclient.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue
import me.zywl.fdpclient.value.impl.IntegerValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "ItemESP", category = ModuleCategory.VISUAL)
object ItemESP : Module() {

    private val entityConvertedPointsMap: MutableMap<EntityItem, DoubleArray> = HashMap()
    private val nameTags = BoolValue("NameTag", false)
    private val itemCount = BoolValue("ItemCount", false).displayable { nameTags.get() }
    private val scaleValue = FloatValue("Scale", 1F, 1F, 4F).displayable { itemCount.get() }
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "LightBox", "ShaderGlow"), "Outline")
    private val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val colorRedValue = IntegerValue("R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 0, 0, 255)
    private val colorThemeClient = BoolValue("ClientTheme", true)

    private fun getColor(): Color {
        return if (colorThemeClient.get()) ClientTheme.getColor(1) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
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

        if (nameTags.get()) {
            for (item in mc.theWorld.getLoadedEntityList()) {
                if (item is EntityItem) {
                    val string = (item.entityItem.displayName + if (itemCount.get() && item.entityItem.stackSize > 1) " x${item.entityItem.stackSize}" else "")
                    GL11.glPushMatrix()
                    GL11.glTranslated(
                        item.lastTickPosX + (item.posX - item.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                        item.lastTickPosY + (item.posY - item.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY - 0.2,
                        item.lastTickPosZ + (item.posZ - item.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
                    )
                    GL11.glRotated((-mc.renderManager.playerViewY).toDouble(), 0.0, 1.0, 0.0)
                    RenderUtils.disableGlCap(GL11.GL_LIGHTING, GL11.GL_DEPTH_TEST)
                    GL11.glScalef(-scaleValue.get(), -scaleValue.get(), -scaleValue.get())
                    mc.fontRendererObj.drawString(string, -6F, -30F,
                        Color(255,255,255).rgb,true)
                    RenderUtils.enableGlCap(GL11.GL_BLEND)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    RenderUtils.resetCaps()

                    // Reset color
                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)

                    // Pop
                    GL11.glPopMatrix()
                }
            }
        }
    }

    private fun getIncremental(`val`: Double, inc: Double): Double {
        val one = 1.0 / inc
        return (`val` * one).roundToInt() / one
    }
}