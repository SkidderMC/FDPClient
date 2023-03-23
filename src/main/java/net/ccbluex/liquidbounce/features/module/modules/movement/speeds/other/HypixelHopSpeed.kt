/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MathUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.potion.Potion
import net.minecraft.network.play.client.C07PacketPlayerDigging
import kotlin.math.roundToInt
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.sqrt
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing


class HypixelHopSpeed : SpeedMode("HypixelHop") {

    private val bypassMode = ListValue("${valuePrefix}BypassMode", arrayOf("Latest", "OldSmooth", "Stable", "Stable2", "Test2", "TestLowHop", "DortwareHop", "OldSafe", "OldTest", "Legit", "Custom","Test"), "Latest")
    private val slowdownValue = FloatValue("${valuePrefix}SlowdownValue", 0f, -0.15f, 0.5f)
    private val customStartSpeed = FloatValue("${valuePrefix}CustomStartSpeed", 1.3f, 1f, 1.6f).displayable {bypassMode.equals("Custom")}  
    private val customSlowValue = FloatValue("${valuePrefix}CustomSlowAmount", 0.05f, 0.3f, 0.01f).displayable {bypassMode.equals("Custom")}  
    private val customSpeedBoost = FloatValue("${valuePrefix}SpeedPotJumpModifier", 0.1f, 0f, 0.4f)
    private val yMotion = FloatValue("${valuePrefix}JumpYMotion", 0.4f, 0.395f, 0.42f)
    private val yPort = BoolValue("${valuePrefix}OldHypixelYPort", false)
    private val yPort2 = BoolValue("${valuePrefix}BadNCPYPort", false)
    private val yPort3 = BoolValue("${valuePrefix}SemiHypixelYPort", true)
    private val yPort4 = BoolValue("${valuePrefix}MicroYPort", true)
    private val damageBoost = BoolValue("${valuePrefix}DamageBoost", false)
    private val damageStrafe = BoolValue("${valuePrefix}StrafeOnDamage", true)
    private val sussyPacket = BoolValue("${valuePrefix}Rise6sussyPacket", true)

    private var watchdogMultiplier = 1.0
    private var minSpeed = 0.0
    private var oldMotionX = 0.0
    private var oldMotionZ = 0.0
    private var pastX = 0.0
    private var pastZ = 0.0
    private var moveDist = 0.0
    private var wasOnGround = false
    private var stage = 0
    private var offGroundTicks = 0
    private var groundTick = 0
    private var damagedTicks = 0

    override fun onUpdate() {
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0
        } else {
            offGroundTicks += 1
        }
        
        if (yPort.get()) {
            if (mc.thePlayer.motionY < 0.1 && mc.thePlayer.motionY > -0.21 && mc.thePlayer.motionY != 0.0 && !mc.thePlayer.onGround) {
                mc.thePlayer.motionY -= 0.05
            }
        }
        
        if (yPort2.get()) {
            if (offGroundTicks == 6) {
                mc.thePlayer.motionY = (mc.thePlayer.motionY - 0.08) * 0.98
            }
        }
        
        if (yPort3.get()) {
            if (mc.thePlayer.motionY <= 0.003 && mc.thePlayer.motionY >= -0.003) {
                mc.thePlayer.motionY = -0.0784
            }
        }
        if (yPort4.get()) {
    
            if (damagedTicks < 0) {
                if (offGroundTicks == 1)
                    mc.thePlayer.motionY -= 0.005
                else if (offGroundTicks == 3)
                    mc.thePlayer.motionY -= 0.001
                else 
                    mc.thePlayer.motionY -= 0.0009
            }
        }
        
        
        if (damageStrafe.get()) {
            if (damagedTicks > 2) {
                MovementUtils.strafe(MovementUtils.getSpeed() * 0.99f)
            }
        }
        if (mc.thePlayer.onGround) {
            damagedTicks = 0
        }
        damagedTicks -= 1
        
        

        moveDist = MathUtils.getDistance(pastX, pastZ, mc.thePlayer.posX, mc.thePlayer.posZ)
        
        when (bypassMode.get().lowercase()) {
            
            "latest" -> {
                if (sussyPacket.get()) 
                    PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(-1,-1,-1), EnumFacing.UP));
                if (mc.thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.defaultSpeed().toFloat())
                    mc.thePlayer.jump()
                    

                    MovementUtils.strafe(MovementUtils.getSpeed() * 1.005f)
                    if (MovementUtils.getSpeed() < 0.43f) {
                        MovementUtils.strafe(0.43f)
                    }
                    MovementUtils.strafe(MovementUtils.getSpeed() * 0.99f)
                    
                    
                } else {
                    
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        mc.thePlayer.motionX *= (1.0003 + 0.0015 * customSpeedBoost.get().toDouble() * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
                        mc.thePlayer.motionZ *= (1.0003 + 0.0015 * customSpeedBoost.get().toDouble() * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
                    }
                    
                    mc.thePlayer.speedInAir = 0.02f + 0.001f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1).toFloat()
                    
                    oldMotionX = mc.thePlayer.motionX
                    oldMotionZ = mc.thePlayer.motionZ
                    
                    MovementUtils.strafe(MovementUtils.getSpeed())
                    mc.thePlayer.motionX = (mc.thePlayer.motionX + oldMotionX * 5) / 6
                    mc.thePlayer.motionZ = (mc.thePlayer.motionZ + oldMotionZ * 5) / 6
                }
            }
            
            "legit" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                }
            }
                    
            "oldsmooth" -> {

                mc.thePlayer.isSprinting = true

                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        stage = mc.thePlayer.posY.roundToInt()
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = yMotion.get().toDouble()
                        
                        oldMotionX = mc.thePlayer.motionX
                        oldMotionZ = mc.thePlayer.motionZ

                        MovementUtils.strafe(0.47f)
                        mc.thePlayer.motionX = (mc.thePlayer.motionX * 3 + oldMotionX) / 4
                        mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 3 + oldMotionZ) / 4
                    } else {
                        if (MovementUtils.getSpeed() < 0.2) {
                            watchdogMultiplier = 0.2 / MovementUtils.getSpeed().toDouble()
                            mc.thePlayer.motionX *= watchdogMultiplier
                            mc.thePlayer.motionZ *= watchdogMultiplier
                        }
                    }
                }

                mc.thePlayer.jumpMovementFactor = 0.028f
            }
            
            "stable2" -> {

                if (!mc.thePlayer.onGround) {
                    
                    oldMotionX = mc.thePlayer.motionX
                    oldMotionZ = mc.thePlayer.motionZ

                    MovementUtils.strafe()
                    mc.thePlayer.motionX = (mc.thePlayer.motionX * 3 + oldMotionX) / 4
                    mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 3 + oldMotionZ) / 4

                } else if (MovementUtils.isMoving()) {
                    mc.thePlayer.jump()
                }
            }
                
            "stable"-> {


                if (!mc.thePlayer.onGround) {
                    if (!wasOnGround) {
                        oldMotionX = mc.thePlayer.motionX
                        oldMotionZ = mc.thePlayer.motionZ

                        MovementUtils.strafe()
                        mc.thePlayer.motionX = (mc.thePlayer.motionX * 3 + oldMotionX) / 4
                        mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 3 + oldMotionZ) / 4
                        
                        mc.thePlayer.motionX *= 0.99
                        mc.thePlayer.motionZ *= 0.99
                    }
                    
                    oldMotionX = mc.thePlayer.motionX
                    oldMotionZ = mc.thePlayer.motionZ
                    wasOnGround = false
                } else if (MovementUtils.isMoving()) {
                    wasOnGround = true
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
            }
            
            "test2" -> {
                if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                    watchdogMultiplier = 0.560625
                    wasOnGround = true
                } else if (wasOnGround) {
                    watchdogMultiplier = moveDist - 0.66 * (moveDist - 0.2875)
                    wasOnGround = false
                } else {
                    moveDist *= 0.91
                    if (mc.thePlayer.moveStrafing > 0) {
                        watchdogMultiplier += (MovementUtils.getSpeed().toDouble() - moveDist) * 0.2875
                        watchdogMultiplier -= 0.015
                    }
                }

            }
            
            "testlowhop"-> {
                if(MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    watchdogMultiplier = 1.2
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.31999998688697817
                }
            }

            "oldsafe"-> {
                if(MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    watchdogMultiplier = 1.45
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
            }

            "oldtest"-> {
                if(MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    watchdogMultiplier = 1.2
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
            }
            "custom"-> {
                if(MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    watchdogMultiplier = customStartSpeed.get().toDouble()
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
            }
            "dortwarehop" -> {
                if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
                watchdogMultiplier = (moveDist - 0.819999f * (moveDist - 0.28f)).toDouble()
                watchdogMultiplier = watchdogMultiplier / (MovementUtils.getSpeed().toDouble() + 0.001)
                mc.thePlayer.motionX *= watchdogMultiplier
                mc.thePlayer.motionZ *= watchdogMultiplier

            }
            "test" ->{
                if (MovementUtils.isMoving()) {
                    mc.timer.timerSpeed = 1.2f
                    if (mc.thePlayer.onGround) {
                        if (groundTick >= 1) {
                            mc.timer.timerSpeed = 1.5f
                            MovementUtils.strafe(0.43f)
                            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                MovementUtils.strafe(0.63f)
                            }
                            mc.thePlayer.jump()
                        }
                        groundTick++
                    } else {
                        groundTick = 0
                    }
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.fallDistance > 0.0) {
                        MovementUtils.strafe()
                    }
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        mc.thePlayer.jumpMovementFactor = 0.024F//if flag pls edit this
                    }
                }
            }
        }
        if (watchdogMultiplier > 1) {
            when (bypassMode.get().lowercase()) {
                "oldsafe" -> watchdogMultiplier -= 0.2
                "oldtest" -> watchdogMultiplier -= 0.05
                "testlowhop" -> watchdogMultiplier -= 0.05
                "custom" -> watchdogMultiplier -= customSlowValue.get().toDouble()
            }
        } else {
            watchdogMultiplier = 1.0
        }
        
        pastX = mc.thePlayer.posX 
        pastZ = mc.thePlayer.posZ
    }

    override fun onMove(event: MoveEvent) {
        when (bypassMode.get().lowercase()) {
            "testlowhop" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier * ( 0.90151f - slowdownValue.get()).toDouble()).toFloat())
            "test2" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier * ( 1.0f - slowdownValue.get()).toDouble()).toFloat())
            "oldsafe" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier * ( 1.081237f - slowdownValue.get()).toDouble()).toFloat())
            "oldtest" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier * ( 1.0f - slowdownValue.get()).toDouble()).toFloat())
            "custom" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier * ( 1.0f - slowdownValue.get()).toDouble()).toFloat())
            
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            if (!LiquidBounce.combatManager.inCombat) {
                return
            }
            
            if (packet.motionY / 8000.0 > 0.1) {
                if (damageBoost.get()) {
                    event.cancelEvent()
                    val recX = packet.motionX / 8000.0
                    val recZ = packet.motionZ / 8000.0
                    if (sqrt(recX * recX + recZ * recZ) > MovementUtils.getSpeed()) {
                        MovementUtils.strafe(sqrt(recX * recX + recZ * recZ).toFloat() * 1.05f)
                        mc.thePlayer.motionY = packet.motionY / 8000.0
                    }
                }
                damagedTicks = 15
            }
        }
    }
}
