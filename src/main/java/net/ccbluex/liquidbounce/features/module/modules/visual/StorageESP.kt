/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.other.ChestAura.clickedTileEntities
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.block.toVec
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.checkSetupFBO
import net.ccbluex.liquidbounce.utils.render.RenderUtils.draw2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.renderFive
import net.ccbluex.liquidbounce.utils.render.RenderUtils.renderFour
import net.ccbluex.liquidbounce.utils.render.RenderUtils.renderOne
import net.ccbluex.liquidbounce.utils.render.RenderUtils.renderThree
import net.ccbluex.liquidbounce.utils.render.RenderUtils.renderTwo
import net.ccbluex.liquidbounce.utils.render.RenderUtils.setColor
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.entity.item.EntityMinecartChest
import net.minecraft.tileentity.*
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.pow

object StorageESP : Module("StorageESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    private val mode by
    ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "Glow", "2D", "WireFrame"), "Outline")

    private val glowRenderScale by float("Glow-Renderscale", 1f, 0.5f..2f) { mode == "Glow" }
    private val glowRadius by int("Glow-Radius", 4, 1..5) { mode == "Glow" }
    private val glowFade by int("Glow-Fade", 10, 0..30) { mode == "Glow" }
    private val glowTargetAlpha by float("Glow-Target-Alpha", 0f, 0f..1f) { mode == "Glow" }

    private val espColorMode by choices("ESP-Color", arrayOf("None", "Custom"), "None")
    private val espColor = ColorSettingsInteger(this, "ESP")
    { espColorMode == "Custom" }.with(255, 179, 72)

    private val maxRenderDistance by int("MaxRenderDistance", 100, 1..500).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }

    private val onLook by boolean("OnLook", false)
    private val maxAngleDifference by float("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by boolean("ThruBlocks", true)

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val chest by boolean("Chest", true)
    private val enderChest by boolean("EnderChest", true)
    private val furnace by boolean("Furnace", true)
    private val dispenser by boolean("Dispenser", true)
    private val hopper by boolean("Hopper", true)
    private val enchantmentTable by boolean("EnchantmentTable", false)
    private val brewingStand by boolean("BrewingStand", false)
    private val sign by boolean("Sign", false)

    private fun getColor(tileEntity: TileEntity): Color? {
        return if (espColorMode == "Custom") {
            when {
                chest && tileEntity is TileEntityChest && tileEntity !in clickedTileEntities ->
                    Color(espColor.color().rgb)

                enderChest && tileEntity is TileEntityEnderChest && tileEntity !in clickedTileEntities ->
                    Color(espColor.color().rgb)

                furnace && tileEntity is TileEntityFurnace -> Color(espColor.color().rgb)
                dispenser && tileEntity is TileEntityDispenser -> Color(espColor.color().rgb)
                hopper && tileEntity is TileEntityHopper -> Color(espColor.color().rgb)
                enchantmentTable && tileEntity is TileEntityEnchantmentTable -> Color(espColor.color().rgb)
                brewingStand && tileEntity is TileEntityBrewingStand -> Color(espColor.color().rgb)
                sign && tileEntity is TileEntitySign -> Color(espColor.color().rgb)
                else -> null
            }
        } else {
            when {
                chest && tileEntity is TileEntityChest && tileEntity !in clickedTileEntities -> Color(0, 66, 255)
                enderChest && tileEntity is TileEntityEnderChest && tileEntity !in clickedTileEntities -> Color.MAGENTA
                furnace && tileEntity is TileEntityFurnace -> Color.BLACK
                dispenser && tileEntity is TileEntityDispenser -> Color.BLACK
                hopper && tileEntity is TileEntityHopper -> Color.GRAY
                enchantmentTable && tileEntity is TileEntityEnchantmentTable -> Color(166, 202, 240) // Light blue
                brewingStand && tileEntity is TileEntityBrewingStand -> Color.ORANGE
                sign && tileEntity is TileEntitySign -> Color.RED
                else -> null
            }
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        try {
            if (mode == "Outline") {
                disableFastRender()
                checkSetupFBO()
            }

            val gamma = mc.gameSettings.gammaSetting

            mc.gameSettings.gammaSetting = 100000f

            for (tileEntity in mc.theWorld.loadedTileEntityList) {
                val color = getColor(tileEntity) ?: continue

                val tileEntityPos = tileEntity.pos

                val distanceSquared = mc.thePlayer.getDistanceSq(
                    tileEntityPos.x.toDouble(),
                    tileEntityPos.y.toDouble(),
                    tileEntityPos.z.toDouble()
                )

                if (distanceSquared <= maxRenderDistanceSq) {
                    if (!(tileEntity is TileEntityChest || tileEntity is TileEntityEnderChest)) {
                        drawBlockBox(tileEntity.pos, color, mode != "OtherBox")

                        if (tileEntity !is TileEntityEnchantmentTable)
                            continue
                    }

                    if (onLook && !mc.thePlayer.isLookingOnEntity(tileEntity, maxAngleDifference.toDouble()))
                        continue

                    if (!thruBlocks && !isEntityHeightVisible(tileEntity)) continue

                    when (mode) {
                        "OtherBox", "Box" -> drawBlockBox(tileEntity.pos, color, mode != "OtherBox")
                        "2D" -> draw2D(tileEntity.pos, color.rgb, Color.BLACK.rgb)
                        "Outline" -> {
                            glColor(color)
                            renderOne(3F)
                            TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                            renderTwo()
                            TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                            renderThree()
                            TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                            renderFour(color)
                            TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                            renderFive()
                            setColor(Color.WHITE)
                        }

                        "WireFrame" -> {
                            glPushMatrix()
                            glPushAttrib(GL_ALL_ATTRIB_BITS)
                            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                            glDisable(GL_TEXTURE_2D)
                            glDisable(GL_LIGHTING)
                            glDisable(GL_DEPTH_TEST)
                            glEnable(GL_LINE_SMOOTH)
                            glEnable(GL_BLEND)
                            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                            glLineWidth(1.5f)
                            glColor(color)
                            TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                            glColor(color)
                            TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                            glPopAttrib()
                            glPopMatrix()
                        }
                    }
                }
            }
            for (entity in mc.theWorld.loadedEntityList) {
                val entityPos = entity.position

                val distanceSquared = mc.thePlayer.getDistanceSq(
                    entityPos.x.toDouble(),
                    entityPos.y.toDouble(),
                    entityPos.z.toDouble()
                )

                if (distanceSquared <= maxRenderDistanceSq) {
                    if (entity is EntityMinecartChest) {
                        if (onLook && !mc.thePlayer.isLookingOnEntity(entity, maxAngleDifference.toDouble()))
                            continue

                        if (!thruBlocks && !isEntityHeightVisible(entity)) continue

                        when (mode) {
                            "OtherBox", "Box" -> drawEntityBox(entity, Color(0, 66, 255), mode != "OtherBox")

                            "2d" -> draw2D(entity.position, Color(0, 66, 255).rgb, Color.BLACK.rgb)
                            "Outline" -> {
                                val entityShadow = mc.gameSettings.entityShadows
                                mc.gameSettings.entityShadows = false
                                glColor(Color(0, 66, 255))
                                renderOne(3f)
                                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                                renderTwo()
                                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                                renderThree()
                                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                                renderFour(Color(0, 66, 255))
                                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                                renderFive()
                                setColor(Color.WHITE)
                                mc.gameSettings.entityShadows = entityShadow
                            }

                            "WireFrame" -> {
                                val entityShadow = mc.gameSettings.entityShadows
                                mc.gameSettings.entityShadows = false
                                glPushMatrix()
                                glPushAttrib(GL_ALL_ATTRIB_BITS)
                                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                                glDisable(GL_TEXTURE_2D)
                                glDisable(GL_LIGHTING)
                                glDisable(GL_DEPTH_TEST)
                                glEnable(GL_LINE_SMOOTH)
                                glEnable(GL_BLEND)
                                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                                glColor(Color(0, 66, 255))
                                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                                glColor(Color(0, 66, 255))
                                glLineWidth(1.5f)
                                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                                glPopAttrib()
                                glPopMatrix()
                                mc.gameSettings.entityShadows = entityShadow
                            }
                        }
                    }
                }
            }

            glColor(Color(255, 255, 255, 255))
            mc.gameSettings.gammaSetting = gamma
        } catch (_: Exception) {
        }
    }


    val onRender2D = handler<Render2DEvent> { event ->
        if (mc.theWorld == null || mode != "Glow")
            return@handler

        val renderManager = mc.renderManager

        try {
            mc.theWorld.loadedTileEntityList
                .groupBy { getColor(it) }
                .forEach { (color, tileEntities) ->
                    color ?: return@forEach

                    GlowShader.startDraw(event.partialTicks, glowRenderScale)

                    for (entity in tileEntities) {
                        val pos = entity.pos.toVec()
                        val distanceSquared = mc.thePlayer.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord)

                        if (distanceSquared > maxRenderDistanceSq)
                            continue

                        if (onLook && !mc.thePlayer.isLookingOnEntity(entity, maxAngleDifference.toDouble()))
                            continue

                        if (!thruBlocks && !isEntityHeightVisible(entity))
                            continue

                        val (x, y, z) = pos - renderManager.renderPos

                        TileEntityRendererDispatcher.instance.renderTileEntityAt(entity, x, y, z, event.partialTicks)
                    }

                    GlowShader.stopDraw(color, glowRadius, glowFade, glowTargetAlpha)
                }
        } catch (ex: Exception) {
            LOGGER.error("An error occurred while rendering all storages for shader esp", ex)
        }
    }
}
