/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlockIntersects
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


@ModuleInfo(name = "Spider", category = ModuleCategory.MOVEMENT)
class Spider : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Collide", "Motion", "AAC3.3.12", "AAC4", "Checker", "Vulcan"), "Collide")
    private val motionValue = FloatValue("Motion", 0.42F, 0.1F, 1F)

    private var groundHeight = 0.0
    private var glitch = false
    private var wasTimer = false
    private var ticks = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(wasTimer) {
            mc.timer.timerSpeed = 1.0f
        }

        if (!mc.thePlayer.isCollidedHorizontally || !MovementUtils.isMoving()) {
            if (!collideBlockIntersects(mc.thePlayer.entityBoundingBox) { block: Block? -> block !is BlockAir } || !MovementUtils.isMoving()) {
                ticks = 0
                return
            }
        }

        if(modeValue.get() == "AAC4" && (mc.thePlayer.motionY < 0.0 || mc.thePlayer.onGround)) {
            glitch = true
        }

        if (mc.thePlayer.onGround) {
            groundHeight = mc.thePlayer.posY
        }

        when (modeValue.get().lowercase()) {
            "collide"-> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                }
            }

            "aac4" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    wasTimer = true
                    mc.timer.timerSpeed = 0.4f
                }
            }

            "aac3.3.12" -> {
                if (mc.thePlayer.onGround) {
                    ticks = 0
                }
                ticks++
                when (ticks) {
                    1, 12, 23 -> mc.thePlayer.motionY = 0.43
                    29 -> mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ)
                    else -> if (ticks >= 30) {
                        ticks = 0
                    }
                }
            }

            "motion" -> {
                mc.thePlayer.motionY = motionValue.get().toDouble()
            }

            "checker" -> {
                if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                }
            }

            "vulcan" -> {
                if (mc.thePlayer.onGround) {
                    ticks = 0
                    mc.thePlayer.jump()
                }
                if (ticks >= 3) {
                    ticks = 0
                }
                ticks++
                when (ticks) {
                    2, 3 -> {
                        mc.thePlayer.jump()
                        MovementUtils.resetMotion(false)
                    }
                }
            }
        }
    }
    @EventTarget
    fun onMove(event: MoveEvent) {
        val isInsideBlock = collideBlockIntersects(mc.thePlayer.entityBoundingBox) { block: Block? -> block !is BlockAir }
        if (isInsideBlock && modeValue.get() == "Checker" && mc.thePlayer.movementInput.moveForward > 0.0) {
            event.x = 0.0
            event.z = 0.0
            event.y = motionValue.get().toDouble()
        }
    }
    
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer && glitch) {
            glitch = false
            val yaw = MovementUtils.direction.toFloat()
            packet.x = packet.x - sin(yaw) * 0.00000001
            packet.z = packet.z + cos(yaw) * 0.00000001
        }
        if (packet is C03PacketPlayer && modeValue.get() == "Vulcan") {
            when (ticks) {
                3 -> {
                    val yaw = MovementUtils.direction.toFloat()
                    val randomModulo = Math.random() * 0.03 + 0.22
                    packet.y -= 0.1
                    packet.x += sin(yaw) * randomModulo
                    packet.z -= cos(yaw) * randomModulo
                }

                2 -> {
                    packet.onGround = true
                }
            }
        }
    }
    
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        wasTimer = false
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (modeValue.get() == "Checker" && (collideBlockIntersects(mc.thePlayer.entityBoundingBox) { block: Block? -> block !is BlockAir } || mc.thePlayer.isCollidedHorizontally)) {
            if(event.y > mc.thePlayer.posY)
                event.boundingBox = AxisAlignedBB.fromBounds(0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0)
        }

        if (!mc.thePlayer.isCollidedHorizontally || !MovementUtils.isMoving()) {
            return
        }

        if (mc.thePlayer.motionY > 0.0) return

        when (modeValue.get().lowercase()) {
            "collide", "aac4" -> {
                event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(),
                    event.x + 1.0, floor(mc.thePlayer.posY), event.z + 1.0)
            }
        }
    }
    override val tag: String
        get() = modeValue.get()
}
