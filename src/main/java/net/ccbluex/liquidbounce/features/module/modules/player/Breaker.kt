/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.extensions.getEyeVec3
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@ModuleInfo(name = "Breaker", category = ModuleCategory.PLAYER)
object Breaker : Module() {

    /**
     * SETTINGS
     */
    private val blockValue = BlockValue("Block", 26)
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val ignoreFirstBlockValue = BoolValue("IgnoreFirstDetection", false)
    private val onClickMouse = BoolValue("onClick", false)
    private val noHitValue = BoolValue("NoHit", false)
    private val noMoveValue = BoolValue("noMove", false)
    private val throughWallsValue = ListValue("ThroughWalls", arrayOf("None", "Raycast", "Around", "Hypixel"), "None")
    private val actionValue = ListValue("Action", arrayOf("Destroy", "Use"), "Destroy")
    private val rangeValue = FloatValue("Range", 5F, 1F, 7F)
    private val switchValue = IntegerValue("SwitchDelay", 250, 0, 1000)
    private val surroundingsValue = BoolValue("Surroundings", true)
    private val rotationsValue = BoolValue("Rotations", true)
    private val fastMineValue = BoolValue("FastMine", false)
    private val fastMineSpeed = FloatValue("FastMine-Speed", 1.5f, 1f, 3f).displayable { fastMineValue.get() }
    private val instantValue = BoolValue("InstantMine", false)
    private val showProcess= BoolValue("ShowProcess", false)
    private val coolDownValue = IntegerValue("Cooldown-Seconds", 15, 0, 60)
    private val toggleResetCDValue = BoolValue("ResetCoolDownWhenToggled", false)
    private val resetOnWorldValue = BoolValue("ResetOnWorldChange", false).displayable { ignoreFirstBlockValue.get() }
    private val renderValue = ListValue("Render-Mode", arrayOf("Box", "Outline", "2D", "None"), "Box")
    private val renderPos = BoolValue("RenderPos", true)
    private val renderBed = BoolValue("RenderBed", true)

    /**
     * VALUES
     */

    private var firstPos: BlockPos? = null
    private var firstPosBed: BlockPos? = null
    var pos: BlockPos? = null
    private var oldPos: BlockPos? = null
    private var blockHitDelay = 0
    private val switchTimer = MSTimer()
    private val coolDownTimer = MSTimer()
    private var isRealBlock = false
    var currentDamage = 0F
    private var facing: EnumFacing? = null
    private var boost = false
    private var damage = 0F

    private var lastWorld: WorldClient? = null

    //Bed ESP
    private val searchTimer = MSTimer()
    private val posList: MutableList<BlockPos> = ArrayList()
    private var color = Color.CYAN
    private var thread: Thread? = null
    var rotTicks = 0


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
        if (pos != null) {
            rotTicks++
        } else {
            rotTicks = 0
        }

        color = ClientTheme.getColorWithAlpha(1, 30)
        if (searchTimer.hasTimePassed(1000L) && (thread == null || !thread!!.isAlive)) {
            val radius = 100
            val selectedBlock = Block.getBlockById(26)
            if (selectedBlock == null || selectedBlock === Blocks.air) return
            thread = Thread({
                val blockList: MutableList<BlockPos> = ArrayList()
                for (x in -radius until radius) {
                    for (y in radius downTo -radius + 1) {
                        for (z in -radius until radius) {
                            val xPos = mc.thePlayer.posX.toInt() + x
                            val yPos = mc.thePlayer.posY.toInt() + y
                            val zPos = mc.thePlayer.posZ.toInt() + z
                            val blockPos = BlockPos(xPos, yPos, zPos)
                            val block = getBlock(blockPos)
                            if (block === selectedBlock) blockList.add(blockPos)
                        }
                    }
                }
                searchTimer.reset()
                synchronized(posList) {
                    posList.clear()
                    posList.addAll(blockList)
                }
            }, "BlockESP-BlockFinder")
            thread!!.start()
        }

        if (noHitValue.get()) {
            val killAura = FDPClient.moduleManager[KillAura::class.java]!!

            if (killAura.state && killAura.currentTarget != null) {
                return
            }
        }

        val targetId = blockValue.get()

        if (pos == null || Block.getIdFromBlock(BlockUtils.getBlock(pos)) != targetId ||
            BlockUtils.getCenterDistance(pos!!) > rangeValue.get()) {
            pos = find(targetId)

        }

        if (!onClickMouse.get() || mc.gameSettings.keyBindAttack.isKeyDown) {
        if (noMoveValue.get()) {
            if (MovementUtils.isMoving()) {
                firstPos = null
                firstPosBed = null
                facing = null
                pos = null
                oldPos = null
                currentDamage = 0F
                RotationUtils.faceBlock(null) ?: return
            }
        }

        if (throughWallsValue.equals("Hypixel")) {
            val blockPos = find(26) ?: return
            if (BlockUtils.isFullBlock(blockPos.up())) {
                pos = blockPos.up()?: return
            } else {
                pos = blockPos?: return
            }
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

        if (surroundingsValue.get() && !throughWallsValue.equals("Hypixel")) {
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
                val autoTool = FDPClient.moduleManager[AutoTool::class.java]!!
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

                if (currentDamage > 1F) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))
                    mc.playerController.onPlayerDestroyBlock(currentPos, EnumFacing.DOWN)
                    mc.theWorld.setBlockState(currentPos, Blocks.air.defaultState, 11)
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
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val blockPoss = pos!!
        val x = blockPoss.x - mc.renderManager.renderPosX
        val y = blockPoss.y - mc.renderManager.renderPosY
        val z = blockPoss.z - mc.renderManager.renderPosZ
        val c = ClientTheme.getColorWithAlpha(1, 30)
        if (renderPos.get()) {
            RenderUtils.renderOutlines(x + 0.5, y - 0.5, z + 0.5, 1.0f, 1.0f, c, 3F)
            GlStateManager.resetColor()
        }
        if (renderBed.get()) {
            synchronized(posList) {
                for (blockPos in posList) {
                    val bedx = blockPos.x - mc.renderManager.renderPosX
                    val bedy = blockPos.y - mc.renderManager.renderPosY
                    val bedz = blockPos.z - mc.renderManager.renderPosZ
                    RenderUtils.renderBox(bedx + 0.5, bedy - 0.5, bedz + 0.5, 1.0F, 1.0F, color)
                    GlStateManager.resetColor()
                }
            }
        }
        when (renderValue.get().lowercase()) {
            "box" -> RenderUtils.drawBlockBox(pos ?: return, if (!coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) Color.DARK_GRAY else Color.RED, false)
            "outline" -> RenderUtils.drawBlockBox(pos ?: return, if (!coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) Color.DARK_GRAY else Color.RED, true)
            "2d" -> RenderUtils.draw2D(pos ?: return, if (!coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) Color.DARK_GRAY.rgb else Color.RED.rgb, Color.BLACK.rgb)
            else -> RenderUtils.drawBlockBox(pos ?: return, if (!coolDownTimer.hasTimePassed(coolDownValue.get().toLong() * 1000L)) Color.DARK_GRAY else Color.RED, false)
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sc = ScaledResolution(mc)
        val width = ScaledResolution(mc).scaledWidth
        val height = ScaledResolution(mc).scaledHeight
        val d = DecimalFormat("0", DecimalFormatSymbols(Locale.ENGLISH))
        var damage = currentDamage
        var damageTick = 0F
        if (damage >= 1F) {
            damage = 1F
        }
        if (damage == 1F) {
            damageTick++
        }
        if (damageTick >= 5F) {
            damage = currentDamage
        }
        if (showProcess.get()) {
            if (damage != 0F) {
                mc.fontRendererObj.drawString(
                    d.format(damage * 100) + "%",
                    width / 2F,
                    height / 2 + 20F,
                    Color.WHITE.rgb,
                    true
                )
            }
        }
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
            FDPClient.hud.addNotification(Notification(name,"Found first ${getBlockName(targetID)} block at ${nearestBlock!!.x.toInt()} ${nearestBlock!!.y.toInt()} ${nearestBlock!!.z.toInt()}",  NotifyType.SUCCESS))
        }
        if (targetID == 26 && firstPos != null && firstPosBed == null) { // bed
            firstPosBed = when (true) {
                (getBlock(firstPos!!.east()) != null && Block.getIdFromBlock(getBlock(firstPos!!.east())!!) == 26) -> firstPos!!.east()
                (getBlock(firstPos!!.west()) != null && Block.getIdFromBlock(getBlock(firstPos!!.west())!!) == 26) -> firstPos!!.west()
                (getBlock(firstPos!!.south()) != null && Block.getIdFromBlock(getBlock(firstPos!!.south())!!) == 26) -> firstPos!!.south()
                (getBlock(firstPos!!.north()) != null && Block.getIdFromBlock(getBlock(firstPos!!.north())!!) == 26) -> firstPos!!.north()
                true -> TODO()
                false -> TODO()
            }
            if (firstPosBed != null)
                FDPClient.hud.addNotification(Notification(name,"Found second ${getBlockName(targetID)} block at ${firstPosBed!!.x.toInt()} ${firstPosBed!!.y.toInt()} ${firstPosBed!!.z.toInt()}", NotifyType.SUCCESS))
        }
    }
    return if (ignoreFirstBlockValue.get() && (firstPos == nearestBlock || firstPosBed == nearestBlock)) null else nearestBlock
}

    @EventTarget
    fun onMotion(e: MotionEvent) {
        if (!fastMineValue.get()) return
        if (e.isPre()) {
            mc.playerController.blockHitDelay = 0
            if (pos != null && boost) {
                val blockState = mc.theWorld.getBlockState(pos) ?: return
                damage += try {
                    blockState.block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) * fastMineSpeed.get()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    return
                }
                if (damage >= 1) {
                    try {
                        mc.theWorld.setBlockState(pos, Blocks.air.defaultState, 11)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        return
                    }
                    PacketUtils.sendPacketNoEvent(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            pos,
                            facing
                        )
                    )
                    damage = 0f
                    boost = false
                }
            }
        }
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
