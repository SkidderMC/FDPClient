/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.stats.StatList
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_I)
class Scaffold : Module() {
    // Mode
    val modeValue = ListValue("Mode", arrayOf("Normal", "Rewinside", "Expand"), "Normal")

    // Tower
    private val towerModeValue = ListValue(
        "TowerMode", arrayOf(
            "None",
            "Jump",
            "Motion",
            "ConstantMotion",
            "PlusMotion",
            "StableMotion",
            "MotionTP",
            "Packet",
            "Teleport",
            "AAC3.3.9",
            "AAC3.6.4",
            "AAC4.4Constant",
            "AAC4Jump"
        ), "None"
    )
    private val stopWhenBlockAbove = BoolValue("StopTowerWhenBlockAbove", true)

    // Delay
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) set(i)
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) set(i)
        }
    }
    private val placeableDelay = BoolValue("PlaceableDelay", false)

    // AutoBlock
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Spoof", "LiteSpoof", "Switch", "OFF"), "LiteSpoof")

    // Basic stuff
    val sprintValue = BoolValue("Sprint", true)
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val searchValue = BoolValue("Search", true)
    private val downValue = BoolValue("Down", true)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post"), "Post")

    // Eagle
    private val eagleValue = ListValue("Eagle", arrayOf("Slient", "Normal", "OFF"), "OFF")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10)
    
    // Expand
    private val expandLengthValue = IntegerValue("ExpandLength", 5, 1, 6)

    // Rotations
    private val rotationsValue = ListValue("Rotations", arrayOf("None", "Vanilla", "AAC", "Test1", "Test2"), "AAC")
    private val aacYawValue = IntegerValue("AACYawOffset", 0, 0, 90)
    //private val tolleyBridgeValue = IntegerValue("TolleyBridgeTick", 0, 0, 10)
    //private val tolleyYawValue = IntegerValue("TolleyYaw", 0, 0, 90)
    private val silentRotationValue = BoolValue("SilentRotation", true)
    private val minRotationSpeedValue:IntegerValue = object :IntegerValue("MinRotationSpeed", 180, 0, 180) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = maxRotationSpeedValue.get()
            if (v < newValue) set(v)
        }
    }
    private val maxRotationSpeedValue:IntegerValue = object :IntegerValue("MaxRotationSpeed", 180, 0, 180) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = minRotationSpeedValue.get()
            if (v > newValue) set(v)
        }
    }
    private val keepLengthValue = IntegerValue("KeepRotationTick", 0, 0, 20)
    //private var tolleyStayTick = 0
    //private var lastTickOnGround = false
    // Zitter
    //private val zitterValue = BoolValue("Zitter", false)
    private val zitterModeValue = ListValue("ZitterMode", arrayOf("Teleport", "Smooth", "OFF"), "OFF")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f)
    private val zitterStrength = FloatValue("ZitterStrength", 0.072f, 0.05f, 0.2f)

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 5f)
    private val towerActiveValue = ListValue("TowerActivation", arrayOf("Always", "PressSpace", "NoMove", "OFF"), "PressSpace")
    private val towerTimerValue = FloatValue("TowerTimer", 1f, 0.1f, 5f)
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)

    // Safety
    private val sameYValue = BoolValue("SameY", false)
    private val safeWalkValue = ListValue("SafeWalk", arrayOf("Ground", "Air", "OFF"), "OFF")
    private val autoJumpValue = BoolValue("AutoJump", false)

    // Extra click
    private val extraClickValue = ListValue("ExtraClick", arrayOf("EmptyC08", "AfterPlace"/*, "RayCast"*/, "OFF"), "OFF")
    private val extraClickMaxDelayValue: IntegerValue = object : IntegerValue("ExtraClickMaxDelay", 100, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMinDelayValue.get()
            if (i > newValue) set(i)
        }
    }
    private val extraClickMinDelayValue: IntegerValue = object : IntegerValue("ExtraClickMinDelay", 50, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMaxDelayValue.get()
            if (i < newValue) set(i)
        }
    }

    // Jump mode
    private val jumpMotionValue = FloatValue("TowerJumpMotion", 0.42f, 0.3681289f, 0.79f)
    private val jumpDelayValue = IntegerValue("TowerJumpDelay", 0, 0, 20)

    // Stable/PlusMotion
    private val stableMotionValue = FloatValue("TowerStableMotion", 0.42f, 0.1f, 1f)
    private val plusMotionValue = FloatValue("TowerPlusMotion", 0.1f, 0.01f, 0.2f)
    private val plusMaxMotionValue = FloatValue("TowerPlusMaxMotion", 0.8f, 0.1f, 2f)

    // ConstantMotion
    private val constantMotionValue = FloatValue("TowerConstantMotion", 0.42f, 0.1f, 1f)
    private val constantMotionJumpGroundValue = FloatValue("TowerConstantMotionJumpGround", 0.79f, 0.76f, 1f)

    // Teleport
    private val teleportHeightValue = FloatValue("TowerTeleportHeight", 1.15f, 0.1f, 5f)
    private val teleportDelayValue = IntegerValue("TowerTeleportDelay", 0, 0, 20)
    private val teleportGroundValue = BoolValue("TowerTeleportGround", true)
    private val teleportNoMotionValue = BoolValue("TowerTeleportNoMotion", false)

    // Visuals
    private val counterDisplayValue = BoolValue("Counter", true)
    private val markValue = BoolValue("Mark", false)

    /**
     * MODULE
     */
    // Target block
    private var targetPlace: PlaceInfo? = null

    // Launch position
    private var launchY = 0

    // Rotation lock
    private var lockRotation: Rotation? = null

    // Auto block slot
    private var slot = 0

    // Zitter Smooth
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private val clickTimer = MSTimer()
    private val towerTimer = TickTimer()
    private var delay: Long = 0
    private var clickDelay: Long = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var shouldGoDown = false
    private var jumpGround = 0.0
    private var towerStatus = false
    private var canSameY = false
    private var lastPlaceBlock: BlockPos?=null

    /**
     * Enable module
     */
    override fun onEnable() {
        if (mc.thePlayer == null) return
        launchY = mc.thePlayer.posY.toInt()
        clickDelay=TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
    }

    /**
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        //if(!mc.thePlayer.onGround) tolleyStayTick=0
        //    else tolleyStayTick++
        //if(tolleyStayTick>100) tolleyStayTick=100
        if(towerStatus && towerModeValue.get().toLowerCase()!="aac3.3.9" && towerModeValue.get().toLowerCase()!="aac4.4constant" && towerModeValue.get().toLowerCase()!="aac4jump") mc.timer.timerSpeed = towerTimerValue.get()
        if(!towerStatus) mc.timer.timerSpeed = timerValue.get()
        if (towerStatus) {
            canSameY = false
            launchY = mc.thePlayer.posY.toInt()
        } else if (sameYValue.get()) {
            canSameY = true
            if(autoJumpValue.get() && !mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
        } else if (autoJumpValue.get()) {
            canSameY = true
            if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                mc.thePlayer.jump()
                launchY = mc.thePlayer.posY.toInt()
            }
        }else {
            canSameY = false
            launchY = mc.thePlayer.posY.toInt()
        }

        if(clickTimer.hasTimePassed(clickDelay)){
            when(extraClickValue.get().toLowerCase()){
                "emptyc08" -> {
                    val packet=C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot))
                    if(clickDelay<35){
                        PacketUtils.sendPacketNoEvent(packet)
                    }
                    if(clickDelay<50){
                        PacketUtils.sendPacketNoEvent(packet)
                    }
                    PacketUtils.sendPacketNoEvent(packet)
                }
            }
            clickDelay=TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
            clickTimer.reset()
        }

        mc.thePlayer.isSprinting = sprintValue.get()
        shouldGoDown = downValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false
        if (mc.thePlayer.onGround) {
            val mode = modeValue.get()

            // Rewinside scaffold mode
            if (mode.equals("Rewinside", ignoreCase = true)) {
                MovementUtils.strafe(0.2f)
                mc.thePlayer.motionY = 0.0
            }

            // Smooth Zitter
            if (zitterModeValue.get().equals("smooth", ignoreCase = true)) {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
                if (zitterTimer.hasTimePassed(100)) {
                    zitterDirection = !zitterDirection
                    zitterTimer.reset()
                }
                if (zitterDirection) {
                    mc.gameSettings.keyBindRight.pressed = true
                    mc.gameSettings.keyBindLeft.pressed = false
                } else {
                    mc.gameSettings.keyBindRight.pressed = false
                    mc.gameSettings.keyBindLeft.pressed = true
                }
            }

            // Eagle
            if (!eagleValue.get().equals("off", ignoreCase = true) && !shouldGoDown) {
                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    val shouldEagle = mc.theWorld.getBlockState(
                        BlockPos(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 1.0, mc.thePlayer.posZ
                        )
                    ).block === Blocks.air
                    if (eagleValue.get().equals("slient", ignoreCase = true)) {
                        if (eagleSneaking != shouldEagle) {
                            mc.netHandler.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer,
                                    if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING
                                )
                            )
                        }
                        eagleSneaking = shouldEagle
                    } else mc.gameSettings.keyBindSneak.pressed = shouldEagle
                    placedBlocksWithoutEagle = 0
                } else placedBlocksWithoutEagle++
            }

            // Zitter
            if (zitterModeValue.get().equals("teleport", ignoreCase = true)) {
                MovementUtils.strafe(zitterSpeed.get())
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                mc.thePlayer.motionX -= sin(yaw) * zitterStrength.get()
                mc.thePlayer.motionZ += cos(yaw) * zitterStrength.get()
                zitterDirection = !zitterDirection
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return
        val packet = event.packet

        // AutoBlock
        if (packet is C09PacketHeldItemChange) {
            slot = packet.slotId
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState
        towerStatus = false;
        // Tower
        towerStatus = (!stopWhenBlockAbove.get() || getBlock(BlockPos(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 2, mc.thePlayer.posZ
                      )) is BlockAir)
        if(towerStatus) {
            //further checks
            when(towerActiveValue.get().toLowerCase()) {
                "off" -> towerStatus = false
                "always" -> {
                    towerStatus = (mc.gameSettings.keyBindLeft.isKeyDown
                            || mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown
                            || mc.gameSettings.keyBindBack.isKeyDown)
                }
                "pressspace" -> {
                    towerStatus = mc.gameSettings.keyBindJump.isKeyDown
                }
                "nomove" -> {
                    towerStatus = !(mc.gameSettings.keyBindLeft.isKeyDown
                            || mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown
                            || mc.gameSettings.keyBindBack.isKeyDown) && mc.gameSettings.keyBindJump.isKeyDown
                }
            }
        }
        if(towerStatus) move()
        // Lock Rotation
        if (rotationsValue.get() != "None" && keepLengthValue.get()>0 && lockRotation != null && silentRotationValue.get()) {
            val limitedRotation =
                RotationUtils.limitAngleChange(RotationUtils.serverRotation, lockRotation, getSpeed())
            RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
        }

        // Place block
        if (placeModeValue.get().equals(eventState.stateName, ignoreCase = true)) place()

        // Update and search for new block
        if (event.isPre()) update()

        // Reset placeable delay
        if (targetPlace == null && placeableDelay.get()) delayTimer.reset()
    }

    private fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    private fun move() {
        when (towerModeValue.get().toLowerCase()) {
            "none" -> {
                 if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.42
                }
            }
            "jump" -> {
                if (mc.thePlayer.onGround && towerTimer.hasTimePassed(jumpDelayValue.get())) {
                    fakeJump()
                    mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
                    towerTimer.reset()
                }
            }
            "motion" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.42
                } else if (mc.thePlayer.motionY < 0.1) mc.thePlayer.motionY = -0.3
            }
            "motiontp" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.42
                } else if (mc.thePlayer.motionY < 0.23)
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            }
            "packet" -> {
                if (mc.thePlayer.onGround && towerTimer.hasTimePassed(2)) {
                    fakeJump()
                    mc.netHandler.addToSendQueue(
                        C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 0.753, mc.thePlayer.posZ, false
                        )
                    )
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ)
                    towerTimer.reset()
                }
            }
            "teleport" -> {
                if (teleportNoMotionValue.get()) mc.thePlayer.motionY = 0.0
                if ((mc.thePlayer.onGround || !teleportGroundValue.get()) && towerTimer.hasTimePassed(teleportDelayValue.get())) {
                    fakeJump()
                    mc.thePlayer.setPositionAndUpdate(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + teleportHeightValue.get(),
                        mc.thePlayer.posZ
                    )
                    towerTimer.reset()
                }
            }
            "constantmotion" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    jumpGround = mc.thePlayer.posY
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                }
                if (mc.thePlayer.posY > jumpGround + constantMotionJumpGroundValue.get()) {
                    fakeJump()
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                    jumpGround = mc.thePlayer.posY
                }
            }
            "plusmotion" -> {
                mc.thePlayer.motionY += plusMotionValue.get()
                if (mc.thePlayer.motionY >= plusMaxMotionValue.get()) {
                    mc.thePlayer.motionY = plusMaxMotionValue.get().toDouble()
                }
            }
            "stablemotion" -> {
                mc.thePlayer.motionY = stableMotionValue.get().toDouble()
            }
            "aac3.3.9" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.4001
                }
                mc.timer.timerSpeed = 1f
                if (mc.thePlayer.motionY < 0) {
                    mc.thePlayer.motionY -= 0.00000945
                    mc.timer.timerSpeed = 1.6f
                }
            }
            "aac3.6.4" -> {
                if (mc.thePlayer.ticksExisted % 4 == 1) {
                    mc.thePlayer.motionY = 0.4195464
                    mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                } else if (mc.thePlayer.ticksExisted % 4 == 0) {
                    mc.thePlayer.motionY = -0.5
                    mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                }
            }
            "aac4.4constant" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    jumpGround = mc.thePlayer.posY
                    mc.thePlayer.motionY = 0.42
                }
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = -0.00000001
                mc.thePlayer.jumpMovementFactor = 0.000F
                mc.timer.timerSpeed = 0.60f
                if (mc.thePlayer.posY > jumpGround + 0.99) {
                    fakeJump()
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY-0.001335979112146, mc.thePlayer.posZ)
                    mc.thePlayer.motionY = 0.42
                    jumpGround = mc.thePlayer.posY
                    mc.timer.timerSpeed = 0.75f
                }
            }
            "aac4jump" -> {
                mc.timer.timerSpeed = 0.97f
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.387565
                    mc.timer.timerSpeed = 1.05f
                }
            }
        }
    }

    private fun update() {
        if (if (!autoBlockValue.get().equals("off", ignoreCase = true)) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null ||
                    !(mc.thePlayer.heldItem.item is ItemBlock && !InventoryUtils.isBlockListBlock(mc.thePlayer.heldItem.item as ItemBlock))
        ) return
        findBlock(modeValue.get().equals("expand", ignoreCase = true))
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean) {
        val blockPosition = if (shouldGoDown) if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) BlockPos(
            mc.thePlayer.posX,
            mc.thePlayer.posY - 0.6,
            mc.thePlayer.posZ
        ) else BlockPos(
            mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ
        ).down() else if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5 && ((!canSameY && sameYValue.get()) || !sameYValue.get())) BlockPos(
            mc.thePlayer
        ) else if(canSameY && launchY<=mc.thePlayer.posY) BlockPos(
            mc.thePlayer.posX, launchY-1.0, mc.thePlayer.posZ) else BlockPos(
            mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ
        ).down()
        if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) return
        if (expand) {
            for (i in 0 until expandLengthValue.get()) {
                if (search(
                        blockPosition.add(
                            if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                            0,
                            if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
                        ), false
                    )
                ) return
            }
        } else if (searchValue.get()) {
            for (x in -1..1) for (z in -1..1) if (search(blockPosition.add(x, 0, z), !shouldGoDown)) return
        }
    }

    /**
     * Place target block
     */
    private fun place() {
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
            return
        }
        if (!delayTimer.hasTimePassed(delay) || !towerStatus && canSameY && launchY - 1 != targetPlace!!.vec3.yCoord.toInt()) return
        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        if (mc.thePlayer.heldItem == null || !(mc.thePlayer.heldItem.item is ItemBlock && !InventoryUtils.isBlockListBlock(mc.thePlayer.heldItem.item as ItemBlock))) {
            if (autoBlockValue.get().equals("off", ignoreCase = true)) return
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
            if (autoBlockValue.get().equals("LiteSpoof", ignoreCase = true) || autoBlockValue.get().equals("Spoof", ignoreCase = true)) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            } else {
                mc.thePlayer.inventory.changeCurrentItem(blockSlot - 36)
            }
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3)) {
            delayTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }

            val swing=swingValue.get()
            if(swing.equals("packet",true)){
                mc.netHandler.addToSendQueue(C0APacketAnimation())
            }else if(swing.equals("normal",true)){
                mc.thePlayer.swingItem()
            }

            lastPlaceBlock=targetPlace!!.blockPos.add(targetPlace!!.enumFacing.directionVec)
            when(extraClickValue.get().toLowerCase()){
                "afterplace" -> {
                    // fake click
                    val p_onPlayerRightClick_4_=targetPlace!!.blockPos
                    val p_onPlayerRightClick_6_=targetPlace!!.vec3
                    val f = (p_onPlayerRightClick_6_.xCoord - p_onPlayerRightClick_4_.x.toDouble()).toFloat()
                    val f1 = (p_onPlayerRightClick_6_.yCoord - p_onPlayerRightClick_4_.y.toDouble()).toFloat()
                    val f2 = (p_onPlayerRightClick_6_.zCoord - p_onPlayerRightClick_4_.z.toDouble()).toFloat()
                    val c08=C08PacketPlayerBlockPlacement(targetPlace!!.blockPos, targetPlace!!.enumFacing.index, itemStack, f, f1, f2)
                    repeat((1000f/TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get()).coerceAtLeast(30)).toInt()+1){
                        PacketUtils.sendPacketNoEvent(c08)
                    }
                }
            }
        }

        if (autoBlockValue.get().equals("LiteSpoof", ignoreCase = true) && blockSlot >= 0)
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))

        // Reset
        targetPlace = null
    }

    /**
     * Disable scaffold module
     */
    override fun onDisable() {
        //tolleyStayTick=999
        if (mc.thePlayer == null) return
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
        lockRotation = null
        mc.timer.timerSpeed = 1f
        shouldGoDown = false
        RotationUtils.reset()
        if (slot != mc.thePlayer.inventory.currentItem) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }

    /**
     * Entity movement event
     *
     * @param event
     */
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (safeWalkValue.get().equals("off", ignoreCase = true) || shouldGoDown) return
        if (safeWalkValue.get().equals("air", ignoreCase = true) || mc.thePlayer.onGround) event.isSafeWalk = true
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (counterDisplayValue.get()) {
            drawTip()
        }
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (!markValue.get()) return
        for (i in 0 until if (modeValue.get().equals("Expand", ignoreCase = true)) expandLengthValue.get() + 1 else 2) {
            val blockPos = BlockPos(
                mc.thePlayer.posX + if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5){ 0.0 } else{ 1.0 }) - (if (shouldGoDown){ 1.0 }else{ 0.0 }),
                mc.thePlayer.posZ + if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
            )
            val placeInfo = get(blockPos)
            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(68, 117, 255, 100), false, true, 1f)
                break
            }
        }
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param checks        visible
     * @return
     */
    private fun search(blockPosition: BlockPos, checks: Boolean): Boolean {
        if (!isReplaceable(blockPosition)) return false
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
        )
        var placeRotation: PlaceRotation? = null
        for (side in EnumFacing.values()) {
            val neighbor = blockPosition.offset(side)
            if (!canBeClicked(neighbor)) continue
            val dirVec = Vec3(side.directionVec)
            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val posVec = Vec3(blockPosition).addVector(xSearch, ySearch, zSearch)
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))
                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)
                        ) {
                            zSearch += 0.1
                            continue
                        }

                        // face block
                        val diffX = hitVec.xCoord - eyesPos.xCoord
                        val diffY = hitVec.yCoord - eyesPos.yCoord
                        val diffZ = hitVec.zCoord - eyesPos.zCoord
                        val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                        val rotation = Rotation(
                            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, diffXZ))).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * 4,
                            rotationVector.yCoord * 4,
                            rotationVector.zCoord * 4
                        )
                        val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                        if (!(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.blockPos == neighbor)) {
                            zSearch += 0.1
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                placeRotation.rotation
                            )
                        ) placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
        }
        if (placeRotation == null) return false
        if (rotationsValue.get() != "None") {
            var rotation: Rotation? = null
            when (rotationsValue.get().toLowerCase()) {
                "aac" -> {
                    if (!towerStatus) {
                        rotation = Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180) + aacYawValue.get(), placeRotation.rotation.pitch)
                    }else{
                        rotation = placeRotation.rotation
                    }
                }
                "vanilla" -> {
                    rotation = placeRotation.rotation
                }
                "test1" -> {
                    var caluyaw = (java.lang.Math.round(placeRotation.rotation.yaw / 45) * 45).toFloat()
                    //Co丶Dynamic : Wo Shi Sha Bi
                    rotation = Rotation(caluyaw, placeRotation.rotation.pitch)
                }
                "test2" -> {
                    rotation = Rotation((MovementUtils.getDirection() + 135).toFloat(), placeRotation.rotation.pitch)
                }
            }
            if (rotation != null) {
                /*if(tolleyBridgeValue.get() > tolleyStayTick && (mc.thePlayer.onGround || lastTickOnGround ||
                    (!mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(
                            -mc.thePlayer.motionX,
                            0.98*(mc.thePlayer.motionY-0.08),
                            -mc.thePlayer.motionZ
                            )).isEmpty() && mc.thePlayer.motionY<=0)
                  ))
                    rotation = Rotation(
                        mc.thePlayer.rotationYaw + tolleyYawValue.get(),
                        placeRotation.rotation.pitch
                    )*/
                if (silentRotationValue.get()) {
                    val limitedRotation =
                        RotationUtils.limitAngleChange(RotationUtils.serverRotation, rotation, getSpeed())
                    RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
                } else {
                    mc.thePlayer.rotationYaw = rotation.yaw
                    mc.thePlayer.rotationPitch = rotation.pitch
                }
            }
            lockRotation = rotation
            //lastTickOnGround=mc.thePlayer.onGround
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    /**
     * @return hotbar blocks amount
     */
    val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock
                    && !InventoryUtils.BLOCK_BLACKLIST.contains((itemStack.item as ItemBlock).getBlock())
                ) amount += itemStack.stackSize
            }
            return amount
        }

    fun drawTip() {
        GlStateManager.pushMatrix()
        val info = "Blocks > $blocksAmount"
        val scaledResolution = ScaledResolution(mc)
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight
        val slot = InventoryUtils.findAutoBlockBlock()
        var stack: ItemStack? = barrier
        if (slot != -1) {
            if(mc.thePlayer.inventory.getCurrentItem()!=null) {
                val handItem = mc.thePlayer.inventory.getCurrentItem().item
                if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                    stack = mc.thePlayer.inventory.getCurrentItem()
                }
            }
            if(stack==barrier) {
                stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                if (stack == null) {
                    stack = barrier
                }
            }
        }
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemIntoGUI(
            stack,
            width / 2 - Fonts.font40.getStringWidth(info),
            (height * 0.6 - Fonts.font40.FONT_HEIGHT * 0.5).toInt()
        )
        RenderHelper.disableStandardItemLighting()
        Fonts.font40.drawCenteredString(info, width / 2f, height * 0.6f, Color.WHITE.rgb, false)
        GlStateManager.popMatrix()
    }

    fun getSpeed():Float{
        return (Math.random() * (maxRotationSpeedValue.get() - minRotationSpeedValue.get()) + minRotationSpeedValue.get()).toFloat()
    }
    
    fun roundYaw(rYaw: Float):Float{
        var lrYaw = rYaw
        while(lrYaw>360) {
            lrYaw -= 360
        }
        while(lrYaw<-360) {
            lrYaw += 360
        }
        return lrYaw
    }
    
    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerStatus)
            event.cancelEvent();
    }
    
    override val tag: String
        get() = modeValue.get()

    private val barrier = ItemStack(Item.getItemById(166), 0, 0)
}
