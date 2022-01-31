/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.extensions.rayTraceWithServerSideRotation
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
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.stats.StatList
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "Scaffold", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_G)
class Scaffold : Module() {

    // Delay
    private val placeableDelay = ListValue("PlaceableDelay", arrayOf("Normal", "Smart", "OFF"), "Normal")
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) set(i)
        }
    }.displayable { !placeableDelay.equals("OFF") } as IntegerValue
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !placeableDelay.equals("OFF") } as IntegerValue

    // AutoBlock
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Spoof", "LiteSpoof", "Switch", "OFF"), "LiteSpoof")

    // Basic stuff
    private val sprintValue = ListValue("Sprint", arrayOf("Always", "Dynamic", "OnGround", "OffGround", "OFF"), "Always")
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val searchValue = BoolValue("Search", true)
    private val downValue = BoolValue("Down", true)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post"), "Post")

    // Eagle
    private val eagleValue = ListValue("Eagle", arrayOf("Silent", "Normal", "OFF"), "OFF")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10).displayable { !eagleValue.equals("OFF") }

    // Expand
    private val expandLengthValue = IntegerValue("ExpandLength", 1, 1, 6)

    // Rotations
    private val rotationsValue = ListValue("Rotations", arrayOf("None", "Vanilla", "AAC", "Test1", "Test2", "Custom"), "AAC")
    private val aacYawValue = IntegerValue("AACYawOffset", 0, 0, 90).displayable { rotationsValue.equals("AAC") }
    private val customYaw = IntegerValue("CustomYaw", -145, -180, 180).displayable { rotationsValue.equals("Custom") }
    private val customPitch = IntegerValue("CustomPitch", 79, -90, 90).displayable { rotationsValue.equals("Custom") }
    // private val tolleyBridgeValue = IntegerValue("TolleyBridgeTick", 0, 0, 10)
    // private val tolleyYawValue = IntegerValue("TolleyYaw", 0, 0, 90)
    private val silentRotationValue = BoolValue("SilentRotation", true).displayable { !rotationsValue.equals("None") }
    private val minRotationSpeedValue: IntegerValue = object : IntegerValue("MinRotationSpeed", 180, 0, 180) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = maxRotationSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { !rotationsValue.equals("None") } as IntegerValue
    private val maxRotationSpeedValue: IntegerValue = object : IntegerValue("MaxRotationSpeed", 180, 0, 180) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = minRotationSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { !rotationsValue.equals("None") } as IntegerValue
    private val keepLengthValue = IntegerValue("KeepRotationTick", 0, 0, 20).displayable { !rotationsValue.equals("None") }

    // Zitter
    private val zitterModeValue = ListValue("ZitterMode", arrayOf("Teleport", "Smooth", "OFF"), "OFF")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f).displayable { !zitterModeValue.equals("OFF") }
    private val zitterStrength = FloatValue("ZitterStrength", 0.072f, 0.05f, 0.2f).displayable { !zitterModeValue.equals("OFF") }

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 5f)
    private val motionSpeedValue = BoolValue("MotionSpeedSet", false)
    private val motionSpeed = FloatValue("MotionSpeed", 0.1f, 0.05f, 1f).displayable { motionSpeedValue.get() }
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)

    // Tower
    private val towerModeValue = ListValue(
        "TowerMode", arrayOf(
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
            "AAC4Jump",
            "Verus"
        ), "Jump"
    )
    private val stopWhenBlockAbove = BoolValue("StopTowerWhenBlockAbove", true)
    private val towerFakeJump = BoolValue("TowerFakeJump", true)
    private val towerActiveValue = ListValue("TowerActivation", arrayOf("Always", "PressSpace", "NoMove", "OFF"), "PressSpace")
    private val towerTimerValue = FloatValue("TowerTimer", 1f, 0.1f, 5f)

    // Safety
    private val sameYValue = ListValue("SameY", arrayOf("Simple", "AutoJump", "WhenSpeed", "OFF"), "WhenSpeed")
    private val safeWalkValue = ListValue("SafeWalk", arrayOf("Ground", "Air", "OFF"), "OFF")
    private val hitableCheck = ListValue("HitableCheck", arrayOf("Simple", "Strict", "OFF"), "Simple")

    // Extra click
    private val extraClickValue = ListValue("ExtraClick", arrayOf("EmptyC08", "AfterPlace", "RayTrace", "OFF"), "OFF")
    private val extraClickMaxDelayValue: IntegerValue = object : IntegerValue("ExtraClickMaxDelay", 100, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMinDelayValue.get()
            if (i > newValue) set(i)
        }
    }.displayable { !extraClickValue.equals("OFF") } as IntegerValue
    private val extraClickMinDelayValue: IntegerValue = object : IntegerValue("ExtraClickMinDelay", 50, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMaxDelayValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !extraClickValue.equals("OFF") } as IntegerValue

    // Jump mode
    private val jumpMotionValue = FloatValue("TowerJumpMotion", 0.42f, 0.3681289f, 0.79f).displayable { towerModeValue.equals("Jump") }
    private val jumpDelayValue = IntegerValue("TowerJumpDelay", 0, 0, 20).displayable { towerModeValue.equals("Jump") }

    // Stable/PlusMotion
    private val stableMotionValue = FloatValue("TowerStableMotion", 0.42f, 0.1f, 1f).displayable { towerModeValue.equals("StableMotion") }
    private val plusMotionValue = FloatValue("TowerPlusMotion", 0.1f, 0.01f, 0.2f).displayable { towerModeValue.equals("PlusMotion") }
    private val plusMaxMotionValue = FloatValue("TowerPlusMaxMotion", 0.8f, 0.1f, 2f).displayable { towerModeValue.equals("PlusMotion") }

    // ConstantMotion
    private val constantMotionValue = FloatValue("TowerConstantMotion", 0.42f, 0.1f, 1f).displayable { towerModeValue.equals("ConstantMotion") }
    private val constantMotionJumpGroundValue = FloatValue("TowerConstantMotionJumpGround", 0.79f, 0.76f, 1f).displayable { towerModeValue.equals("ConstantMotion") }

    // Teleport
    private val teleportHeightValue = FloatValue("TowerTeleportHeight", 1.15f, 0.1f, 5f).displayable { towerModeValue.equals("Teleport") }
    private val teleportDelayValue = IntegerValue("TowerTeleportDelay", 0, 0, 20).displayable { towerModeValue.equals("Teleport") }
    private val teleportGroundValue = BoolValue("TowerTeleportGround", true).displayable { towerModeValue.equals("Teleport") }
    private val teleportNoMotionValue = BoolValue("TowerTeleportNoMotion", false).displayable { towerModeValue.equals("Teleport") }

    // Visuals
    private val counterDisplayValue = BoolValue("Counter", true)
    private val markValue = BoolValue("Mark", false)

    /**
     * MODULE
     */
    // Target block
    private var targetPlace: PlaceInfo? = null

    // Last OnGround position
    private var lastGroundY = 0

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
    private var lastPlace = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var shouldGoDown = false
    private var jumpGround = 0.0
    private var towerStatus = false
    private var canSameY = false
    private var lastPlaceBlock: BlockPos? = null
    private var afterPlaceC08: C08PacketPlayerBlockPlacement? = null
    
    //Other
    private var doSpoof = false

    /**
     * Enable module
     */
    override fun onEnable() {
        slot = mc.thePlayer.inventory.currentItem
        doSpoof = false
        if (mc.thePlayer == null) return
        lastGroundY = mc.thePlayer.posY.toInt()
        lastPlace = 2
        clickDelay = TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
    }

    /**
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // if(!mc.thePlayer.onGround) tolleyStayTick=0
        //    else tolleyStayTick++
        // if(tolleyStayTick>100) tolleyStayTick=100
        if (towerStatus && towerModeValue.get().lowercase() != "aac3.3.9" && towerModeValue.get().lowercase() != "aac4.4constant" && towerModeValue.get().lowercase() != "aac4jump") mc.timer.timerSpeed = towerTimerValue.get()
        if (!towerStatus) mc.timer.timerSpeed = timerValue.get()
        if (towerStatus || mc.thePlayer.isCollidedHorizontally) {
            canSameY = false
            lastGroundY = mc.thePlayer.posY.toInt()
        } else {
            when (sameYValue.get().lowercase()) {
                "simple" -> {
                    canSameY = true
                }
                "autojump" -> {
                    canSameY = true
                    if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                    }
                }
                "whenspeed" -> {
                    canSameY = LiquidBounce.moduleManager[Speed::class.java]!!.state
                }
                else -> {
                    canSameY = false
                }
            }
            if (mc.thePlayer.onGround) {
                lastGroundY = mc.thePlayer.posY.toInt()
            }
        }

        if (clickTimer.hasTimePassed(clickDelay)) {
            fun sendPacket(c08: C08PacketPlayerBlockPlacement) {
                if (clickDelay <35) {
                    PacketUtils.sendPacketNoEvent(c08)
                }
                if (clickDelay <50) {
                    PacketUtils.sendPacketNoEvent(c08)
                }
                PacketUtils.sendPacketNoEvent(c08)
            }
            when (extraClickValue.get().lowercase()) {
                "emptyc08" -> sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)))
                "afterplace" -> {
                    if (afterPlaceC08 != null) {
                        if (mc.thePlayer.getDistanceSqToCenter(lastPlaceBlock) <10) {
                            sendPacket(afterPlaceC08!!)
                        } else {
                            afterPlaceC08 = null
                        }
                    }
                }
                "raytrace" -> {
                    val rayTraceInfo = mc.thePlayer.rayTraceWithServerSideRotation(5.0)
                    if (BlockUtils.getBlock(rayTraceInfo.blockPos) != Blocks.air) {
                        val blockPos = rayTraceInfo.blockPos
                        val hitVec = rayTraceInfo.hitVec
                        val directionVec = rayTraceInfo.sideHit.directionVec
                        val targetPos = rayTraceInfo.blockPos.add(directionVec.x, directionVec.y, directionVec.z)
                        if (mc.thePlayer.entityBoundingBox.intersectsWith(Blocks.stone.getSelectedBoundingBox(mc.theWorld, targetPos))) {
                            sendPacket(C08PacketPlayerBlockPlacement(blockPos, rayTraceInfo.sideHit.index, mc.thePlayer.inventory.getStackInSlot(slot), (hitVec.xCoord - blockPos.x.toDouble()).toFloat(), (hitVec.yCoord - blockPos.y.toDouble()).toFloat(), (hitVec.zCoord - blockPos.z.toDouble()).toFloat()))
                        } else {
                            sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)))
                        }
                    }
                }
            }
            clickDelay = TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
            clickTimer.reset()
        }

        mc.thePlayer.isSprinting = canSprint

        shouldGoDown = downValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false
        if (mc.thePlayer.onGround) {
            // Smooth Zitter
            if (zitterModeValue.equals("smooth")) {
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
            if (!eagleValue.equals("off") && !shouldGoDown) {
                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    val shouldEagle = mc.theWorld.getBlockState(
                        BlockPos(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 1.0, mc.thePlayer.posZ
                        )
                    ).block === Blocks.air
                    if (eagleValue.equals("silent")) {
                        if (eagleSneaking != shouldEagle) {
                            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING))
                        }
                        eagleSneaking = shouldEagle
                    } else mc.gameSettings.keyBindSneak.pressed = shouldEagle
                    placedBlocksWithoutEagle = 0
                } else placedBlocksWithoutEagle++
            }

            // Zitter
            if (zitterModeValue.equals("teleport")) {
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
        
        //Verus
        if (packet is C03PacketPlayer) {
            if (doSpoof) {
                packet.onGround = true
            }
        }

        // AutoBlock
        if (packet is C09PacketHeldItemChange) {
            if(packet.slotId == slot) {
                event.cancelEvent()
            } else {
                slot = packet.slotId
            }
        } else if (packet is C08PacketPlayerBlockPlacement) {
            // c08 item override to solve issues in scaffold and some other modules, maybe bypass some anticheat in future
            packet.stack = mc.thePlayer.inventory.mainInventory[slot]
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState
        towerStatus = false
        // Tower
        if (motionSpeedValue.get()) MovementUtils.setMotion(motionSpeed.get().toDouble())
        towerStatus = (!stopWhenBlockAbove.get() || BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ)) is BlockAir)
        if (towerStatus) {
            // further checks
            when (towerActiveValue.get().lowercase()) {
                "off" -> towerStatus = false
                "always" -> {
                    towerStatus = (mc.gameSettings.keyBindLeft.isKeyDown ||
                            mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown ||
                            mc.gameSettings.keyBindBack.isKeyDown)
                }
                "pressspace" -> {
                    towerStatus = mc.gameSettings.keyBindJump.isKeyDown
                }
                "nomove" -> {
                    towerStatus = !(mc.gameSettings.keyBindLeft.isKeyDown ||
                            mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown ||
                            mc.gameSettings.keyBindBack.isKeyDown) && mc.gameSettings.keyBindJump.isKeyDown
                }
            }
        }
        if (towerStatus) move()

        // Lock Rotation
        if (rotationsValue.get() != "None" && keepLengthValue.get()> 0 && lockRotation != null && silentRotationValue.get()) {
            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, lockRotation, rotationSpeed)
            RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
        }

        // Update and search for new block
        if (event.eventState == EventState.PRE) update()

        // Place block
        if (placeModeValue.equals(eventState.stateName)) place()

        // Reset placeable delay
        if (targetPlace == null && !placeableDelay.equals("OFF")) {
            if (placeableDelay.equals("Smart")) {
                if (lastPlace == 0) {
                    delayTimer.reset()
                }
            } else {
                delayTimer.reset()
            }
        }
    }

    private fun fakeJump() {
        if (!towerFakeJump.get()) {
            return
        }

        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    private fun move() {
        when (towerModeValue.get().lowercase()) {
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
                } else if (mc.thePlayer.motionY < 0.1) {
                    mc.thePlayer.motionY = -0.3
                }
            }
            "motiontp" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.42
                } else if (mc.thePlayer.motionY < 0.23) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
                }
            }
            "packet" -> {
                if (mc.thePlayer.onGround && towerTimer.hasTimePassed(2)) {
                    fakeJump()
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.753, mc.thePlayer.posZ, false))
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
                fakeJump()
            }
            "stablemotion" -> {
                mc.thePlayer.motionY = stableMotionValue.get().toDouble()
                fakeJump()
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
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.001335979112146, mc.thePlayer.posZ)
                    mc.thePlayer.motionY = 0.42
                    jumpGround = mc.thePlayer.posY
                    mc.timer.timerSpeed = 0.75f
                }
            }
            "verus" -> {
                mc.thePlayer.setPosition(mc.thePlayer.posX, (mc.thePlayer.posY * 2).roundToInt().toDouble() / 2, mc.thePlayer.posZ)
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    mc.thePlayer.motionY = 0.5
                    mc.timer.timerSpeed = 0.8f
                    doSpoof = false
                }else{
                    mc.timer.timerSpeed = 1.33f
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.onGround = true
                    doSpoof = true
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
        if (if (!autoBlockValue.equals("off")) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null ||
                    !(mc.thePlayer.heldItem.item is ItemBlock && !InventoryUtils.isBlockListBlock(mc.thePlayer.heldItem.item as ItemBlock))) {
            return
        }

        findBlock(expandLengthValue.get()> 1)
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean) {
        val blockPosition = if (shouldGoDown) {
            if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) {
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ)
            } else {
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ).down()
            }
        } else if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5 && !canSameY) {
            BlockPos(mc.thePlayer)
        } else if (canSameY && lastGroundY <= mc.thePlayer.posY) {
            BlockPos(mc.thePlayer.posX, lastGroundY - 1.0, mc.thePlayer.posZ)
        } else {
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down()
        }
        if (!expand && (!BlockUtils.isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) return
        if (expand) {
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPosition.add(if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                        0, if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0), false)) {
                    return
                }
            }
        } else if (searchValue.get()) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                        return
                    }
                }
            }
        }
    }

    /**
     * Place target block
     */
    private fun place() {
        if (targetPlace == null) {
            if (!placeableDelay.equals("OFF")) {
                if (lastPlace == 0 && placeableDelay.equals("Smart")) delayTimer.reset()
                if (placeableDelay.equals("Normal")) delayTimer.reset()
                if (lastPlace> 0) lastPlace--
            }
            return
        }
        if (!delayTimer.hasTimePassed(delay) || !towerStatus && canSameY && lastGroundY - 1 != targetPlace!!.vec3.yCoord.toInt()) {
            return
        }

        if (!rotationsValue.equals("None")) {
            val rayTraceInfo = mc.thePlayer.rayTraceWithServerSideRotation(5.0)
            when (hitableCheck.get().lowercase()) {
                "simple" -> {
                    if (!rayTraceInfo.blockPos.equals(targetPlace!!.blockPos)) {
                        return
                    }
                }
                "strict" -> {
                    if (!rayTraceInfo.blockPos.equals(targetPlace!!.blockPos) || rayTraceInfo.sideHit != targetPlace!!.enumFacing) {
                        return
                    }
                }
            }
        }

        val isDynamicSprint = sprintValue.equals("dynamic")
        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        if (mc.thePlayer.heldItem == null || !(mc.thePlayer.heldItem.item is ItemBlock && !InventoryUtils.isBlockListBlock(mc.thePlayer.heldItem.item as ItemBlock))) {
            if (autoBlockValue.equals("off")) return
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
            if (autoBlockValue.equals("LiteSpoof") || autoBlockValue.equals("Spoof")) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            } else {
                mc.thePlayer.inventory.currentItem = blockSlot - 36
            }
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }
        if (isDynamicSprint) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
        }
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3)) {
            // delayTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }

            if (swingValue.equals("packet")) {
                mc.netHandler.addToSendQueue(C0APacketAnimation())
            } else if (swingValue.equals("normal")) {
                mc.thePlayer.swingItem()
            }
            lastPlace = 2
            lastPlaceBlock = targetPlace!!.blockPos.add(targetPlace!!.enumFacing.directionVec)
            when (extraClickValue.get().lowercase()) {
                "afterplace" -> {
                    // fake click
                    val blockPos = targetPlace!!.blockPos
                    val hitVec = targetPlace!!.vec3
                    afterPlaceC08 = C08PacketPlayerBlockPlacement(targetPlace!!.blockPos, targetPlace!!.enumFacing.index, itemStack, (hitVec.xCoord - blockPos.x.toDouble()).toFloat(), (hitVec.yCoord - blockPos.y.toDouble()).toFloat(), (hitVec.zCoord - blockPos.z.toDouble()).toFloat())
                }
            }
        }
        if (isDynamicSprint) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
        }

        if (autoBlockValue.equals("LiteSpoof") && blockSlot >= 0) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }

        // Reset
        targetPlace = null
    }

    /**
     * Disable scaffold module
     */
    override fun onDisable() {
        // tolleyStayTick=999
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
        if (safeWalkValue.equals("off") || shouldGoDown) return
        if (safeWalkValue.equals("air") || mc.thePlayer.onGround) event.isSafeWalk = true
    }

    private val barrier = ItemStack(Item.getItemById(166), 0, 0)

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (counterDisplayValue.get()) {
            GlStateManager.pushMatrix()
            val info = LanguageManager.getAndFormat("ui.scaffold.blocks", blocksAmount)
            val scaledResolution = ScaledResolution(mc)
            val width = scaledResolution.scaledWidth
            val height = scaledResolution.scaledHeight
            val slot = InventoryUtils.findAutoBlockBlock()
            var stack: ItemStack = barrier
            if (slot != -1) {
                if (mc.thePlayer.inventory.getCurrentItem() != null) {
                    val handItem = mc.thePlayer.inventory.getCurrentItem().item
                    if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                        stack = mc.thePlayer.inventory.getCurrentItem()
                    }
                }
                if (stack == barrier) {
                    stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                    if (stack == null) {
                        stack = barrier
                    }
                }
            }
            RenderHelper.enableGUIStandardItemLighting()
            mc.renderItem.renderItemIntoGUI(stack, width / 2 - mc.fontRendererObj.getStringWidth(info), (height * 0.6 - mc.fontRendererObj.FONT_HEIGHT * 0.5).toInt())
            RenderHelper.disableStandardItemLighting()
            mc.fontRendererObj.drawCenteredString(info, width / 2f, height * 0.6f, Color.WHITE.rgb, false)
            GlStateManager.popMatrix()
        }
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!markValue.get()) return
        for (i in 0 until (expandLengthValue.get() + 1)) {
            val blockPos = BlockPos(
                mc.thePlayer.posX + if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) { 0.0 } else { 1.0 }) - (if (shouldGoDown) { 1.0 } else { 0.0 }),
                mc.thePlayer.posZ + if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
            )
            val placeInfo = get(blockPos)
            if (BlockUtils.isReplaceable(blockPos) && placeInfo != null) {
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
        if (!BlockUtils.isReplaceable(blockPosition)) return false
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
        )
        var placeRotation: PlaceRotation? = null
        for (side in EnumFacing.values()) {
            val neighbor = blockPosition.offset(side)
            if (!BlockUtils.canBeClicked(neighbor)) continue
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
        if (!rotationsValue.equals("None")) {
            lockRotation = when (rotationsValue.get().lowercase()) {
                "aac" -> {
                    if (!towerStatus) {
                        Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180) + aacYawValue.get(), placeRotation.rotation.pitch)
                    } else {
                        placeRotation.rotation
                    }
                }
                "vanilla" -> {
                    placeRotation.rotation
                }
                "test1" -> {
                    val caluyaw = ((placeRotation.rotation.yaw / 45).roundToInt() * 45).toFloat()
                    Rotation(caluyaw, placeRotation.rotation.pitch)
                }
                "test2" -> {
                    Rotation(((MovementUtils.direction * 180f / Math.PI).toFloat() + 135), placeRotation.rotation.pitch)
                }
                "custom" -> {
                    Rotation(mc.thePlayer.rotationYaw + customYaw.get(), customPitch.get().toFloat())
                }
                else -> return false // this should not happen
            }
            if (silentRotationValue.get()) {
                val limitedRotation =
                    RotationUtils.limitAngleChange(RotationUtils.serverRotation, lockRotation!!, rotationSpeed)
                RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
            } else {
                mc.thePlayer.rotationYaw = lockRotation!!.yaw
                mc.thePlayer.rotationPitch = lockRotation!!.pitch
            }
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    /**
     * @return hotbar blocks amount
     */
    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock && InventoryUtils.canPlaceBlock((itemStack.item as ItemBlock).block)) {
                    amount += itemStack.stackSize
                }
            }
            return amount
        }

    private val rotationSpeed: Float
        get() = (Math.random() * (maxRotationSpeedValue.get() - minRotationSpeedValue.get()) + minRotationSpeedValue.get()).toFloat()

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerStatus) {
            event.cancelEvent()
        }
    }

    val canSprint: Boolean
        get() = MovementUtils.isMoving() && when (sprintValue.get().lowercase()) {
            "always", "dynamic" -> true
            "onground" -> mc.thePlayer.onGround
            "offground" -> !mc.thePlayer.onGround
            else -> false
        }

    override val tag: String
        get() = if (towerStatus) { "Tower" } else { "Normal" }
}
