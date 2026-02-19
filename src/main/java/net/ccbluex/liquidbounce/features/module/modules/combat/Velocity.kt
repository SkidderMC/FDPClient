/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.*
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.isOnGround
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.runWithModifiedRaycastResult
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.Vec3
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Velocity module - Modifies knockback taken
 *
 * Reduces or modifies the knockback you take from attacks and explosions.
 * Supports various modes for different anti-cheat systems.
 */
object Velocity : Module("Velocity", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    /**
     * OPTIONS
     */
    private val mode by choices(
        "Mode", arrayOf(
            "Simple", "AAC", "AACPush", "AACZero", "AACv4",
            "Reverse", "SmoothReverse", "Jump", "Glitch", "Legit",
            "GhostBlock", "Vulcan", "S32Packet", "MatrixReduce",
            "IntaveReduce", "Delay", "GrimC03", "Hypixel", "HypixelAir",
            "Click", "BlocksMC", "GrimVertical"
        ), "Simple"
    )

    private val horizontal by float("Horizontal", 0F, -1F..1F) { mode in arrayOf("Simple", "AAC", "Legit") }
    private val vertical by float("Vertical", 0F, -1F..1F) { mode in arrayOf("Simple", "Legit") }

    // Reverse
    private val reverseStrength by float("ReverseStrength", 1F, 0.1F..1F) { mode == "Reverse" }
    private val reverse2Strength by float("SmoothReverseStrength", 0.05F, 0.02F..0.1F) { mode == "SmoothReverse" }

    private val onLook by boolean("onLook", false) { mode in arrayOf("Reverse", "SmoothReverse") }
    private val range by float("Range", 3.0F, 1F..5.0F) {
        onLook && mode in arrayOf("Reverse", "SmoothReverse")
    }
    private val maxAngleDifference by float("MaxAngleDifference", 45.0f, 5.0f..90f) {
        onLook && mode in arrayOf("Reverse", "SmoothReverse")
    }

    // AAC Push
    private val aacPushXZReducer by float("AACPushXZReducer", 2F, 1F..3F) { mode == "AACPush" }
    private val aacPushYReducer by boolean("AACPushYReducer", true) { mode == "AACPush" }

    // AAC v4
    private val aacv4MotionReducer by float("AACv4MotionReducer", 0.62F, 0F..1F) { mode == "AACv4" }

    // Legit
    private val legitDisableInAir by boolean("DisableInAir", true) { mode == "Legit" }

    // Chance
    private val chance by int("Chance", 100, 0..100) { mode == "Jump" || mode == "Legit" }

    // Jump
    private val jumpCooldownMode by choices("JumpCooldownMode", arrayOf("Ticks", "ReceivedHits"), "Ticks")
    { mode == "Jump" }
    private val ticksUntilJump by int("TicksUntilJump", 4, 0..20)
    { jumpCooldownMode == "Ticks" && mode == "Jump" }
    private val hitsUntilJump by int("ReceivedHitsUntilJump", 2, 0..5)
    { jumpCooldownMode == "ReceivedHits" && mode == "Jump" }

    // Ghost Block
    private val hurtTimeRange by intRange("HurtTime", 1..9, 1..10) {
        mode == "GhostBlock"
    }

    // Delay
    private val spoofDelay by int("SpoofDelay", 500, 0..5000) { mode == "Delay" }
    var delayMode = false

    // IntaveReduce
    private val reduceFactor by float("Factor", 0.6f, 0.6f..1f) { mode == "IntaveReduce" }
    private val hurtTime by int("HurtTime", 9, 1..10) { mode == "IntaveReduce" }

    private val pauseOnExplosion by boolean("PauseOnExplosion", true)
    private val ticksToPause by int("TicksToPause", 20, 1..50) { pauseOnExplosion }

    // TODO: Could this be useful in other modes? (Jump?)
    // Limits
    private val limitMaxMotionValue = boolean("LimitMaxMotion", false) { mode == "Simple" }
    private val maxXZMotion by float("MaxXZMotion", 0.4f, 0f..1.9f) { limitMaxMotionValue.isActive() }
    private val maxYMotion by float("MaxYMotion", 0.36f, 0f..0.46f) { limitMaxMotionValue.isActive() }

    //Grim
    private val grimVerticalMode by choices("GrimVerticalMode", arrayOf("Reduce", "1.17", "Vertical"), "Reduce") { mode == "GrimVertical" }
    private val smartVelo by boolean("SmartVelo", true) { mode == "GrimVertical" && grimVerticalMode == "Vertical" }
    private val sendC0FValue by boolean("C0F", false) { mode == "GrimVertical" && grimVerticalMode == "Vertical" }
    private val c0fPacketAmount by int("C0FPacketAmount", 0, 1..40) { mode == "GrimVertical" && grimVerticalMode == "Vertical" && sendC0FValue }
    private val callEvent by boolean("CallEvent", true) { mode == "GrimVertical" && grimVerticalMode == "Vertical" }
    private val via by boolean("Via", true) { mode == "GrimVertical" && (grimVerticalMode == "Vertical" || grimVerticalMode == "Reduce") }


    //0.00075 is added silently

    // Vanilla XZ limits
    // Non-KB: 0.4 (no sprint), 0.9 (sprint)
    // KB 1: 0.9 (no sprint), 1.4 (sprint)
    // KB 2: 1.4 (no sprint), 1.9 (sprint)
    // Vanilla Y limits
    // 0.36075 (no sprint), 0.46075 (sprint)

    private val clicks by intRange("Clicks", 3..5, 1..20) { mode == "Click" }
    private val hurtTimeToClick by int("HurtTimeToClick", 10, 0..10) { mode == "Click" }
    private val whenFacingEnemyOnly by boolean("WhenFacingEnemyOnly", true) { mode == "Click" }
    private val ignoreBlocking by boolean("IgnoreBlocking", false) { mode == "Click" }
    private val clickRange by float("ClickRange", 3f, 1f..6f) { mode == "Click" }
    private val swingMode by choices("SwingMode", arrayOf("Off", "Normal", "Packet"), "Normal") { mode == "Click" }

    /**
     * VALUES
     */
    private val velocityTimer = MSTimer()
    private var hasReceivedVelocity = false

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Jump
    private var limitUntilJump = 0

    // IntaveReduce
    private var intaveTick = 0
    private var lastAttackTime = 0L
    private var intaveDamageTick = 0

    // Delay
    private val packets = LinkedHashMap<Packet<*>, Long>()

    // Grim
    private var timerTicks = 0

    // Vulcan
    private var transaction = false

    // Hypixel
    private var absorbedVelocity = false

    //GrimVertical Variables
    private var attack = false
    private var motionXZ = 0.0
    private var velocityInput = false
    private var canCancel = false
    private var canSpoof = false

    // Pause On Explosion
    private var pauseTicks = 0

    override val tag
        get() = if (mode == "Simple" || mode == "Legit") {
            val horizontalPercentage = (horizontal * 100).toInt()
            val verticalPercentage = (vertical * 100).toInt()

            "$horizontalPercentage% $verticalPercentage%"
        } else mode

    override fun onDisable() {
        pauseTicks = 0
        mc.thePlayer?.speedInAir = 0.02F
        timerTicks = 0
        canCancel = false
        canSpoof = false
        reset()
    }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (thePlayer.isInLiquid || thePlayer.isInWeb || thePlayer.isDead)
            return@handler

        when (mode.lowercase()) {
            "glitch" -> {
                thePlayer.noClip = hasReceivedVelocity

                if (thePlayer.hurtTime == 7)
                    thePlayer.motionY = 0.4

                hasReceivedVelocity = false
            }

            "reverse" -> {
                val nearbyEntity = getNearestEntityInRange()

                if (!hasReceivedVelocity)
                    return@handler

                if (nearbyEntity != null) {
                    if (!thePlayer.onGround) {
                        if (onLook && !thePlayer.isLookingOnEntity(nearbyEntity, maxAngleDifference.toDouble())) {
                            return@handler
                        }

                        speed *= reverseStrength
                    } else if (velocityTimer.hasTimePassed(80))
                        hasReceivedVelocity = false
                }
            }

            "smoothreverse" -> {
                val nearbyEntity = getNearestEntityInRange()

                if (hasReceivedVelocity) {
                    if (nearbyEntity == null) {
                        thePlayer.speedInAir = 0.02F
                        reverseHurt = false
                    } else {
                        if (onLook && !thePlayer.isLookingOnEntity(nearbyEntity, maxAngleDifference.toDouble())) {
                            hasReceivedVelocity = false
                            thePlayer.speedInAir = 0.02F
                            reverseHurt = false
                        } else {
                            if (thePlayer.hurtTime > 0) {
                                reverseHurt = true
                            }

                            if (!thePlayer.onGround) {
                                thePlayer.speedInAir = if (reverseHurt) reverse2Strength else 0.02F
                            } else if (velocityTimer.hasTimePassed(80)) {
                                hasReceivedVelocity = false
                                thePlayer.speedInAir = 0.02F
                                reverseHurt = false
                            }
                        }
                    }
                }
            }

            "aac" -> if (hasReceivedVelocity && velocityTimer.hasTimePassed(80)) {
                thePlayer.motionX *= horizontal
                thePlayer.motionZ *= horizontal
                //mc.thePlayer.motionY *= vertical ?
                hasReceivedVelocity = false
            }

            "aacv4" ->
                if (thePlayer.hurtTime > 0 && !thePlayer.onGround) {
                    val reduce = aacv4MotionReducer
                    thePlayer.motionX *= reduce
                    thePlayer.motionZ *= reduce
                }

            "aacpush" -> {
                if (jump) {
                    if (thePlayer.onGround)
                        jump = false
                } else {
                    // Strafe
                    if (thePlayer.hurtTime > 0 && thePlayer.motionX != 0.0 && thePlayer.motionZ != 0.0)
                        thePlayer.onGround = true

                    // Reduce Y
                    if (thePlayer.hurtResistantTime > 0 && aacPushYReducer && !Speed.handleEvents())
                        thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducer

                    thePlayer.motionX /= reduce
                    thePlayer.motionZ /= reduce
                }
            }

            "aaczero" ->
                if (thePlayer.hurtTime > 0) {
                    if (!hasReceivedVelocity || thePlayer.onGround || thePlayer.fallDistance > 2F)
                        return@handler

                    thePlayer.motionY -= 1.0
                    thePlayer.isAirBorne = true
                    thePlayer.onGround = true
                } else
                    hasReceivedVelocity = false

            "legit" -> {
                if (legitDisableInAir && !isOnGround(0.5))
                    return@handler

                if (mc.thePlayer.maxHurtResistantTime != mc.thePlayer.hurtResistantTime || mc.thePlayer.maxHurtResistantTime == 0)
                    return@handler

                if (nextInt(endExclusive = 100) < chance) {
                    val horizontal = horizontal / 100f
                    val vertical = vertical / 100f

                    thePlayer.motionX *= horizontal.toDouble()
                    thePlayer.motionZ *= horizontal.toDouble()
                    thePlayer.motionY *= vertical.toDouble()
                }
            }

            "intavereduce" -> {
                if (!hasReceivedVelocity) return@handler
                intaveTick++

                if (mc.thePlayer.hurtTime == 2) {
                    intaveDamageTick++
                    if (thePlayer.onGround && intaveTick % 2 == 0 && intaveDamageTick <= 10) {
                        thePlayer.tryJump()
                        intaveTick = 0
                    }
                    hasReceivedVelocity = false
                }
            }

            "hypixel" -> {
                if (hasReceivedVelocity && thePlayer.onGround) {
                    absorbedVelocity = false
                }
            }

            "hypixelair" -> {
                if (hasReceivedVelocity) {
                    if (thePlayer.onGround) {
                        thePlayer.tryJump()
                    }
                    hasReceivedVelocity = false
                }
            }

            "grimvertical" -> {
                when(grimVerticalMode.lowercase()){
                    "1.17" -> {
                        if (canSpoof) {
                            sendPacket(C03PacketPlayer.C06PacketPlayerPosLook(thePlayer.posX, thePlayer.posY, thePlayer.posZ, thePlayer.rotationYaw, thePlayer.rotationPitch, thePlayer.onGround))
                            sendPacket(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(thePlayer).down(), DOWN))
                            canSpoof = false
                        }
                    }
                    "vertical" -> {
                        if (attack) {
                            val entity = mc.thePlayer.entityId

                            if (via) {
                                sendPacket(C02PacketUseEntity(mc.theWorld.getEntityByID(entity), C02PacketUseEntity.Action.ATTACK))
                                if (callEvent)
                                    sendPacket(C0APacketAnimation())
                            }
                            else {
                                if (callEvent)
                                    sendPacket(C0APacketAnimation())
                                sendPacket(C02PacketUseEntity(mc.theWorld.getEntityByID(entity), C02PacketUseEntity.Action.ATTACK))
                            }


                            if (smartVelo && thePlayer.onGround) {
                                thePlayer.motionX *= motionXZ
                                thePlayer.motionZ *= motionXZ
                            } else {
                                thePlayer.motionX *= 0.077760000
                                thePlayer.motionZ *= 0.077760000
                            }
                            velocityInput = false
                            attack = false
                        }
                    }
                }
            }
        }
    }

    private fun getMotionNoXZ(packetEntityVelocity: S12PacketEntityVelocity): Double {
        val vec = Vec3(
            packetEntityVelocity.motionX.toDouble(),
            packetEntityVelocity.motionY.toDouble(),
            packetEntityVelocity.motionZ.toDouble()
        )

        val strength = vec.lengthVector()

        val motionNoXZ: Double
        if (strength >= 20000.0) {
            motionNoXZ = if (mc.thePlayer.onGround) {
                0.06425
            } else {
                0.075
            }
        } else if (strength >= 5000.0) {
            motionNoXZ = if (mc.thePlayer.onGround) {
                0.02625
            } else {
                0.0552
            }
        } else {
            motionNoXZ = 0.0175
        }

        return motionNoXZ
    }

    /**
     * @see net.minecraft.entity.player.EntityPlayer.attackTargetEntityWithCurrentItem
     * Lines 1035 and 1058
     *
     * Minecraft only applies motion slow-down when you are sprinting and attacking, once per tick.
     * An example scenario: If you perform a mouse double-click on an entity, the game will only accept the first attack.
     *
     * This is where we come in clutch by making the player always sprint before dropping
     *
     * [clicks] amount of hits on the target [Entity]
     *
     * We also explicitly-cast the player as an [Entity] to avoid triggering any other things caused from setting new sprint status.
     *
     * @see net.minecraft.client.entity.EntityPlayerSP.setSprinting
     * @see net.minecraft.entity.EntityLivingBase.setSprinting
     */
    val onGameTick = handler<GameTickEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        mc.theWorld ?: return@handler

        if (mode != "Click" || thePlayer.hurtTime != hurtTimeToClick || ignoreBlocking && (thePlayer.isBlocking || KillAura.blockStatus))
            return@handler

        var entity = mc.objectMouseOver?.entityHit

        if (entity == null) {
            if (whenFacingEnemyOnly) {
                var result: Entity? = null

                runWithModifiedRaycastResult(
                    currentRotation ?: thePlayer.rotation,
                    clickRange.toDouble(),
                    0.0
                ) {
                    result = it.entityHit?.takeIf { isSelected(it, true) }
                }

                entity = result
            } else getNearestEntityInRange(clickRange)?.takeIf { isSelected(it, true) }
        }

        entity ?: return@handler

        val swingHand = {
            when (swingMode.lowercase()) {
                "normal" -> thePlayer.swingItem()
                "packet" -> sendPacket(C0APacketAnimation())
            }
        }

        repeat(clicks.random()) {
            thePlayer.attackEntityWithModifiedSprint(entity, true) { swingHand() }
        }
    }

    val onAttack = handler<AttackEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mode != "IntaveReduce" || !hasReceivedVelocity) return@handler

        if (player.hurtTime == hurtTime && System.currentTimeMillis() - lastAttackTime <= 8000) {
            player.motionX *= reduceFactor
            player.motionZ *= reduceFactor
        }

        lastAttackTime = System.currentTimeMillis()
    }

    private fun checkAir(blockPos: BlockPos): Boolean {
        val world = mc.theWorld ?: return false

        if (!world.isAirBlock(blockPos)) {
            return false
        }

        timerTicks = 20

        sendPackets(
            C03PacketPlayer(true),
            C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, DOWN)
        )

        world.setBlockToAir(blockPos)

        return true
    }

    val onPacket = handler<PacketEvent>(priority = 1) { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        val packet = event.packet

        if (!handleEvents())
            return@handler

        if (pauseTicks > 0) {
            pauseTicks--
            return@handler
        }

        if (event.isCancelled)
            return@handler

        if ((packet is S12PacketEntityVelocity && thePlayer.entityId == packet.entityID && packet.motionY > 0 && (packet.motionX != 0 || packet.motionZ != 0))
            || (packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                    && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0))
        ) {
            velocityTimer.reset()

            if (pauseOnExplosion && packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0)
            ) {
                pauseTicks = ticksToPause
            }

            when (mode.lowercase()) {
                "simple" -> handleVelocity(event)

                "aac", "reverse", "smoothreverse", "aaczero", "ghostblock", "intavereduce" -> hasReceivedVelocity = true

                "jump" -> {
                    var packetDirection = 0.0
                    when (packet) {
                        is S12PacketEntityVelocity -> {
                            if (packet.entityID != thePlayer.entityId) return@handler
                            val motionX = packet.motionX.toDouble()
                            val motionZ = packet.motionZ.toDouble()
                            packetDirection = atan2(motionX, motionZ)
                        }
                        is S27PacketExplosion -> {
                            val motionX = thePlayer.motionX + packet.field_149152_f
                            val motionZ = thePlayer.motionZ + packet.field_149159_h
                            packetDirection = atan2(motionX, motionZ)
                        }
                    }
                    val degreePlayer = MovementUtils.direction
                    val degreePacket = Math.floorMod(packetDirection.toInt(), 360).toDouble()
                    var angle = abs(degreePacket + degreePlayer)
                    val threshold = 120.0
                    angle = Math.floorMod(angle.toInt(), 360).toDouble()
                    val inRange = angle in 180 - threshold / 2..180 + threshold / 2
                    if (inRange)
                        hasReceivedVelocity = true
                }

                "glitch" -> {
                    if (!thePlayer.onGround)
                        return@handler

                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "matrixreduce" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        packet.motionX = (packet.motionX * 0.33).toInt()
                        packet.motionZ = (packet.motionZ * 0.33).toInt()

                        if (thePlayer.onGround) {
                            packet.motionX = (packet.motionX * 0.86).toInt()
                            packet.motionZ = (packet.motionZ * 0.86).toInt()
                        }
                    }
                }

                "blocksmc" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        hasReceivedVelocity = true
                        event.cancelEvent()

                        sendPacket(C0BPacketEntityAction(thePlayer, START_SNEAKING))
                        sendPacket(C0BPacketEntityAction(thePlayer, STOP_SNEAKING))
                    }
                }

                "grimc03" -> {
                    if (thePlayer.isMoving) {
                        hasReceivedVelocity = true
                        event.cancelEvent()
                    }
                }

                "hypixel" -> {
                    hasReceivedVelocity = true
                    if (!thePlayer.onGround) {
                        if (!absorbedVelocity) {
                            event.cancelEvent()
                            absorbedVelocity = true
                            return@handler
                        }
                    }

                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        packet.motionX = (thePlayer.motionX * 8000).toInt()
                        packet.motionZ = (thePlayer.motionZ * 8000).toInt()
                    }
                }

                "hypixelair" -> {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "vulcan" -> {
                    event.cancelEvent()
                }

                "s32packet" -> {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "grimvertical" -> {
                    if (packet is S12PacketEntityVelocity) {
                        if (packet.entityID == thePlayer.entityId) {
                            when (grimVerticalMode.lowercase()) {
                                "reduce" -> {
                                    val velocityX = packet.motionX / 8000.0
                                    val velocityZ = packet.motionZ / 8000.0

                                    thePlayer.motionX = velocityX * 0.078
                                    thePlayer.motionZ = velocityZ * 0.078
                                    event.cancelEvent()
                                }

                                "1.17" -> {
                                    canCancel = true
                                    canSpoof = true
                                    event.cancelEvent()
                                }

                                "vertical" -> {
                                    if (packet.motionX == 0 && packet.motionZ == 0 ||
                                        mc.thePlayer == null ||
                                        mc.theWorld.getEntityByID(packet.entityID) != mc.thePlayer
                                    ) {
                                        return@handler
                                    }

                                    velocityInput = true
                                    motionXZ = getMotionNoXZ(packet)

                                    if (thePlayer.isSprinting && thePlayer.serverSprintState && thePlayer.isMoving) {
                                        for (i in 0 until c0fPacketAmount) {
                                            if (sendC0FValue) {
                                                mc.netHandler.addToSendQueue(
                                                    C0FPacketConfirmTransaction(
                                                        nextInt(102, 1000024123),
                                                        nextInt(102, 1000024123).toShort(),
                                                        true
                                                    )
                                                )
                                            }
                                        }
                                        attack = true
                                    }
                                    event.cancelEvent()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mode == "BlocksMC" && hasReceivedVelocity) {
            if (packet is C0BPacketEntityAction) {
                hasReceivedVelocity = false
                event.cancelEvent()
            }
        }

        if (mode == "Vulcan") {
            if (Disabler.handleEvents() && Disabler.verusCombat && (!Disabler.onlyCombat || Disabler.isOnCombat)) return@handler

            if (packet is S32PacketConfirmTransaction) {
                event.cancelEvent()
                sendPacket(
                    C0FPacketConfirmTransaction(
                        if (transaction) 1 else -1,
                        if (transaction) -1 else 1,
                        transaction
                    ), false
                )
                transaction = !transaction
            }
        }

        if (mode == "S32Packet" && packet is S32PacketConfirmTransaction) {

            if (!hasReceivedVelocity)
                return@handler

            event.cancelEvent()
            hasReceivedVelocity = false
        }
    }

    /**
     * Tick Event (Abuse Timer Balance)
     */
    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mode != "GrimC03")
            return@handler

        // Timer Abuse (https://github.com/CCBlueX/LiquidBounce/issues/2519)
        if (timerTicks > 0 && mc.timer.timerSpeed <= 1) {
            val timerSpeed = 0.8f + (0.2f * (20 - timerTicks) / 20)
            mc.timer.timerSpeed = timerSpeed.coerceAtMost(1f)
            --timerTicks
        } else if (mc.timer.timerSpeed <= 1) {
            mc.timer.timerSpeed = 1f
        }

        if (hasReceivedVelocity) {
            val pos = BlockPos(player.posX, player.posY, player.posZ)

            if (checkAir(pos))
                hasReceivedVelocity = false
        }
    }

    /**
     * Delay Mode
     */
    val onDelayPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (event.isCancelled)
            return@handler

        if (mode == "Delay") {
            if (packet is S32PacketConfirmTransaction || packet is S12PacketEntityVelocity) {

                event.cancelEvent()

                // Delaying packet like PingSpoof
                synchronized(packets) {
                    packets[packet] = System.currentTimeMillis()
                }
            }
            delayMode = true
        } else {
            delayMode = false
        }
    }

    /**
     * Reset on world change
     */
    val onWorld = handler<WorldEvent> {
        packets.clear()
    }

    val onGameLoop = handler<GameLoopEvent> {
        if (mode == "Delay")
            sendPacketsByOrder(false)
    }

    private fun sendPacketsByOrder(velocity: Boolean) {
        synchronized(packets) {
            packets.entries.removeAll { (packet, timestamp) ->
                if (velocity || timestamp <= System.currentTimeMillis() - spoofDelay) {
                    PacketUtils.schedulePacketProcess(packet)
                    true
                } else false
            }
        }
    }

    private fun reset() {
        sendPacketsByOrder(true)

        packets.clear()
    }

    val onJump = handler<JumpEvent> { event ->
        val thePlayer = mc.thePlayer

        if (thePlayer == null || thePlayer.isInLiquid || thePlayer.isInWeb)
            return@handler

        when (mode.lowercase()) {
            "aacpush" -> {
                jump = true

                if (!thePlayer.isCollidedVertically)
                    event.cancelEvent()
            }

            "aaczero" ->
                if (thePlayer.hurtTime > 0)
                    event.cancelEvent()
        }
    }

    val onStrafe = handler<StrafeEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mode == "Jump" && hasReceivedVelocity) {
            if (!player.isJumping && nextInt(endExclusive = 100) < chance && shouldJump() && player.isSprinting && player.onGround && player.hurtTime == 9) {
                player.tryJump()
                limitUntilJump = 0
            }
            hasReceivedVelocity = false
            return@handler
        }

        when (jumpCooldownMode.lowercase()) {
            "ticks" -> limitUntilJump++
            "receivedhits" -> if (player.hurtTime == 9) limitUntilJump++
        }
    }

    val onBlockBB = handler<BlockBBEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (mode == "GhostBlock") {
            if (hasReceivedVelocity) {
                if (player.hurtTime in hurtTimeRange) {
                    // Check if there is air exactly 1 level above the player's Y position
                    if (event.block is BlockAir && event.y == mc.thePlayer.posY.toInt() + 1) {
                        event.boundingBox = AxisAlignedBB(
                            event.x.toDouble(),
                            event.y.toDouble(),
                            event.z.toDouble(),
                            event.x + 1.0,
                            event.y + 1.0,
                            event.z + 1.0
                        )
                    }
                } else if (player.hurtTime == 0) {
                    hasReceivedVelocity = false
                }
            }
        }
    }

    private fun shouldJump() = when (jumpCooldownMode.lowercase()) {
        "ticks" -> limitUntilJump >= ticksUntilJump
        "receivedhits" -> limitUntilJump >= hitsUntilJump
        else -> false
    }

    private fun handleVelocity(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            // Always cancel event and handle motion from here
            event.cancelEvent()

            if (horizontal == 0f && vertical == 0f)
                return

            // Don't modify player's motionXZ when horizontal value is 0
            if (horizontal != 0f) {
                var motionX = packet.realMotionX
                var motionZ = packet.realMotionZ

                if (limitMaxMotionValue.get()) {
                    val distXZ = sqrt(motionX * motionX + motionZ * motionZ)

                    if (distXZ > maxXZMotion) {
                        val ratioXZ = maxXZMotion / distXZ

                        motionX *= ratioXZ
                        motionZ *= ratioXZ
                    }
                }

                mc.thePlayer.motionX = motionX * horizontal
                mc.thePlayer.motionZ = motionZ * horizontal
            }

            // Don't modify player's motionY when vertical value is 0
            if (vertical != 0f) {
                var motionY = packet.realMotionY

                if (limitMaxMotionValue.get())
                    motionY = motionY.coerceAtMost(maxYMotion + 0.00075)

                mc.thePlayer.motionY = motionY * vertical
            }
        } else if (packet is S27PacketExplosion) {
            // Don't cancel explosions, modify them, they could change blocks in the world
            if (horizontal != 0f && vertical != 0f) {
                packet.field_149152_f = 0f
                packet.field_149153_g = 0f
                packet.field_149159_h = 0f

                return
            }

            // Unlike with S12PacketEntityVelocity explosion packet motions get added to player motion, doesn't replace it
            // Velocity might behave a bit differently, especially LimitMaxMotion
            packet.field_149152_f *= horizontal // motionX
            packet.field_149153_g *= vertical // motionY
            packet.field_149159_h *= horizontal // motionZ

            if (limitMaxMotionValue.get()) {
                val distXZ =
                    sqrt(packet.field_149152_f * packet.field_149152_f + packet.field_149159_h * packet.field_149159_h)
                val distY = packet.field_149153_g
                val maxYMotion = maxYMotion + 0.00075f

                if (distXZ > maxXZMotion) {
                    val ratioXZ = maxXZMotion / distXZ

                    packet.field_149152_f *= ratioXZ
                    packet.field_149159_h *= ratioXZ
                }

                if (distY > maxYMotion) {
                    packet.field_149153_g *= maxYMotion / distY
                }
            }
        }
    }

    private fun getNearestEntityInRange(range: Float = this.range): Entity? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld.loadedEntityList.filter {
            isSelected(it, true) && player.getDistanceToEntityBox(it) <= range
        }.minByOrNull { player.getDistanceToEntityBox(it) }
    }
}
