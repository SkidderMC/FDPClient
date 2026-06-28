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
import net.ccbluex.liquidbounce.features.module.modules.other.ChestStealer
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.block.toVec
import net.ccbluex.liquidbounce.utils.render.RenderUtils.checkSetupFBO
import net.ccbluex.liquidbounce.utils.render.RenderUtils.draw2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawSelectionBoundingBox
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
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object StorageESP : Module("StorageESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    private val mode by
    ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "Glow", "2D", "WireFrame"), "Outline")

    private val glowSettings = GlowRenderSettings(isSupported = { mode == "Glow" }).also { addValues(it.values) }

    private val espColorMode by choices("ESP-Color", arrayOf("None", "Custom"), "None")
        .describe("Use type-based colors or a single custom color.")
    private val boxColor by color("Color", Color(255, 179, 72)) { espColorMode == "Custom" }
        .describe("Custom color used for storage highlights.")

    private val outline by boolean("Outline", true)
        .describe("Draw an outline around highlighted storages.")
    private val mergeAdjacent by boolean("MergeAdjacent", false)
        .describe("Merge touching storages into a single box.")
    private val tracers by boolean("Tracers", false)
        .describe("Draw lines from your view to each storage.")
    private val tracerThickness by float("TracerThickness", 2F, 1F..5F) { tracers }
        .describe("Line thickness of the tracers.")
    private val requiresChestStealer by boolean("RequiresChestStealer", false)
        .describe("Only render while ChestStealer is active.")

    private val renderFilters = RenderFilterSettings(100, 1..500).also { addValues(it.values) }

    private val chest by boolean("Chest", true)
        .describe("Highlight chests.")
    private val enderChest by boolean("EnderChest", true)
        .describe("Highlight ender chests.")
    private val furnace by boolean("Furnace", true)
        .describe("Highlight furnaces.")
    private val dispenser by boolean("Dispenser", true)
        .describe("Highlight dispensers.")
    private val hopper by boolean("Hopper", true)
        .describe("Highlight hoppers.")
    private val enchantmentTable by boolean("EnchantmentTable", false)
        .describe("Highlight enchantment tables.")
    private val brewingStand by boolean("BrewingStand", false)
        .describe("Highlight brewing stands.")
    private val sign by boolean("Sign", false)
        .describe("Highlight signs.")

    private fun getColor(tileEntity: TileEntity): Color? {
        return if (espColorMode == "Custom") {
            when {
                chest && tileEntity is TileEntityChest && tileEntity !in clickedTileEntities -> boxColor
                enderChest && tileEntity is TileEntityEnderChest && tileEntity !in clickedTileEntities -> boxColor
                furnace && tileEntity is TileEntityFurnace -> boxColor
                dispenser && tileEntity is TileEntityDispenser -> boxColor
                hopper && tileEntity is TileEntityHopper -> boxColor
                enchantmentTable && tileEntity is TileEntityEnchantmentTable -> boxColor
                brewingStand && tileEntity is TileEntityBrewingStand -> boxColor
                sign && tileEntity is TileEntitySign -> boxColor
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
        if (requiresChestStealer && !ChestStealer.state) {
            return@handler
        }

        try {
            if (mode == "Outline") {
                disableFastRender()
                checkSetupFBO()
            }

            val gamma = mc.gameSettings.gammaSetting

            mc.gameSettings.gammaSetting = 100000f

            if (tracers) {
                renderTracers()
            }

            if (mergeAdjacent && (mode == "Box" || mode == "OtherBox")) {
                renderMergedBoxes()
            }

            for (tileEntity in mc.theWorld.loadedTileEntityList) {
                val color = getColor(tileEntity) ?: continue

                if (!shouldRender(tileEntity)) {
                    continue
                }

                val withOutline = outline && mode != "OtherBox"

                if (mergeAdjacent && (mode == "Box" || mode == "OtherBox")) {
                    continue
                }

                if (!(tileEntity is TileEntityChest || tileEntity is TileEntityEnderChest)) {
                    drawBlockBox(tileEntity.pos, color, withOutline)

                    if (tileEntity !is TileEntityEnchantmentTable) {
                        continue
                    }
                }

                when (mode) {
                    "OtherBox", "Box" -> drawBlockBox(tileEntity.pos, color, withOutline)
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
                    "OtherBox", "Box" -> drawEntityBox(entity, minecartColor, outline && mode != "OtherBox")
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
            && (!renderFilters.onLook || mc.thePlayer.isLookingOn(tileEntity, renderFilters.maxAngleDifference.toDouble()))
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
            && (!renderFilters.onLook || mc.thePlayer.isLookingOn(entity, renderFilters.maxAngleDifference.toDouble()))
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

    private fun renderTracers() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val partial = mc.timer.renderPartialTicks
        val yaw = (player.prevRotationYaw..player.rotationYaw).lerpWith(partial)
        val pitch = (player.prevRotationPitch..player.rotationPitch).lerpWith(partial)
        val eyeVector = Vec3(0.0, 0.0, 1.0).rotatePitch(-pitch.toRadians()).rotateYaw(-yaw.toRadians())
        val renderPos = mc.renderManager.renderPos

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(tracerThickness)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glBegin(GL_LINES)

        for (tileEntity in world.loadedTileEntityList) {
            val color = getColor(tileEntity) ?: continue
            if (!shouldRender(tileEntity)) {
                continue
            }

            val (x, y, z) = tileEntity.pos.toVec() - renderPos
            glColor(color)
            glVertex3d(eyeVector.xCoord, player.getEyeHeight() + eyeVector.yCoord, eyeVector.zCoord)
            glVertex3d(x + 0.5, y + 0.5, z + 0.5)
        }

        for (entity in world.loadedEntityList) {
            if (entity !is EntityMinecartChest || !shouldRender(entity)) {
                continue
            }

            val (x, y, z) = entity.interpolatedPosition(entity.lastTickPos) - renderPos
            glColor(Color(0, 66, 255))
            glVertex3d(eyeVector.xCoord, player.getEyeHeight() + eyeVector.yCoord, eyeVector.zCoord)
            glVertex3d(x, y, z)
        }

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun renderMergedBoxes() {
        val world = mc.theWorld ?: return
        val renderPos = mc.renderManager.renderPos
        val withOutline = outline && mode != "OtherBox"

        val byColor = HashMap<Int, MutableSet<BlockPos>>()
        for (tileEntity in world.loadedTileEntityList) {
            val color = getColor(tileEntity) ?: continue
            if (!shouldRender(tileEntity)) {
                continue
            }
            byColor.getOrPut(color.rgb) { HashSet() }.add(tileEntity.pos)
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        for ((rgb, positions) in byColor) {
            val color = Color(rgb, true)

            for (cluster in mergeClusters(positions)) {
                var minX = Double.MAX_VALUE
                var minY = Double.MAX_VALUE
                var minZ = Double.MAX_VALUE
                var maxX = -Double.MAX_VALUE
                var maxY = -Double.MAX_VALUE
                var maxZ = -Double.MAX_VALUE

                for (pos in cluster) {
                    val (x, y, z) = pos.toVec() - renderPos
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (z < minZ) minZ = z
                    if (x + 1.0 > maxX) maxX = x + 1.0
                    if (y + 1.0 > maxY) maxY = y + 1.0
                    if (z + 1.0 > maxZ) maxZ = z + 1.0
                }

                val box = AxisAlignedBB.fromBounds(minX, minY, minZ, maxX, maxY, maxZ)

                glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (withOutline) 26 else 35)
                drawFilledBox(box)

                if (withOutline) {
                    glLineWidth(1f)
                    glEnable(GL_LINE_SMOOTH)
                    glColor(color)
                    drawSelectionBoundingBox(box)
                }
            }
        }

        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun mergeClusters(positions: Set<BlockPos>): List<List<BlockPos>> {
        val remaining = HashSet(positions)
        val clusters = mutableListOf<List<BlockPos>>()
        val neighbors = arrayOf(
            BlockPos(1, 0, 0), BlockPos(-1, 0, 0),
            BlockPos(0, 1, 0), BlockPos(0, -1, 0),
            BlockPos(0, 0, 1), BlockPos(0, 0, -1),
        )

        while (remaining.isNotEmpty()) {
            val start = remaining.first()
            remaining.remove(start)

            val cluster = mutableListOf(start)
            val queue = ArrayDeque<BlockPos>()
            queue.add(start)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                for (offset in neighbors) {
                    val neighbor = current.add(offset.x, offset.y, offset.z)
                    if (remaining.remove(neighbor)) {
                        cluster.add(neighbor)
                        queue.add(neighbor)
                    }
                }
            }

            clusters.add(cluster)
        }

        return clusters
    }
}
