/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isBlockBBValid
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColorWithAlpha
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.animSmooth
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.onPlayerRightClick
import net.ccbluex.liquidbounce.utils.extensions.rotation
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockDamageText
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.faceBlock
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.performRaytrace
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.block.Block
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.*
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color

object Fucker : Module("Fucker", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    /**
     * SETTINGS
     */
    private val hypixel by boolean("Hypixel", false)

    private val block by block("Block", 26)
    private val throughWalls by choices("ThroughWalls", arrayOf("None", "Raycast", "Around"), "None") { !hypixel }
    private val range by float("Range", 5F, 1F..7F)

    private val action by choices("Action", arrayOf("Destroy", "Use"), "Destroy")
    private val surroundings by boolean("Surroundings", true) { !hypixel }
    private val instant by boolean("Instant", false) { (action == "Destroy" || surroundings) && !hypixel }

    private val switch by int("SwitchDelay", 250, 0..1000)
    private val swing by boolean("Swing", true)
    val noHit by boolean("NoHit", false)

    private val options = RotationSettings(this).withoutKeepRotation()

    private val blockProgress by boolean("BlockProgress", true).subjective()

    private val scale by float("Scale", 2F, 1F..6F) { blockProgress }.subjective()
    private val font by font("Font", Fonts.fontSemibold40) { blockProgress }.subjective()
    private val fontShadow by boolean("Shadow", true) { blockProgress }.subjective()

    private val color by color("Color", Color(200, 100, 0)) { blockProgress }.subjective()

    private val ignoreOwnBed by boolean("IgnoreOwnBed", true)
    private val ownBedDist by int("MaxBedDistance", 16, 1..32) { ignoreOwnBed }

    private val renderPos by boolean("Render-Pos", false)
    private val clientTheme by boolean("RenderPos Color", true) { renderPos }
    private val posProcess by boolean("PosProcess", false) { renderPos }

    private val posOutline by boolean("PosOutline", false)
    /**
     * VALUES
     */
    var pos: BlockPos? = null
        private set
    private var obstructingPos: BlockPos? = null
    private var spawnLocation: Vec3? = null
    private var oldPos: BlockPos? = null
    private var blockHitDelay = 0
    private val switchTimer = MSTimer()
    var currentDamage = 0F
    var isOwnBed = false
    private var damageAnim = 0F

    // Surroundings
    private var areSurroundings = false

    override fun onToggle(state: Boolean) {
        if (pos != null && !mc.thePlayer.capabilities.isCreativeMode) {
            sendPacket(C07PacketPlayerDigging(ABORT_DESTROY_BLOCK, pos, EnumFacing.DOWN))
        }

        currentDamage = 0F
        pos = null
        obstructingPos = null
        areSurroundings = false
        isOwnBed = false
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mc.thePlayer == null || mc.theWorld == null) return@handler

        val packet = event.packet
        if (packet is S08PacketPlayerPosLook) {
            spawnLocation = Vec3(packet.x, packet.y, packet.z)
        }
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (noHit && KillAura.handleEvents() && KillAura.target != null) return@handler

        val targetId = block

        if (pos == null || pos!!.block!!.id != targetId || getCenterDistance(pos!!) > range) {
            pos = find(targetId)
            obstructingPos = null
        }

        // Reset current breaking when there is no target block
        if (pos == null) {
            currentDamage = 0F
            areSurroundings = false
            isOwnBed = false
            obstructingPos = null
            return@handler
        }

        var currentPos = pos ?: return@handler

        // Check if it is the player's own bed
        isOwnBed = ignoreOwnBed && isBedNearSpawn(currentPos)
        if (isOwnBed) {
            obstructingPos = null
            return@handler
        }

        if (surroundings || hypixel) {
            if (hypixel && obstructingPos == null) {
                val abovePos = currentPos.up()
                if (abovePos.block != Blocks.air && isHittable(abovePos)) {
                    obstructingPos = abovePos
                    currentPos = obstructingPos!!
                }
            } else if (surroundings && obstructingPos == null) {
                val eyes = player.eyes
                val spotToBed = faceBlock(currentPos) ?: return@handler
                val blockPos = world.rayTraceBlocks(eyes, spotToBed.vec, false, false, true)?.blockPos
                if (blockPos != null && blockPos.block != Blocks.air && blockPos != currentPos) {
                    obstructingPos = blockPos
                    currentPos = obstructingPos!!
                }
            } else if (obstructingPos != null) {
                currentPos = obstructingPos!!
                if (surroundings) {
                    val eyes = player.eyes
                    val spotToObstruction = faceBlock(currentPos) ?: return@handler
                    val rayTraceResultToObstruction = world.rayTraceBlocks(eyes, spotToObstruction.vec, false, false, true)
                    // If a new block is blocking it, reset and re-evaluate next cycle.
                    if (rayTraceResultToObstruction?.blockPos != currentPos &&
                        rayTraceResultToObstruction?.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK
                    ) {
                        obstructingPos = null
                        return@handler
                    }
                    val spotToBed = faceBlock(pos!!) ?: return@handler
                    val rayTraceToBed = world.rayTraceBlocks(eyes, spotToBed.vec, false, false, true)
                    // Target bed if it's open
                    if (rayTraceToBed?.blockPos == pos &&
                        rayTraceToBed.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK
                    ) {
                        obstructingPos = null
                        currentPos = pos!!
                    }
                }
            }
        }

        val spot = faceBlock(currentPos) ?: return@handler

        // Reset switch timer when position changes
        if (oldPos != null && oldPos != currentPos) {
            currentDamage = 0F
            switchTimer.reset()
        }
        oldPos = currentPos

        if (!switchTimer.hasTimePassed(switch)) return@handler

        // Block hit delay
        if (blockHitDelay > 0) {
            blockHitDelay--
            return@handler
        }

        // Face block
        if (options.rotationsActive) {
            setTargetRotation(spot.rotation, options = options)
        }
    }

    /**
     * Check if the bed at the given position is near the spawn location
     */
    private fun isBedNearSpawn(currentPos: BlockPos): Boolean {
        if (currentPos.block != Block.getBlockById(block) || spawnLocation == null) {
            return false
        }
        return spawnLocation!!.squareDistanceTo(currentPos.center) < ownBedDist * ownBedDist
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler
        val controller = mc.playerController ?: return@handler

        var currentPos = pos ?: return@handler
        if (obstructingPos != null) {
            currentPos = obstructingPos!!
        }

        val targetRotation = if (options.rotationsActive) {
            currentRotation ?: player.rotation
        } else {
            toRotation(currentPos.center, false).fixedSensitivity()
        }

        val raytrace = performRaytrace(currentPos, targetRotation, range) ?: return@handler

        when {
            // Destroy block
            action == "Destroy" || areSurroundings -> {
                isOwnBed = ignoreOwnBed && isBedNearSpawn(currentPos)
                if (isOwnBed) {
                    obstructingPos = null
                    return@handler
                }

                EventManager.call(ClickBlockEvent(currentPos, raytrace.sideHit))

                if (instant && !hypixel) {
                    // CivBreak style block breaking
                    sendPacket(C07PacketPlayerDigging(START_DESTROY_BLOCK, currentPos, raytrace.sideHit))
                    if (swing) player.swingItem()
                    sendPacket(C07PacketPlayerDigging(STOP_DESTROY_BLOCK, currentPos, raytrace.sideHit))
                    clearTarget(currentPos)
                    return@handler
                }

                val block = currentPos.block ?: return@handler

                if (currentDamage == 0F) {
                    // Prevent flagging FastBreak
                    sendPacket(C07PacketPlayerDigging(STOP_DESTROY_BLOCK, currentPos, raytrace.sideHit))
                    nextTick {
                        sendPacket(C07PacketPlayerDigging(START_DESTROY_BLOCK, currentPos, raytrace.sideHit))
                    }
                    if (player.capabilities.isCreativeMode ||
                        block.getPlayerRelativeBlockHardness(player, world, currentPos) >= 1f
                    ) {
                        if (swing) player.swingItem()
                        controller.onPlayerDestroyBlock(currentPos, raytrace.sideHit)
                        clearTarget(currentPos)
                        return@handler
                    }
                }

                if (swing) player.swingItem()
                currentDamage += block.getPlayerRelativeBlockHardness(player, world, currentPos)
                world.sendBlockBreakProgress(player.entityId, currentPos, (currentDamage * 10F).toInt() - 1)

                if (currentDamage >= 1F) {
                    sendPacket(C07PacketPlayerDigging(STOP_DESTROY_BLOCK, currentPos, raytrace.sideHit))
                    controller.onPlayerDestroyBlock(currentPos, raytrace.sideHit)
                    blockHitDelay = 4
                    clearTarget(currentPos)
                }
            }
            // Use block
            action == "Use" -> {
                if (player.onPlayerRightClick(currentPos, raytrace.sideHit, raytrace.hitVec, player.heldItem)) {
                    if (swing) player.swingItem() else sendPacket(C0APacketAnimation())
                    blockHitDelay = 4
                    clearTarget(currentPos)
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        val renderPos = obstructingPos ?: pos
        val posToDraw = renderPos ?: return@handler

        isOwnBed = ignoreOwnBed && isBedNearSpawn(posToDraw)
        if (mc.thePlayer == null || isOwnBed) return@handler

        if (block.blockById == Blocks.air) return@handler

        if (blockProgress) {
            posToDraw.drawBlockDamageText(
                currentDamage,
                font,
                fontShadow,
                color.rgb,
                scale
            )
        }
        renderPosOverlay(posToDraw, currentDamage)

        drawBlockBox(posToDraw, Color.RED, true)
    }

    private fun renderPosOverlay(pos: BlockPos, currentDamage: Float) {
        if (!renderPos) return
        val renderManager = mc.renderManager
        val renderX = pos.x - renderManager.renderPosX + 0.5
        val renderY = pos.y - renderManager.renderPosY - 0.5
        val renderZ = pos.z - renderManager.renderPosZ + 0.5
        val renderColor = getRenderColor(pos)
        val scaleValue = if (posProcess) damageAnim.animSmooth(currentDamage, 0.5F) else 1.0f
        if (posOutline) {
            RenderUtils.renderOutlines(renderX, renderY, renderZ, scaleValue, scaleValue, renderColor, 1.5F)
        } else {
            RenderUtils.renderBox(renderX, renderY, renderZ, scaleValue, scaleValue, renderColor)
        }
        GlStateManager.resetColor()
    }

    private fun getRenderColor(pos: BlockPos): Color {
        return if (clientTheme) getColorWithAlpha(1, 80) else if (pos.block != Blocks.bed) Color(255, 0, 0, 50) else Color(0, 255, 0, 50)
    }
    /**
     * Finds a new target block by [targetID]
     */
    private fun find(targetID: Int): BlockPos? {
        val eyes = mc.thePlayer?.eyes ?: return null
        var nearestBlockDistanceSq = Double.MAX_VALUE
        val nearestBlock = BlockPos.MutableBlockPos()
        val rangeSq = range * range

        eyes.getAllInBoxMutable(range + 1.0).forEach {
            val distSq = it.distanceSqToCenter(eyes.xCoord, eyes.yCoord, eyes.zCoord)
            if (it.block?.id != targetID || distSq > rangeSq || distSq > nearestBlockDistanceSq ||
                !isHittable(it) && !surroundings && !hypixel
            ) return@forEach

            nearestBlockDistanceSq = distSq
            nearestBlock.set(it)
        }

        return nearestBlock.takeIf { nearestBlockDistanceSq != Double.MAX_VALUE }
    }

    /**
     * Checks if the block is hittable (or allowed to be hit through walls)
     */
    private fun isHittable(blockPos: BlockPos): Boolean {
        val thePlayer = mc.thePlayer ?: return false
        return when (throughWalls.lowercase()) {
            "raycast" -> {
                val eyesPos = thePlayer.eyes
                val movingObjectPosition = mc.theWorld.rayTraceBlocks(eyesPos, blockPos.center, false, true, false)
                movingObjectPosition != null && movingObjectPosition.blockPos == blockPos
            }
            "around" -> EnumFacing.entries.any { !isBlockBBValid(blockPos.offset(it)) }
            else -> true
        }
    }

    /**
     * Clears the current target if it matches [currentPos] and resets related values.
     */
    private fun clearTarget(currentPos: BlockPos) {
        if (currentPos == obstructingPos) {
            obstructingPos = null
        }
        if (currentPos == pos) {
            pos = null
        }
        areSurroundings = false
        currentDamage = 0F
    }

    override val tag
        get() = getBlockName(block)
}