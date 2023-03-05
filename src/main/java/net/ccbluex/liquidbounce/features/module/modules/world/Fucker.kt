/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.player.AutoTool
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.extensions.getEyeVec3
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color

@ModuleInfo(name = "Fucker", category = ModuleCategory.WORLD)
object Fucker : Module() {

    /**
     * SETTINGS
     */

    private val blockValue = BlockValue("Block", 26)
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val throughWallsValue = ListValue("ThroughWalls", arrayOf("None", "Raycast", "Around"), "None")
    private val renderValue = ListValue("Render-Mode", arrayOf("Box", "Outline", "2D", "None"), "Box")
    private val rangeValue = FloatValue("Range", 5F, 1F, 7F)
    private val actionValue = ListValue("Action", arrayOf("Destroy", "Use"), "Destroy")
    private val ignoreFirstBlockValue = BoolValue("IgnoreFirstDetection", false)
    private val instantValue = BoolValue("Instant", false)
    private val switchValue = IntegerValue("SwitchDelay", 250, 0, 1000)
    private val rotationsValue = BoolValue("Rotations", true)
    private val surroundingsValue = BoolValue("Surroundings", true)
    private val noHitValue = BoolValue("NoHit", false)
    private val coolDownValue = IntegerValue("Cooldown-Seconds", 15, 0, 60)
    private val toggleResetCDValue = BoolValue("ResetCoolDownWhenToggled", false)
    private val resetOnWorldValue = BoolValue("ResetOnWorldChange", false).displayable { ignoreFirstBlockValue.get() }

    /**
     * VALUES
     */

    private var firstPos: BlockPos? = null
    private var firstPosBed: BlockPos? = null
    private var pos: BlockPos? = null
    private var oldPos: BlockPos? = null
    private var blockHitDelay = 0
    private val switchTimer = MSTimer()
    private val coolDownTimer = MSTimer()
    private var isRealBlock = false
    var currentDamage = 0F

    private var lastWorld: WorldClient? = null


    override fun onEnable() {
        if (toggleResetCDValue.get()) coolDownTimer.reset()
        firstPos = null
        firstPosBed = null
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient != lastWorld && resetOnWorldValue.get()) {
            firstPos = null
            firstPosBed = null
        }
        lastWorld = event.worldClient
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (noHitValue.get()) {
            val killAura = LiquidBounce.moduleManager[KillAura::class.java]!!

            if (killAura.state && killAura.currentTarget != null) {
                return
            }
        }

        val targetId = blockValue.get()

        if (pos == null || Block.getIdFromBlock(BlockUtils.getBlock(pos)) != targetId ||
            BlockUtils.getCenterDistance(pos!!) > rangeValue.get()) {
            pos = find(targetId)
        }

        // Reset current breaking when there is no target block
        if (pos == null) {
            currentDamage = 0F
            return
        }

        var currentPos = pos ?: return
        var rotations = RotationUtils.faceBlock(currentPos) ?: return

        // Surroundings
        var surroundings = false

        if (surroundingsValue.get()) {
            val eyes = mc.thePlayer.getPositionEyes(1F)
            val blockPos = mc.theWorld.rayTraceBlocks(eyes, rotations.vec, false,
                    false, true).blockPos

            if (blockPos != null && blockPos.getBlock() !is BlockAir) {
                if (currentPos.x != blockPos.x || currentPos.y != blockPos.y || currentPos.z != blockPos.z) {
                    surroundings = true
                }

                pos = blockPos
                currentPos = pos ?: return
                rotations = RotationUtils.faceBlock(currentPos) ?: return
            }
        }

        // Reset switch timer when position changed
        if (oldPos != null && oldPos != currentPos) {
            currentDamage = 0F
            switchTimer.reset()
        }

        oldPos = currentPos

        if (!switchTimer.hasTimePassed(switchValue.get().toLong())) {
            return
        }

        // Block hit delay
        if (blockHitDelay > 0) {
            blockHitDelay--
            return
        }

        // Face block
        if (rotationsValue.get()) {
            RotationUtils.setTargetRotation(rotations.rotation)
        }

        when {
            // Destory block
            actionValue.equals("destroy") || surroundings || !isRealBlock -> {
                // Auto Tool
                val autoTool = LiquidBounce.moduleManager[AutoTool::class.java]!!
                if (autoTool.state) {
                    autoTool.switchSlot(currentPos)
                }

                // Break block
                if (instantValue.get()) {
                    // CivBreak style block breaking
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))
                    if (swingValue.equals("Normal")) {
                        mc.thePlayer.swingItem()
                    } else if (swingValue.equals("Packet")) {
                        mc.netHandler.addToSendQueue(C0APacketAnimation())
                    }
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))
                    currentDamage = 0F
                    return
                }

                // Minecraft block breaking
                val block = currentPos.getBlock() ?: return

                if (currentDamage == 0F) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))

                    if (mc.thePlayer.capabilities.isCreativeMode ||
                            block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) >= 1.0F) {
                        if (swingValue.equals("Normal")) {
                            mc.thePlayer.swingItem()
                        } else if (swingValue.equals("Packet")) {
                            mc.netHandler.addToSendQueue(C0APacketAnimation())
                        }
                        mc.playerController.onPlayerDestroyBlock(pos, EnumFacing.DOWN)

                        currentDamage = 0F
                        pos = null
                        return
                    }
                }

                if (swingValue.equals("Normal")) {
                    mc.thePlayer.swingItem()
                } else if (swingValue.equals("Packet")) {
                    mc.netHandler.addToSendQueue(C0APacketAnimation())
                }
                currentDamage += block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, currentPos)
                mc.theWorld.sendBlockBreakProgress(mc.thePlayer.entityId, currentPos, (currentDamage * 10F).toInt() - 1)

                if (currentDamage >= 1F) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))
                    mc.playerController.onPlayerDestroyBlock(currentPos, EnumFacing.DOWN)
                    blockHitDelay = 4
                    currentDamage = 0F
                    pos = null
                }
            }

            // Use block
            actionValue.equals("use") -> {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, pos, EnumFacing.DOWN,
                        Vec3(currentPos.x.toDouble(), currentPos.y.toDouble(), currentPos.z.toDouble()))) {
                    if (swingValue.equals("Normal")) {
                        mc.thePlayer.swingItem()
                    } else if (swingValue.equals("Packet")) {
                        mc.netHandler.addToSendQueue(C0APacketAnimation())
                    }
                    blockHitDelay = 4
                    currentDamage = 0F
                    pos = null
                }
            }
        }

        @EventTarget
        fun onRender3D(event: Render3DEvent) {
            when (renderValue.get().lowercase()) {
                "box" -> RenderUtils.drawBlockBox(pos ?: return, if (!coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) Color.DARK_GRAY else Color.RED, false)
                "outline" -> RenderUtils.drawBlockBox(pos ?: return, if (!coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) Color.DARK_GRAY else Color.RED, true)
                "2d" -> RenderUtils.draw2D(pos ?: return, if (!coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) Color.DARK_GRAY.rgb else Color.RED.rgb, Color.BLACK.rgb)
            }
        }

        @EventTarget
        fun onRender2D(event: Render2DEvent) {
            val sc = ScaledResolution(mc)
            if (coolDownValue.get() > 0 && !coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) {
                val timeLeft = "Cooldown: ${(coolDownTimer.hasTimeLeft(coolDownValue.get().toLong() * 1000L) / 1000L).toInt()}s"
                val strWidth = Fonts.minecraftFont.getStringWidth(timeLeft)

                Fonts.minecraftFont.drawString(timeLeft, sc.getScaledWidth() / 2 - strWidth / 2 - 1, sc.getScaledHeight() / 2 - 70, 0x000000)
                Fonts.minecraftFont.drawString(timeLeft, sc.getScaledWidth() / 2 - strWidth / 2 + 1, sc.getScaledHeight() / 2 - 70, 0x000000)
                Fonts.minecraftFont.drawString(timeLeft, sc.getScaledWidth() / 2 - strWidth / 2, sc.getScaledHeight() / 2 - 69, 0x000000)
                Fonts.minecraftFont.drawString(timeLeft, sc.getScaledWidth() / 2 - strWidth / 2, sc.getScaledHeight() / 2 - 71, 0x000000)
                Fonts.minecraftFont.drawString(timeLeft, sc.getScaledWidth() / 2 - strWidth / 2, sc.getScaledHeight() / 2 - 70, -1)
            }
        }

    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        RenderUtils.drawBlockBox(pos ?: return, Color.RED, false, true, 1F)
    }

    /**
     * Find new target block by [targetID]
     */
    private fun find(targetID: Int): BlockPos? {
        val radius = rangeValue.get().toInt() + 1

        var nearestBlockDistance = Double.MAX_VALUE
        var nearestBlock: BlockPos? = null

        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    val blockPos = BlockPos(mc.thePlayer.posX.toInt() + x, mc.thePlayer.posY.toInt() + y,
                        mc.thePlayer.posZ.toInt() + z)
                    val block = getBlock(blockPos) ?: continue

                    if (Block.getIdFromBlock(block) != targetID) continue

                    val distance = getCenterDistance(blockPos)
                    if (distance > rangeValue.get()) continue
                    if (nearestBlockDistance < distance) continue
                    if (!isHitable(blockPos) && !surroundingsValue.get()) continue

                    nearestBlockDistance = distance
                    nearestBlock = blockPos
                }
            }
        }

    if (ignoreFirstBlockValue.get() && nearestBlock != null) {
        if (firstPos == null) {
            firstPos = nearestBlock
            LiquidBounce.hud.addNotification(Notification(name,"Found first ${getBlockName(targetID)} block at ${nearestBlock!!.x.toInt()} ${nearestBlock!!.y.toInt()} ${nearestBlock!!.z.toInt()}",  NotifyType.SUCCESS))
        }
        if (targetID == 26 && firstPos != null && firstPosBed == null) { // bed
            when (true) {
                getBlock(firstPos!!.east()) != null && Block.getIdFromBlock(getBlock(firstPos!!.east())!!) == 26 -> firstPosBed = firstPos!!.east()
                getBlock(firstPos!!.west()) != null && Block.getIdFromBlock(getBlock(firstPos!!.west())!!) == 26 -> firstPosBed = firstPos!!.west()
                getBlock(firstPos!!.south()) != null && Block.getIdFromBlock(getBlock(firstPos!!.south())!!) == 26 -> firstPosBed = firstPos!!.south()
                getBlock(firstPos!!.north()) != null && Block.getIdFromBlock(getBlock(firstPos!!.north())!!) == 26 -> firstPosBed = firstPos!!.north()
            }
            if (firstPosBed != null)
                LiquidBounce.hud.addNotification(Notification(name,"Found second Bed block at ${firstPosBed!!.x.toInt()} ${firstPosBed!!.y.toInt()} ${firstPosBed!!.z.toInt()}", NotifyType.SUCCESS))
        }
    }
    return if (ignoreFirstBlockValue.get() && (firstPos == nearestBlock || firstPosBed == nearestBlock)) null else nearestBlock
}

    /**
     * Check if block is hitable (or allowed to hit through walls)
     */
    private fun isHitable(blockPos: BlockPos): Boolean {
        return when (throughWallsValue.get().lowercase()) {
            "raycast" -> {
                val eyesPos = mc.thePlayer.getEyeVec3()
                val movingObjectPosition = mc.theWorld.rayTraceBlocks(eyesPos,
                        Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5), false,
                        true, false)

                movingObjectPosition != null && movingObjectPosition.blockPos == blockPos
            }
            "around" -> !BlockUtils.isFullBlock(blockPos.down()) || !BlockUtils.isFullBlock(blockPos.up()) || !BlockUtils.isFullBlock(blockPos.north()) ||
                    !BlockUtils.isFullBlock(blockPos.east()) || !BlockUtils.isFullBlock(blockPos.south()) || !BlockUtils.isFullBlock(blockPos.west())
            else -> true
        }
    }

    override val tag: String
        get() = BlockUtils.getBlockName(blockValue.get())
}
