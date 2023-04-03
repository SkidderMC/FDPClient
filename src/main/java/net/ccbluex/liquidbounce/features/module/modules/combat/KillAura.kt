/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix
import net.ccbluex.liquidbounce.features.module.modules.movement.TargetStrafe
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.rayTraceWithServerSideRotation
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {
    /**
     * OPTIONS
     */

    private val attackDisplay = BoolValue("Attack Options:", true)
    // CPS
    private val maxCpsValue: IntegerValue = object : IntegerValue("MaxCPS", 12, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCpsValue.get()
            if (i > newValue) set(i)

            attackDelay = getAttackDelay(minCpsValue.get(), this.get())
        }
    }.displayable {!simulateCooldown.get() && attackDisplay.get()} as IntegerValue

    private val minCpsValue: IntegerValue = object : IntegerValue("MinCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCpsValue.get()
            if (i < newValue) set(i)

            attackDelay = getAttackDelay(this.get(), maxCpsValue.get())
        }
    }.displayable {!simulateCooldown.get() && attackDisplay.get()} as IntegerValue
    
    private val simulateCooldown = BoolValue("SimulateCooldown", false).displayable { attackDisplay.get() }
    private val cooldownNoDupAtk = BoolValue("NoDuplicateAttack", false).displayable { simulateCooldown.get() && attackDisplay.get() }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10).displayable { attackDisplay.get() }

    // Range  
    val rangeValue = object : FloatValue("Range", 3.7f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { attackDisplay.get() } as FloatValue
    private val throughWallsRangeValue = object : FloatValue("ThroughWallsRange", 1.5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = rangeValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { attackDisplay.get() } as FloatValue
    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f)
    private val swingRangeValue = object : FloatValue("SwingRange", 5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
            if (maxRange > newValue) set(maxRange)
        }
    }.displayable { attackDisplay.get() } as FloatValue
    private val discoverRangeValue = FloatValue("DiscoverRange", 6f, 0f, 8f).displayable { attackDisplay.get() }
   
    private val hitselectValue = BoolValue("Hitselect", false).displayable { attackDisplay.get() }
    private val hitselectRangeValue = FloatValue("HitselectRange", 2.7f, 2f, 4f).displayable { attackDisplay.get() }
    
    private val blinkCheck = BoolValue("BlinkCheck", true).displayable { attackDisplay.get() }
    private val noScaffValue = BoolValue("NoScaffold", true).displayable { attackDisplay.get() }
    private val noFlyValue = BoolValue("NoFly", false).displayable { attackDisplay.get() }

    // Bypass
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal").displayable { attackDisplay.get() }
    private val attackTimingValue = ListValue("AttackTiming", arrayOf("All", "Pre", "Post"), "All").displayable { attackDisplay.get() }
    private val keepSprintValue = BoolValue("KeepSprint", true).displayable { attackDisplay.get() }
    

    private val noBadPacketsValue = BoolValue("NoBadPackets", false).displayable { attackDisplay.get() }

    // AutoBlock
    val autoBlockValue = ListValue("AutoBlock", arrayOf("Range", "Fake", "Off"), "Range").displayable { attackDisplay.get() }

    private val autoBlockRangeValue = object : FloatValue("AutoBlockRange", 5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !autoBlockValue.equals("Off") && autoBlockValue.displayable }
    private val blockTimingValue =          ListValue("BlockTiming", arrayOf("Pre", "Post", "Both"), "Pre").displayable { autoBlockValue.equals("Range") && autoBlockValue.displayable }
    private val autoBlockPacketValue =      ListValue("AutoBlockPacket", arrayOf("AfterTick", "AfterAttack", "Vanilla", "Delayed", "Legit", "OldIntave", "OldHypixel"), "Vanilla").displayable { autoBlockValue.equals("Range") && autoBlockValue.displayable }
    private val interactAutoBlockValue =    BoolValue("InteractAutoBlock", false).displayable { autoBlockPacketValue.displayable }
    private val blockRateValue =            IntegerValue("BlockRate", 100, 1, 100).displayable { autoBlockPacketValue.displayable }
    private val alwaysBlockDisplayValue =   BoolValue("AlwaysRenderBlocking", true).displayable { autoBlockValue.displayable && autoBlockValue.equals("Range") }
    
    // Raycast
    private val raycastValue = BoolValue("RayCast", true).displayable { attackDisplay.get() }
    private val raycastTargetValue = BoolValue("RaycastOnlyTarget", false).displayable { raycastValue.get() && raycastValue.displayable }
    
    private val rotationDisplay = BoolValue("Rotation Options:", true)
    // Modes
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Fov", "LivingTime", "Armor", "HurtTime", "RegenAmplifier"), "Fov").displayable { rotationDisplay.get() }
    private val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch").displayable { rotationDisplay.get() }
    private val switchDelayValue = IntegerValue("SwitchDelay", 15, 1, 2000).displayable { targetModeValue.equals("Switch") && rotationDisplay.get() }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50).displayable { targetModeValue.equals("Multi") && rotationDisplay.get()}

    // Rotations
    private val rotationModeValue = ListValue(
        "RotationMode",
        arrayOf("None", "LiquidBounce", "ForceCenter", "SmoothCenter", "SmoothLiquid", "LockView", "OldMatrix", "Test"),
        "LiquidBounce"
    ).displayable { rotationDisplay.get() }
    
    private val silentRotationValue = BoolValue("SilentRotation", true).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}
    
    // Others
    private val hitAbleValue = BoolValue("AlwaysHitAble", true).displayable { rotationDisplay.get() }
    private val fovValue = FloatValue("FOV", 180f, 0f, 180f).displayable { rotationDisplay.get() }

    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 360f, 1f, 360f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { rotationDisplay.get() } as FloatValue

    private val minTurnSpeedValue: FloatValue = object : FloatValue("MinTurnSpeed", 360f, 1f, 360f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { rotationDisplay.get() } as FloatValue

    private val rotationSmoothModeValue = ListValue("SmoothMode", arrayOf("Custom", "Line", "Quad", "Sine", "QuadSine"), "Custom").displayable { rotationDisplay.get() }
    private val rotationSmoothValue = FloatValue("CustomSmooth", 2f, 1f, 10f).displayable { rotationSmoothModeValue.equals("Custom") && rotationDisplay.get()}
    
    // Random Value
    private val randomCenterModeValue = ListValue("RandomCenter", arrayOf("Off", "Cubic", "Horizonal", "Vertical"), "Off").displayable { rotationDisplay.get() }
    private val randomCenRangeValue = FloatValue("RandomRange", 0.0f, 0.0f, 1.2f).displayable { !randomCenterModeValue.equals("Off") && rotationDisplay.get()}
    
    // Keep Rotate
    private val rotationRevValue = BoolValue("RotationReverse", false).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}
    private val rotationRevTickValue = IntegerValue("RotationReverseTick", 5, 1, 20).displayable {  rotationRevValue.get() && rotationRevValue.displayable }
    private val keepDirectionValue = BoolValue("KeepDirection", true).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}
    private val keepDirectionTickValue = IntegerValue("KeepDirectionTick", 15, 1, 20).displayable { keepDirectionValue.get() && keepDirectionValue.displayable }
    private val rotationDelayValue = BoolValue("RotationDelay", false).displayable { !rotationModeValue.equals("None") && rotationDisplay.get() }
    private val rotationDelayMSValue = IntegerValue("RotationDelayMS", 300, 0, 1000).displayable { rotationDelayValue.get() && rotationDelayValue.displayable }
      
    // Backtrace
    //private val backtraceValue = BoolValue("Backtrace", false)
   

    // Predict
    private val predictValue = BoolValue("Predict", true).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}

    private val maxPredictSizeValue: FloatValue = object : FloatValue("MaxPredictSize", 1f, -2f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSizeValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue

    private val minPredictSizeValue: FloatValue = object : FloatValue("MinPredictSize", 1f, -2f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSizeValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue
    
    
    private val predictPlayerValue = BoolValue("PredictPlayer", true).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}
    
    private val maxPredictPlayerSizeValue: FloatValue = object : FloatValue("MaxPredictPlayerSize", 1f, -1f, 3f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictPlayerSizeValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictPlayerValue.displayable && predictPlayerValue.get() } as FloatValue

    private val minPredictPlayerSizeValue: FloatValue = object : FloatValue("MinPredictPlayerSize", 1f, -1f, 3f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictPlayerSizeValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictPlayerValue.displayable && predictPlayerValue.get() } as FloatValue
    
    private val bypassDisplay = BoolValue("Bypass Options:", true)
    // Bypass
    val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Silent").displayable { silentRotationValue.get() && !rotationModeValue.equals("None") && bypassDisplay.get() }

    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f).displayable { bypassDisplay.get() }
    private val fakeSwingValue = BoolValue("FakeSwing", true).displayable { failRateValue.get() != 0f && failRateValue.displayable }
    
    private val noInventoryAttackValue = ListValue("NoInvAttack", arrayOf("Spoof", "CancelRun", "Off"), "Off").displayable { bypassDisplay.get() }
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500).displayable { !noInventoryAttackValue.equals("Off") && noInventoryAttackValue.displayable }

    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Liquid", "FDP", "Block", "OtherBlock", "Jello", "Sims", "Lies", "None"), "Jello")
    private val circleValue = BoolValue("Circle", true)
    private val circleRedValue = IntegerValue("CircleRed", 255, 0, 255).displayable { circleValue.get() }
    private val circleGreenValue = IntegerValue("CircleGreen", 255, 0, 255).displayable { circleValue.get() }
    private val circleBlueValue = IntegerValue("CircleBlue", 255, 0, 255).displayable { circleValue.get() }
    private val circleAlphaValue = IntegerValue("CircleAlpha", 255, 0, 255).displayable { circleValue.get() }
    private val circleThicknessValue = FloatValue("CircleThickness", 2F, 1F, 5F).displayable { circleValue.get() }

    /**
     * MODULE
     */

    // Target
    var currentTarget: EntityLivingBase? = null
    private var hitable = false
    private var packetSent = false
    private val prevTargetEntities = mutableListOf<Int>()
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()
    val canFakeBlock: Boolean
        get() = inRangeDiscoveredTargets.isNotEmpty()

    // Attack delay
    private val attackTimer = MSTimer()
    private val switchTimer = MSTimer()
    private val rotationTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Swing
    private var canSwing = false

    // Last Tick Can Be Seen
    private var lastCanBeSeen = false

    // Fake block status
    var blockingStatus = false
    private var espAnimation = 0.0
    private var isUp = true

    val displayBlocking: Boolean
        get() = blockingStatus || (((autoBlockValue.equals("Fake") || (alwaysBlockDisplayValue.get() && autoBlockValue.equals("Range"))) && canFakeBlock))

    private var predictAmount = 1.0f
    private var predictPlayerAmount = 1.0f
    
    // hit select
    private var canHitselect = false
    private val hitselectTimer = MSTimer()
    
    private var delayBlock = false
    private var legitBlocking = 0
    

    private val getAABB: ((Entity) -> AxisAlignedBB) = {
        var aabb = it.entityBoundingBox
        aabb = if (predictValue.get()) aabb.offset(
            (it.posX - it.lastTickPosX) * predictAmount,
            (it.posY - it.lastTickPosY) * predictAmount,
            (it.posZ - it.lastTickPosZ) * predictAmount
        ) else aabb
        aabb = if (predictPlayerValue.get()) aabb.offset(
            mc.thePlayer.motionX * predictPlayerAmount * -1f,
            mc.thePlayer.motionY * predictPlayerAmount * -1f,
            mc.thePlayer.motionZ * predictPlayerAmount * -1f
        ) else aabb
        aabb.expand(
            it.collisionBorderSize.toDouble(),
            it.collisionBorderSize.toDouble(),
            it.collisionBorderSize.toDouble()
        )
        aabb
    }

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        lastCanBeSeen = false
        delayBlock = false
        legitBlocking = 0

        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        LiquidBounce.moduleManager[TargetStrafe::class.java]!!.doStrafe = false
        currentTarget = null
        hitable = false
        packetSent = false
        prevTargetEntities.clear()
        discoveredTargets.clear()
        inRangeDiscoveredTargets.clear()
        attackTimer.reset()
        clicks = 0
        canSwing = false

        stopBlocking()
        RotationUtils.setTargetRotationReverse(
            RotationUtils.serverRotation,
            if (keepDirectionValue.get()) { keepDirectionTickValue.get() + 1 } else { 1 },
            if (rotationRevValue.get()) { rotationRevTickValue.get() + 1 } else { 0 }
        )
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            packetSent = false
        }
        
        updateHitable()
        val target = this.currentTarget ?: discoveredTargets.first()
        
        if (autoBlockValue.equals("Range") && event.eventState == EventState.POST && autoBlockPacketValue.equals("OldHypixel")) {
             if (mc.thePlayer.swingProgressInt == 1) {
                stopBlocking()
            } else if (mc.thePlayer.swingProgressInt == 2) {
                startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
            }
        }
        
        if (autoBlockValue.equals("Delayed") && delayBlock) {
              startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
              delayBlock = false
        }
            
        
        if ((attackTimingValue.equals("Pre") && !(event.eventState == EventState.PRE)) || (attackTimingValue.equals("Post") && !(event.eventState == EventState.POST)) || attackTimingValue.equals("All"))
            return
        
        runAttackLoop()
        
        if (packetSent && noBadPacketsValue.get()) {
            return
        }
        // AutoBlock
        if (autoBlockValue.equals("Range") && discoveredTargets.isNotEmpty() && (!autoBlockPacketValue.equals("AfterAttack")
                    || discoveredTargets.any { mc.thePlayer.getDistanceToEntityBox(it) > maxRange }) && canBlock
        ) {
            if (mc.thePlayer.getDistanceToEntityBox(target) <= autoBlockRangeValue.get()) {
                startBlocking(
                    target,
                    interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
                )
            } else {
                if (!mc.thePlayer.isBlocking) {
                    stopBlocking()
                }
            }
        }
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(ignoredEvent: UpdateEvent) {
        if (cancelRun) {
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
            return
        }

        if (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())
        ) {
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        updateTarget()

        if (discoveredTargets.isEmpty()) {
            stopBlocking()
            return
        }

        LiquidBounce.moduleManager[TargetStrafe::class.java]!!.targetEntity = currentTarget?:return
        
        LiquidBounce.moduleManager[StrafeFix::class.java]!!.applyForceStrafe(rotationStrafeValue.equals("Silent"), !rotationStrafeValue.equals("Off") && !rotationModeValue.equals("None"))

        if (attackTimingValue.equals("All")) {
            runAttackLoop()
        }
    }

 

    private fun runAttackLoop() { 
        
        // hit select (take damage to get yvelo to crit, for legit killaura)
        if (hitselectValue.get()) {
            if (canHitselect) {
                if (inRangeDiscoveredTargets.isEmpty() && hitselectTimer.hasTimePassed(600L)) canHitselect = false
            } else {
                if (mc.thePlayer.hurtTime > 7) {
                    canHitselect = true
                    hitselectTimer.reset()
                }
                inRangeDiscoveredTargets.forEachIndexed { index, entity -> if ( mc.thePlayer.getDistanceToEntityBox(entity) < hitselectRangeValue.get() ) canHitselect = true; hitselectTimer.reset() }
            }
            if (!canHitselect) return
        }
        
        // legit auto block, block if about to get damage, else, dont block
        if (autoBlockPacketValue.equals("Legit")) {
            if (mc.thePlayer.hurtTime == 1) {
                legitBlocking = 3
            } else if (legitBlocking > 0) {
                legitBlocking--
                if (!discoveredTargets.isEmpty() && !blockingStatus) {
                    val target = this.currentTarget ?: discoveredTargets.first()
                    startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
                    blockingStatus = true
                }
                return
            } else {
                if (blockingStatus) stopBlocking()
                blockingStatus = false
            }
        }
        
        
        if (simulateCooldown.get() && CooldownHelper.getAttackCooldownProgress() < 1.0f) {
            return
        }
        
        if (simulateCooldown.get() && cooldownNoDupAtk.get() && clicks > 0) {
            clicks = 1
        }

        try {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        } catch (e: java.lang.IllegalStateException) {
            return
        }
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val openInventory = noInventoryAttackValue.equals("Spoof") && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Check is not hitable or check failrate
        if (hitable && !failHit) {
            // Close inventory when open
            if (openInventory) {
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            }

            // Attack
            if (!targetModeValue.equals("Multi")) {
                attackEntity(if (raycastValue.get()) {
                    (RaycastUtils.raycastEntity(maxRange.toDouble()) {
                        it is EntityLivingBase && it !is EntityArmorStand && (!raycastTargetValue.get() || EntityUtils.canRayCast(
                            it
                        )) && !EntityUtils.isFriend(it)
                    } ?: currentTarget!!) as EntityLivingBase } else { currentTarget!! })
            } else {
                inRangeDiscoveredTargets.forEachIndexed { index, entity ->
                    if (limitedMultiTargetsValue.get() == 0 || index < limitedMultiTargetsValue.get()) {
                        attackEntity(entity)
                    }
                }
            }

            if (targetModeValue.equals("Switch")) {
                if (switchTimer.hasTimePassed(switchDelayValue.get().toLong())) {
                    prevTargetEntities.add(currentTarget!!.entityId)
                    switchTimer.reset()
                }
            } else {
                prevTargetEntities.add(currentTarget!!.entityId)
            }

            // Open inventory
            if (openInventory) {
                mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
            }
        } else if (fakeSwingValue.get() || canSwing) {
            runSwing()
        }
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Settings
        val fov = fovValue.get()
        val switchMode = targetModeValue.equals("Switch")

        // Find possible targets
        discoveredTargets.clear()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !EntityUtils.isSelected(
                    entity,
                    true
                ) || (switchMode && prevTargetEntities.contains(entity.entityId))
            ) {
                continue
            }

            var distance = mc.thePlayer.getDistanceToEntityBox(entity)
            if (Backtrack.state) {
                val trackedDistance = Backtrack.getNearestTrackedDistance(entity)

                if (distance > trackedDistance) {
                    distance = trackedDistance
                }
            }

            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= discoverRangeValue.get() && (fov == 180F || entityFov <= fov)) {
                discoveredTargets.add(entity)
            }
        }

        // Sort targets by priority
        when (priorityValue.get().lowercase()) {
            "distance" -> discoveredTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> discoveredTargets.sortBy { it.health + it.absorptionAmount } // Sort by health
            "fov" -> discoveredTargets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> discoveredTargets.sortBy { -it.ticksExisted } // Sort by existence
            "armor" -> discoveredTargets.sortBy { it.totalArmorValue } // Sort by armor
            "hurtresistanttime" -> discoveredTargets.sortBy { it.hurtResistantTime } // hurt resistant time
            "regenamplifier" -> discoveredTargets.sortBy { if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(Potion.regeneration).amplifier else -1 }
        }

        inRangeDiscoveredTargets.clear()
        inRangeDiscoveredTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it) < (rangeValue.get() - if (mc.thePlayer.isSprinting) rangeSprintReducementValue.get() else 0F) })

        // Cleanup last targets when no targets found and try again
        if (inRangeDiscoveredTargets.isEmpty() && prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
            return
        }

        // Find best target
        for (entity in discoveredTargets) {
            // Update rotations to current target
            if (!updateRotations(entity)) {
                var success = false
                Backtrack.loopThroughBacktrackData(entity) {
                    if (updateRotations(entity)) {
                        success = true
                        return@loopThroughBacktrackData true
                    }

                    return@loopThroughBacktrackData false
                }

                if (!success) {
                    // when failed then try another target
                    continue
                }
            }

            // Set target to current entity
            if (mc.thePlayer.getDistanceToEntityBox(entity) < discoverRangeValue.get()) {
                currentTarget = entity
                LiquidBounce.moduleManager[TargetStrafe::class.java]!!.targetEntity = currentTarget?:return
                LiquidBounce.moduleManager[TargetStrafe::class.java]!!.doStrafe = LiquidBounce.moduleManager[TargetStrafe::class.java]!!.toggleStrafe()
                return
            }
        }

        currentTarget = null
        LiquidBounce.moduleManager[TargetStrafe::class.java]!!.doStrafe = false
    }

    private fun runSwing() {
        val swing = swingValue.get()
        if (swing.equals("packet", true)) {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        } else if (swing.equals("normal", true)) {
            mc.thePlayer.swingItem()
        }
    }

    /**
     * Attack [entity]
     * @throws IllegalStateException when bad packets protection
     */
    private fun attackEntity(entity: EntityLivingBase) {
        if (packetSent && noBadPacketsValue.get()) return

        // Call attack event
        val event = AttackEvent(entity)
        LiquidBounce.eventManager.callEvent(event)
        if (event.isCancelled) return

        // Stop blocking
        preAttack()

        // Attack target
        runSwing()
        packetSent = true
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
        
        
        swingKeepSprint(entity)

        postAttack(entity)
        
        CooldownHelper.resetLastAttackedTicks()
    }
    
    private fun preAttack() {
        if (mc.thePlayer.isBlocking || blockingStatus) {
            when (autoBlockPacketValue.get().lowercase()) {
                "vanilla" -> null
                "aftertick", "afterattack", "delayed" -> stopBlocking()
                "oldintave" -> {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    blockingStatus = false
                }
                "legit", "oldhypixel" -> null
                else -> null
            }
        }
    }
    
    private fun postAttack(entity: EntityLivingBase) {
        if (mc.thePlayer.isBlocking || (autoBlockValue.equals("Range") && canBlock)) {
            if (blockRateValue.get() > 0 && Random().nextInt(100) <= blockRateValue.get()) {
                when (autoBlockPacketValue.get().lowercase()) {
                    "vanilla", "afterattack", "oldintave" -> startBlocking(entity, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(entity) < maxRange))
                    "aftertick", "oldhypixel", "legit" -> null
                    "delayed" -> delayBlock = true
                    else -> null
                }
            }
        }
    }
    
    private fun swingKeepSprint(entity: EntityLivingBase) {
        if (keepSprintValue.get()) {
            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F) {
                mc.thePlayer.onEnchantmentCritical(entity)
            }
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
            }
        }
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        if (rotationModeValue.equals("None")) {
            return true
        }

        // 视角差异
        val entityFov = RotationUtils.getRotationDifference(RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox), true), RotationUtils.serverRotation)

        // 可以被看见
        if (entityFov <= mc.gameSettings.fovSetting) lastCanBeSeen = true
        else if (lastCanBeSeen) { // 不可以被看见但是上一次tick可以看见
            rotationTimer.reset() // 重置计时器
            lastCanBeSeen = false
        }

        if (predictValue.get()) {
            predictAmount = RandomUtils.nextFloat(maxPredictSizeValue.get(), minPredictSizeValue.get())
        }
        if (predictPlayerValue.get()) {
            predictPlayerAmount = RandomUtils.nextFloat(maxPredictPlayerSizeValue.get(), minPredictPlayerSizeValue.get())
        }

        val boundingBox = if (rotationModeValue.get() == "Test") entity.entityBoundingBox else getAABB(entity)

        val rModes = when (rotationModeValue.get()) {
            "LiquidBounce", "SmoothLiquid", "Derp" -> "LiquidBounce"
            "ForceCenter", "SmoothCenter", "OldMatrix", "Spin", "FastSpin" -> "CenterLine"
            "LockView" -> "CenterSimple"
            "Test" -> "HalfUp"
            else -> "LiquidBounce"
        }

        val (_, directRotation) =
                RotationUtils.calculateCenter(
                        rModes,
                        randomCenterModeValue.get(),
                        (randomCenRangeValue.get()).toDouble(),
                        boundingBox,
                        predictValue.get() && rotationModeValue.get() != "Test",
                        true
                ) ?: return false

        if (rotationModeValue.get() == "OldMatrix") directRotation.pitch = 89.9f

        var diffAngle = RotationUtils.getRotationDifference(RotationUtils.serverRotation, directRotation)
        if (diffAngle < 0) diffAngle = -diffAngle
        if (diffAngle > 180.0) diffAngle = 180.0

        val calculateSpeed = when (rotationSmoothModeValue.get()) {
            "Custom" -> diffAngle / rotationSmoothValue.get()
            "Line" -> (diffAngle / 360) * maxTurnSpeedValue.get() + (1 - diffAngle / 360) * minTurnSpeedValue.get()
            "Quad" -> (diffAngle / 360.0).pow(2.0) * maxTurnSpeedValue.get() + (1 - (diffAngle / 360.0).pow(2.0)) * minTurnSpeedValue.get()
            "Sine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5) * maxTurnSpeedValue.get() + (cos(diffAngle / 360 * Math.PI) * 0.5 + 0.5) * minTurnSpeedValue.get()
            "QuadSine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5).pow(2.0) * maxTurnSpeedValue.get() + (1 - (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5).pow(2.0)) * minTurnSpeedValue.get()
            else -> 360.0
        }
        
        if (!lastCanBeSeen && rotationDelayValue.get() && !rotationTimer.hasTimePassed(rotationDelayMSValue.get().toLong())) return true

        val rotation = when (rotationModeValue.get()) {
            "LiquidBounce", "ForceCenter" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation, directRotation,
                (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat()
            )
            "LockView" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                (180.0).toFloat()
            )
            "SmoothCenter", "SmoothLiquid", "OldMatrix" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                (calculateSpeed).toFloat()
            )
            "Test" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                (calculateSpeed).toFloat()
            )
            else -> return true
        }

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRotationReverse(
                rotation,
                if (keepDirectionValue.get()) { keepDirectionTickValue.get() } else { 1 },
                if (rotationRevValue.get()) { rotationRevTickValue.get() } else { 0 }
            )
        } else {
            rotation.toPlayer(mc.thePlayer)
        }
        return true
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if (currentTarget == null) {
            canSwing = false
            hitable = false
            return
        }
        val entityDist = mc.thePlayer.getDistanceToEntityBox(currentTarget as Entity)
        canSwing = entityDist < swingRangeValue.get() && (currentTarget as EntityLivingBase).hurtTime <= hurtTimeValue.get()
        if (hitAbleValue.get()) {
            hitable = entityDist <= maxRange.toDouble()
            return
        }
        // Disable hitable check if turn speed is zero
        if (maxTurnSpeedValue.get() <= 0F) {
            hitable = true
            return
        }
        val wallTrace = mc.thePlayer.rayTraceWithServerSideRotation(entityDist)
        hitable = RotationUtils.isFaced(currentTarget, maxRange.toDouble()) && (entityDist < throughWallsRangeValue.get() || wallTrace?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) && (currentTarget as EntityLivingBase).hurtTime <= hurtTimeValue.get()
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (autoBlockValue.equals("Range") && mc.thePlayer.getDistanceToEntityBox(interactEntity) > autoBlockRangeValue.get()) {
            return
        }

        if (blockingStatus) {
            return
        }

        if (packetSent && noBadPacketsValue.get()) {
            return
        }

        if (interact) {
            val positionEye = mc.renderViewEntity?.getPositionEyes(1F)

            val expandSize = interactEntity.collisionBorderSize.toDouble()
            val boundingBox = interactEntity.entityBoundingBox.expand(expandSize, expandSize, expandSize)

            val (yaw, pitch) = RotationUtils.targetRotation ?: Rotation(mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch)
            val yawCos = cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val yawSin = sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val pitchCos = -cos(-pitch * 0.017453292F)
            val pitchSin = sin(-pitch * 0.017453292F)
            val range = min(maxRange.toDouble(), mc.thePlayer!!.getDistanceToEntityBox(interactEntity)) + 1
            val lookAt = positionEye!!.addVector(yawSin * pitchCos * range, pitchSin * range, yawCos * pitchCos * range)

            val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
            val hitVec = movingObject.hitVec

            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, Vec3(
                    hitVec.xCoord - interactEntity.posX,
                    hitVec.yCoord - interactEntity.posY,
                    hitVec.zCoord - interactEntity.posZ)
            ))
            //mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
        packetSent = true
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            if (packetSent && noBadPacketsValue.get()) {
                return
            }
            mc.netHandler.addToSendQueue(
                C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN, //if (MovementUtils.isMoving()) BlockPos(-1, -1, -1) else BlockPos.ORIGIN,
                    EnumFacing.DOWN
                )
            )
            blockingStatus = false
            packetSent = true
        }
    }
    
    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(circleThicknessValue.get())
            GL11.glColor4f(
                circleRedValue.get().toFloat() / 255.0F,
                circleGreenValue.get().toFloat() / 255.0F,
                circleBlueValue.get().toFloat() / 255.0F,
                circleAlphaValue.get().toFloat() / 255.0F
            )
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 5) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(
                    cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(),
                    (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get())
                )
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
        }
        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) && currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = getAttackDelay(minCpsValue.get(), maxCpsValue.get())
        }

        discoveredTargets.forEach {
            when (markValue.get().lowercase()) {
                "liquid" -> {
                    RenderUtils.drawPlatform(
                        it,
                        if (it.hurtTime <= 0) Color(37, 126, 255, 170) else Color(255, 0, 0, 170)
                    )
                }
                "block", "otherblock" -> {
                    val bb = it.entityBoundingBox
                    it.entityBoundingBox = getAABB(it).expand(0.2, 0.2, 0.2)
                    RenderUtils.drawEntityBox(
                        it,
                        if (it.hurtTime <= 0) if (it == currentTarget) Color(255, 0, 0, 170) else Color(255, 0, 0, 170) else Color(255, 0, 0, 170),
                        markValue.equals("Block"),
                        true,
                        4f
                    )
                    it.entityBoundingBox = bb
                }
                "fdp" -> {
                    val drawTime = (System.currentTimeMillis() % 1500).toInt()
                    val drawMode = drawTime > 750
                    var drawPercent = drawTime / 750.0
                    // true when goes up
                    if (!drawMode) {
                        drawPercent = 1 - drawPercent
                    } else {
                        drawPercent -= 1
                    }
                    drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                    mc.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)

                    val bb = it.entityBoundingBox
                    val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
                    val height = bb.maxY - bb.minY
                    val x =
                        it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                    val y =
                        (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
                    val z =
                        it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    mc.entityRenderer.disableLightmap()
                    GL11.glLineWidth((radius * 8f).toFloat())
                    GL11.glBegin(GL11.GL_LINE_STRIP)
                    for (i in 0..360 step 10) {
                        RenderUtils.glColor(
                            Color.getHSBColor(
                                if (i < 180) {
                                    HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (i / 180f)
                                } else {
                                    HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (-(i - 360) / 180f)
                                }, 0.7f, 1.0f
                            )
                        )
                        GL11.glVertex3d(x - sin(i * Math.PI / 180F) * radius, y, z + cos(i * Math.PI / 180F) * radius)
                    }
                    GL11.glEnd()

                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }
                "jello" -> { 
                    val drawTime = (System.currentTimeMillis() % 2000).toInt()
                    val drawMode=drawTime>1000
                    var drawPercent=drawTime/1000.0
                    //true when goes up
                    if(!drawMode){
                        drawPercent=1-drawPercent
                    }else{
                        drawPercent-=1
                    }
                    drawPercent=EaseUtils.easeInOutQuad(drawPercent)
                    val points = mutableListOf<Vec3>()
                    val bb=it.entityBoundingBox
                    val radius=bb.maxX-bb.minX
                    val height=bb.maxY-bb.minY
                    val posX = it.lastTickPosX + (it.posX - it.lastTickPosX) * mc.timer.renderPartialTicks
                    var posY = it.lastTickPosY + (it.posY - it.lastTickPosY) * mc.timer.renderPartialTicks
                    if(drawMode){
                        posY-=0.5
                    }else{
                        posY+=0.5
                    }
                    val posZ = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * mc.timer.renderPartialTicks
                    for(i in 0..360 step 7){
                        points.add(Vec3(posX - sin(i * Math.PI / 180F) * radius,posY+height*drawPercent,posZ + cos(i * Math.PI / 180F) * radius))
                    }
                    points.add(points[0])
                    //draw
                    mc.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glBegin(GL11.GL_LINE_STRIP)
                    val baseMove=(if(drawPercent>0.5){1-drawPercent}else{drawPercent})*2
                    val min=(height/60)*20*(1-baseMove)*(if(drawMode){-1}else{1})
                    for(i in 0..20) {
                        var moveFace=(height/60F)*i*baseMove
                        if(drawMode){
                            moveFace=-moveFace
                        }
                        val firstPoint=points[0]
                        GL11.glVertex3d(
                            firstPoint.xCoord - mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                            firstPoint.zCoord - mc.renderManager.viewerPosZ
                        )
                        GL11.glColor4f(1F, 1F, 1F, 0.7F*(i/20F))
                        for (vec3 in points) {
                            GL11.glVertex3d(
                                vec3.xCoord - mc.renderManager.viewerPosX, vec3.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                                vec3.zCoord - mc.renderManager.viewerPosZ
                            )
                        }
                        GL11.glColor4f(0F,0F,0F,0F)
                    }
                    GL11.glEnd()
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }
                "lies" -> {
                    val everyTime = 3000
                    val drawTime = (System.currentTimeMillis() % everyTime).toInt()
                    val drawMode = drawTime > (everyTime / 2)
                    var drawPercent = drawTime / (everyTime / 2.0)
                    // true when goes up
                    if (!drawMode) {
                        drawPercent = 1 - drawPercent
                    } else {
                        drawPercent -= 1
                    }
                    drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                    mc.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7425)
                    mc.entityRenderer.disableLightmap()

                    val bb = it.entityBoundingBox
                    val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
                    val height = bb.maxY - bb.minY
                    val x =
                        it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                    val y =
                        (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
                    val z =
                        it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    val eased = (height / 3) * (if (drawPercent > 0.5) {
                        1 - drawPercent
                    } else {
                        drawPercent
                    }) * (if (drawMode) {
                        -1
                    } else {
                        1
                    })
                    for (i in 5..360 step 5) {
                        val color = Color.getHSBColor(
                            if (i < 180) {
                                HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (i / 180f)
                            } else {
                                HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (-(i - 360) / 180f)
                            }, 0.7f, 1.0f
                        )
                        val x1 = x - sin(i * Math.PI / 180F) * radius
                        val z1 = z + cos(i * Math.PI / 180F) * radius
                        val x2 = x - sin((i - 5) * Math.PI / 180F) * radius
                        val z2 = z + cos((i - 5) * Math.PI / 180F) * radius
                        GL11.glBegin(GL11.GL_QUADS)
                        RenderUtils.glColor(color, 0f)
                        GL11.glVertex3d(x1, y + eased, z1)
                        GL11.glVertex3d(x2, y + eased, z2)
                        RenderUtils.glColor(color, 150f)
                        GL11.glVertex3d(x2, y, z2)
                        GL11.glVertex3d(x1, y, z1)
                        GL11.glEnd()
                    }

                    GL11.glEnable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7424)
                    GL11.glColor4f(1f, 1f, 1f, 1f)
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }
             
                "sims" -> {
                    val radius = 0.15f
                    val side = 4
                    GL11.glPushMatrix()
                    GL11.glTranslated(
                        it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX,
                        (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + it.height * 1.1,
                        it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    )
                    GL11.glRotatef(-it.width, 0.0f, 1.0f, 0.0f)
                    GL11.glRotatef((mc.thePlayer.ticksExisted + mc.timer.renderPartialTicks) * 5, 0f, 1f, 0f)
                    RenderUtils.glColor(if (it.hurtTime <= 0) Color(80, 255, 80) else Color(255, 0, 0))
                    RenderUtils.enableSmoothLine(1.5F)
                    val c = Cylinder()
                    GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
                    c.draw(0F, radius, 0.3f, side, 1)
                    c.drawStyle = 100012
                    GL11.glTranslated(0.0, 0.0, 0.3)
                    c.draw(radius, 0f, 0.3f, side, 1)
                    GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
                    GL11.glTranslated(0.0, 0.0, -0.3)
                    c.draw(0F, radius, 0.3f, side, 1)
                    GL11.glTranslated(0.0, 0.0, 0.3)
                    c.draw(radius, 0F, 0.3f, side, 1)
                    RenderUtils.disableSmoothLine()
                    GL11.glPopMatrix()
                }
            }
        }
    }


    private fun esp(entity : EntityLivingBase, partialTicks : Float, radius : Float) {
        GL11.glPushMatrix()
        GL11.glDisable(3553)
        RenderUtils.startSmooth()
        GL11.glDisable(2929)
        GL11.glDepthMask(false)
        GL11.glLineWidth(1.0F)
        GL11.glBegin(3)
        val x: Double = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.renderManager.viewerPosX
        val y: Double = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.renderManager.viewerPosY
        val z: Double = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.renderManager.viewerPosZ
        for (i in 0..360) {
            val rainbow = Color(Color.HSBtoRGB((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
            GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
            GL11.glVertex3d(x + radius * cos(i * 6.283185307179586 / 45.0), y + espAnimation, z + radius * sin(i * 6.283185307179586 / 45.0))
        }
        GL11.glEnd()
        GL11.glDepthMask(true)
        GL11.glEnable(2929)
        RenderUtils.endSmooth()
        GL11.glEnable(3553)
        GL11.glPopMatrix()
    }

    /**
     * Attack Delay
     */
    private fun getAttackDelay(minCps: Int, maxCps: Int): Long {
        return TimeUtils.randomClickDelay(minCps.coerceAtMost(maxCps), minCps.coerceAtLeast(maxCps))
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || (blinkCheck.get() && LiquidBounce.moduleManager[Blink::class.java]!!.state) || LiquidBounce.moduleManager[FreeCam::class.java]!!.state ||
                (noScaffValue.get() && LiquidBounce.moduleManager[Scaffold::class.java]!!.state) || (noFlyValue.get() && LiquidBounce.moduleManager[Fly::class.java]!!.state) || (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get()))
        

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    /**
     * Range
     */
    private val maxRange: Float
        get() = max(rangeValue.get(), throughWallsRangeValue.get())

    /**
     * HUD Tag
     */

    override val tag: String
        get() = targetModeValue.get() + ", " + autoBlockValue.get() + ", Reach:" + rangeValue.get() + ", " + minCpsValue.get() + " - " + maxCpsValue.get()
}
