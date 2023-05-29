/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.potion.Potion
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.sqrt
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing


class HypixelHopSpeed : SpeedMode("HypixelHop") {

    private val bypassMode = ListValue("${valuePrefix}BypassMode", arrayOf("Latest", "Legit", "GroundStrafe"), "Latest")
    private val customSpeedBoost = FloatValue("${valuePrefix}SpeedPotJumpModifier", 0.1f, 0f, 0.4f)
    private val yMotion = FloatValue("${valuePrefix}JumpYMotion", 0.4f, 0.395f, 0.42f)
    private val yPort = BoolValue("${valuePrefix}OldHypixelYPort", false)
    private val yPort2 = BoolValue("${valuePrefix}BadNCPYPort", false)
    private val yPort3 = BoolValue("${valuePrefix}SemiHypixelYPort", true)
    private val yPort4 = BoolValue("${valuePrefix}MicroYPort", true)
    private val damageBoost = BoolValue("${valuePrefix}DamageBoost", false)
    private val damageStrafe = BoolValue("${valuePrefix}StrafeOnDamage", true)
    private val sussyPacket = BoolValue("${valuePrefix}Rise6sussyPacket", true)


    private var minSpeed = 0.0
  
    private var wasOnGround = false
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
            if (mc.thePlayer.motionY <= 0.03 && mc.thePlayer.motionY >= -0.03) {
                mc.thePlayer.motionY = (mc.thePlayer.motionY - 0.08) * 0.98
            }
        }
        if (yPort4.get()) {
    
            if (damagedTicks < 0) {
                if (offGroundTicks == 1)
                    mc.thePlayer.motionY -= 0.005
                else if (offGroundTicks == 3)
                    mc.thePlayer.motionY -= 0.001
            }
        }
        
        
        if (damageStrafe.get()) {
            if (damagedTicks > 2) {
                MovementUtils.strafe(MovementUtils.getSpeed() * 0.99f)
            }
        }

        damagedTicks -= 1
        
        
        
        when (bypassMode.get().lowercase()) {
            
            "latest" -> {
                if (sussyPacket.get()) 
                    PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(-1,-1,-1), EnumFacing.UP))
                    
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    
                    val minSpeed = 0.42f + 0.05f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1).toFloat()
                    MovementUtils.strafe(MovementUtils.getSpeed() * (1.0 + 0.008 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)).toFloat())
                    if (MovementUtils.getSpeed() < minSpeed) {
                        MovementUtils.strafe(minSpeed)
                    }
                    MovementUtils.strafe(MovementUtils.getSpeed() * 0.99f)
                    
                    
                } else {
                    
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        mc.thePlayer.motionX *= (1.0003 + 0.0008 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
                        mc.thePlayer.motionZ *= (1.0003 + 0.0008 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
                    }
                    
                    mc.thePlayer.speedInAir = 0.02f + 0.001f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1).toFloat()
                    
                }
            }
            
            "legit" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                }
            }
            "groundstrafe" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    MovementUtils.strafe(MovementUtils.getSpeed())
                }
            }
        }
    }
                    
   
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            if (!FDPClient.combatManager.inCombat) {
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
