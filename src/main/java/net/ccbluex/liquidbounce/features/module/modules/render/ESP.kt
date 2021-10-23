/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.WorldToScreen
import net.ccbluex.liquidbounce.utils.render.shader.FramebufferShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector3f
import java.awt.Color

@ModuleInfo(name = "ESP", category = ModuleCategory.RENDER)
class ESP : Module() {
    val modeValue = ListValue(
        "Mode",
        arrayOf("Box", "OtherBox", "WireFrame", "2D", "Real2D", "CSGO", "Outline", "ShaderOutline", "ShaderGlow", "Jello"),
        "Jello"
    )
    private val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    val wireframeWidth = FloatValue("WireFrame-Width", 2f, 0.5f, 5f).displayable { modeValue.equals("WireFrame") }
    private val shaderOutlineRadius = FloatValue("ShaderOutline-Radius", 1.35f, 1f, 2f).displayable { modeValue.equals("ShaderOutline") }
    private val shaderGlowRadius = FloatValue("ShaderGlow-Radius", 2.3f, 2f, 3f).displayable { modeValue.equals("ShaderGlow") }
    private val CSGOWidth = FloatValue("CSGO-Width", 2f, 0.5f, 5f).displayable { modeValue.equals("CSGO") }
    private val colorRedValue = IntegerValue("R", 255, 0, 255).displayable { !colorTeam.get() }
    private val colorGreenValue = IntegerValue("G", 255, 0, 255).displayable { !colorTeam.get() }
    private val colorBlueValue = IntegerValue("B", 255, 0, 255).displayable { !colorTeam.get() }
    private val colorRainbow = BoolValue("Rainbow", false).displayable { !colorTeam.get() }
    private val colorTeam = BoolValue("Team", false)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val mode = modeValue.get()
        val mvMatrix = WorldToScreen.getMatrix(GL11.GL_MODELVIEW_MATRIX)
        val projectionMatrix = WorldToScreen.getMatrix(GL11.GL_PROJECTION_MATRIX)

        val need2dTranslate = mode.equals("csgo", ignoreCase = true) || mode.equals("real2d", ignoreCase = true)
        if (need2dTranslate) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.enableTexture2D()
            GlStateManager.depthMask(true)
            GL11.glLineWidth(1.0f)
        }

        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, true)) {
                val entityLiving = entity as EntityLivingBase
                val color = getColor(entityLiving)
                when (mode.lowercase()) {
                    "box", "otherbox" -> RenderUtils.drawEntityBox(entity, color, !mode.equals("otherbox", ignoreCase = true), true, outlineWidth.get())

                    "outline" -> RenderUtils.drawEntityBox(entity, color, true, false, outlineWidth.get())

                    "2d" -> {
                        val renderManager = mc.renderManager
                        val timer = mc.timer
                        val posX =
                            entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX
                        val posY =
                            entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY
                        val posZ =
                            entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
                        RenderUtils.draw2D(entityLiving, posX, posY, posZ, color.rgb, Color.BLACK.rgb)
                    }

                    "csgo", "real2d" -> {
                        val renderManager = mc.renderManager
                        val timer = mc.timer
                        val bb = entityLiving.entityBoundingBox
                            .offset(-entityLiving.posX, -entityLiving.posY, -entityLiving.posZ)
                            .offset(
                                entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks,
                                entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks,
                                entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks
                            )
                            .offset(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ)
                        val boxVertices = arrayOf(
                            doubleArrayOf(bb.minX, bb.minY, bb.minZ),
                            doubleArrayOf(bb.minX, bb.maxY, bb.minZ),
                            doubleArrayOf(bb.maxX, bb.maxY, bb.minZ),
                            doubleArrayOf(bb.maxX, bb.minY, bb.minZ),
                            doubleArrayOf(bb.minX, bb.minY, bb.maxZ),
                            doubleArrayOf(bb.minX, bb.maxY, bb.maxZ),
                            doubleArrayOf(bb.maxX, bb.maxY, bb.maxZ),
                            doubleArrayOf(bb.maxX, bb.minY, bb.maxZ)
                        )
                        var minX = mc.displayWidth.toFloat()
                        var minY = mc.displayHeight.toFloat()
                        var maxX = 0f
                        var maxY = 0f
                        for (boxVertex in boxVertices) {
                            val screenPos = WorldToScreen.worldToScreen(
                                Vector3f(
                                    boxVertex[0].toFloat(), boxVertex[1].toFloat(), boxVertex[2].toFloat()
                                ), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight
                            ) ?: continue
                            minX = screenPos.x.coerceAtMost(minX)
                            minY = screenPos.y.coerceAtMost(minY)
                            maxX = screenPos.x.coerceAtLeast(maxX)
                            maxY = screenPos.y.coerceAtLeast(maxY)
                        }

                        // out of screen
                        if (!(minX == mc.displayWidth.toFloat() || minY == mc.displayHeight.toFloat() || maxX == 0f || maxY == 0f)) {
                            if (mode.equals("csgo", ignoreCase = true)) {
                                val width = CSGOWidth.get() * ((maxY - minY) / 50)
                                RenderUtils.drawRect(minX - width, minY - width, minX, maxY, color)
                                RenderUtils.drawRect(maxX, minY - width, maxX + width, maxY + width, color)
                                RenderUtils.drawRect(minX - width, maxY, maxX, maxY + width, color)
                                RenderUtils.drawRect(minX - width, minY - width, maxX, minY, color)

                                // hp bar
                                val hpSize = (maxY + width - minY) * (entityLiving.health / entityLiving.maxHealth)
                                RenderUtils.drawRect(minX - width * 3, minY - width, minX - width * 2, maxY + width, Color.GRAY)
                                RenderUtils.drawRect(minX - width * 3, maxY - hpSize, minX - width * 2, maxY + width, ColorUtils.healthColor(entityLiving.health, entityLiving.maxHealth))
                            } else if (mode.equals("real2d", ignoreCase = true)) {
                                RenderUtils.drawRect(minX - 1, minY - 1, minX, maxY, color)
                                RenderUtils.drawRect(maxX, minY - 1, maxX + 1, maxY + 1, color)
                                RenderUtils.drawRect(minX - 1, maxY, maxX, maxY + 1, color)
                                RenderUtils.drawRect(minX - 1, minY - 1, maxX, minY, color)
                            }
                        }
                    }
                }
            }
        }

        if (need2dTranslate) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val mode = modeValue.get().lowercase()
        val partialTicks = event.partialTicks

        if (mode.equals("jello", ignoreCase = true)) {
            val hurtingEntities = ArrayList<EntityLivingBase>()
            var shader: FramebufferShader = GlowShader.GLOW_SHADER
            var radius = 3f
            var color = Color(120, 120, 120)
            var hurtColor = Color(120, 0, 0)
            var firstRun = true

            for (i in 0..1) {
                shader.startDraw(partialTicks)
                for (entity in mc.theWorld.loadedEntityList) {
                    if (EntityUtils.isSelected(entity, false)) {
                        val entityLivingBase = entity as EntityLivingBase
                        if (firstRun && entityLivingBase.hurtTime > 0) {
                            hurtingEntities.add(entityLivingBase)
                            continue
                        }
                        mc.renderManager.renderEntityStatic(entity, partialTicks, true)
                    }
                }
                shader.stopDraw(color, radius, 1f)

                // hurt
                if (hurtingEntities.size > 0) {
                    shader.startDraw(partialTicks)
                    for (entity in hurtingEntities) {
                        mc.renderManager.renderEntityStatic(entity, partialTicks, true)
                    }
                    shader.stopDraw(hurtColor, radius, 1f)
                }
                shader = OutlineShader.OUTLINE_SHADER
                radius = 1.2f
                color = Color(255, 255, 255, 170)
                hurtColor = Color(255, 0, 0, 170)
                firstRun = false
            }
            return
        }

        // normal shader esp
        val shader = when (mode) {
            "shaderoutline" -> OutlineShader.OUTLINE_SHADER
            "shaderglow" -> GlowShader.GLOW_SHADER
            else -> return
        }
        val radius = when (mode) {
            "shaderoutline" -> shaderOutlineRadius.get()
            "shaderglow" -> shaderGlowRadius.get()
            else -> 1f
        }

        // search
        val entityMap: MutableMap<Color, ArrayList<EntityLivingBase>> = HashMap()
        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, false)) {
                val entityLiving = entity as EntityLivingBase
                val color = getColor(entityLiving)
                if (!entityMap.containsKey(color)) {
                    entityMap[color] = ArrayList()
                }
                entityMap[color]!!.add(entityLiving)
            }
        }

        // draw
        for ((key, value) in entityMap) {
            shader.startDraw(partialTicks)
            for (entity in value) {
                mc.renderManager.renderEntityStatic(entity, partialTicks, true)
            }
            shader.stopDraw(key, radius, 1f)
        }
    }

    override val tag: String
        get() = modeValue.get()

    fun getColor(entity: Entity): Color {
        if (entity is EntityLivingBase) {
            if (entity.hurtTime > 0) return Color.RED
            if (EntityUtils.isFriend(entity)) return Color.BLUE
            if (colorTeam.get()) {
                val chars = entity.displayName.formattedText.toCharArray()
                var color = Int.MAX_VALUE
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size) continue
                    val index = getColorIndex(chars[i + 1])
                    if (index < 0 || index > 15) continue
                    color = ColorUtils.hexColors[index]
                    break
                }
                return Color(color)
            }
        }

        return if (colorRainbow.get()) ColorUtils.rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
    }
}