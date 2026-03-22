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
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.entity.item.EntityMinecartChest
import net.minecraft.tileentity.*
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object StorageESP : Module("StorageESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    private val mode by
    ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "Glow", "2D", "WireFrame"), "Outline")

    private val glowSettings = GlowRenderSettings(isSupported = { mode == "Glow" }).also { addValues(it.values) }

    private val espColorMode by choices("ESP-Color", arrayOf("None", "Custom"), "None")
    private val espColor = ColorSettingsInteger(this, "ESP")
    { espColorMode == "Custom" }.with(255, 179, 72)

    private val renderFilters = RenderFilterSettings(100, 1..500).also { addValues(it.values) }

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

                if (!shouldRender(tileEntity)) {
                    continue
                }

                if (!(tileEntity is TileEntityChest || tileEntity is TileEntityEnderChest)) {
                    drawBlockBox(tileEntity.pos, color, mode != "OtherBox")

                    if (tileEntity !is TileEntityEnchantmentTable) {
                        continue
                    }
                }

                when (mode) {
                    "OtherBox", "Box" -> drawBlockBox(tileEntity.pos, color, mode != "OtherBox")
                    "2D" -> draw2D(tileEntity.pos, color.rgb, Color.BLACK.rgb)
                    "Outline" -> renderOutline(color) {
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                    }

                    "WireFrame" -> renderWireFrame(color) {
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                    }
                }
            }
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity !is EntityMinecartChest || !shouldRender(entity)) {
                    continue
                }

                val minecartColor = Color(0, 66, 255)

                when (mode) {
                    "OtherBox", "Box" -> drawEntityBox(entity, minecartColor, mode != "OtherBox")
                    "2D" -> draw2D(entity.position, minecartColor.rgb, Color.BLACK.rgb)
                    "Outline" -> renderWithEntityShadowsDisabled {
                        renderOutline(minecartColor) {
                            mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        }
                    }

                    "WireFrame" -> renderWithEntityShadowsDisabled {
                        renderWireFrame(minecartColor) {
                            mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
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

                    renderGlow(event.partialTicks, color, glowSettings) {
                        for (entity in tileEntities) {
                            if (!shouldRender(entity)) {
                                continue
                            }

                            val (x, y, z) = entity.pos.toVec() - renderManager.renderPos
                            TileEntityRendererDispatcher.instance.renderTileEntityAt(entity, x, y, z, event.partialTicks)
                        }
                    }
                }
        } catch (ex: Exception) {
            LOGGER.error("An error occurred while rendering all storages for shader esp", ex)
        }
    }

    private fun shouldRender(tileEntity: TileEntity): Boolean {
        val distanceSquared = mc.thePlayer.getDistanceSq(
            tileEntity.pos.x.toDouble(),
            tileEntity.pos.y.toDouble(),
            tileEntity.pos.z.toDouble(),
        )

        return renderFilters.withinDistance(distanceSquared)
            && (!renderFilters.onLook || mc.thePlayer.isLookingOnEntity(tileEntity, renderFilters.maxAngleDifference.toDouble()))
            && (renderFilters.thruBlocks || isEntityHeightVisible(tileEntity))
    }

    private fun shouldRender(entity: EntityMinecartChest): Boolean {
        val entityPos = entity.position
        val distanceSquared = mc.thePlayer.getDistanceSq(
            entityPos.x.toDouble(),
            entityPos.y.toDouble(),
            entityPos.z.toDouble(),
        )

        return renderFilters.withinDistance(distanceSquared)
            && (!renderFilters.onLook || mc.thePlayer.isLookingOnEntity(entity, renderFilters.maxAngleDifference.toDouble()))
            && (renderFilters.thruBlocks || isEntityHeightVisible(entity))
    }

    private inline fun renderOutline(color: Color, renderer: () -> Unit) {
        glColor(color)
        renderOne(3F)
        renderer()
        renderTwo()
        renderer()
        renderThree()
        renderer()
        renderFour(color)
        renderer()
        renderFive()
        setColor(Color.WHITE)
    }

    private inline fun renderWireFrame(color: Color, renderer: () -> Unit) {
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
        renderer()
        glColor(color)
        renderer()
        glPopAttrib()
        glPopMatrix()
    }

    private inline fun renderWithEntityShadowsDisabled(renderer: () -> Unit) {
        val entityShadow = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false

        try {
            renderer()
        } finally {
            mc.gameSettings.entityShadows = entityShadow
        }
    }
}
