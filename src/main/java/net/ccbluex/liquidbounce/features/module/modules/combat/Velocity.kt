/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@ModuleInfo(name = "Velocity", category = ModuleCategory.COMBAT)
class Velocity : Module() {

    val horizontalValue = FloatValue("Horizontal", 0f, -2f, 2f).displayable { getValue("Simple")?.value == true || getValue("Tick")?.value == true }
    val verticalValue = FloatValue("Vertical", 0f, -2f, 2f).displayable { getValue("Simple")?.value == true || getValue("Tick")?.value == true }
    val chanceValue = IntegerValue("Chance", 100, 0, 100).displayable { getValue("Simple")?.value == true || getValue("Tick")?.value == true }
    val velocityTickValue = IntegerValue("VelocityTick", 1, 0, 10).displayable { getValue("Tick")?.value == true }
    val onlyGroundValue = BoolValue("OnlyGround", false)
    val onlyCombatValue = BoolValue("OnlyCombat", false)
    // private val onlyHitVelocityValue = BoolValue("OnlyHitVelocity",false)
    private val noFireValue = BoolValue("noFire", false)

    private val overrideDirectionValue = ListValue("OverrideDirection", arrayOf("None", "Hard", "Offset"), "None")
    private val overrideDirectionYawValue = FloatValue("OverrideDirectionYaw", 0F, -180F, 180F)
        .displayable { !overrideDirectionValue.equals("None") }
    private val mode = LinkedList<BoolValue>()
    private val mode2 = LinkedList<VelocityMode>()
    private val settings = arrayListOf<Value<*>>(horizontalValue, verticalValue, chanceValue, velocityTickValue, onlyGroundValue, onlyCombatValue, noFireValue, overrideDirectionValue, overrideDirectionYawValue)

    private val modeList = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.velocitys",  VelocityMode::class.java)
        .map { it.newInstance() as VelocityMode }
        .sortedBy { it.modeName }
        .forEach {
            val modulesMode = object : BoolValue(it.modeName, false) {
                override fun onChange(oldValue: Boolean, newValue: Boolean) {
                    if (state) {
                        if (newValue && !oldValue) {
                            it.onEnable()
                        } else if (!newValue && oldValue) {
                            it.onDisable()
                        }
                    }
                }
            }
            settings.add(modulesMode)
            mode.add(modulesMode)
            mode2.add(it)
        }



    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.velocitys", VelocityMode::class.java)
        .map { it.newInstance() as VelocityMode }
        .sortedBy { it.modeName }



    override fun onEnable() {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onEnable()
            }
        }
        antiDesync = false
        needReset = true
    }

    val velocityTimer = MSTimer()
    var wasTimer = false
    var velocityInput = false
    var velocityTick = 0

    var antiDesync = false

    var needReset = true

    var displayTag = ""


    override fun onDisable() {
        antiDesync = false
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.capabilities.flySpeed = 0.05f
        mc.thePlayer.noClip = false

        mc.timer.timerSpeed = 1F
        mc.thePlayer.speedInAir = 0.02F

        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onDisable()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        displayTag = ""
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onUpdate(event)
                displayTag += it.modeName + " "
            }
        }
        if (displayTag == "Simple " ) {
            displayTag = "${(horizontalValue.get() * 100).toInt()}% ${(verticalValue.get() * 100).toInt()}% ${chanceValue.get()}%"
        }
        if (displayTag.count { it == ' ' } > 3) {
            displayTag = (displayTag.count { it == ' ' } + 1).toString()
        }
        if (wasTimer) {
            mc.timer.timerSpeed = 1f
            wasTimer = false
        }
        if(velocityInput) {
            velocityTick++
        }else velocityTick = 0

        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !FDPClient.combatManager.inCombat)) {
            return
        }
        if (noFireValue.get() && mc.thePlayer.isBurning) return
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onVelocity(event)
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onMotion(event)
            }
        }
    }
    
    @EventTarget
    fun onAttack(event: AttackEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onAttack(event)
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent){
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onStrafe(event)
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onPacket(event)
            }
        }
        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !FDPClient.combatManager.inCombat)) {
            return
        }

        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            // if(onlyHitVelocityValue.get() && packet.getMotionY()<400.0) return
            if (noFireValue.get() && mc.thePlayer.isBurning) return
            velocityTimer.reset()
            velocityTick = 0

            if (!overrideDirectionValue.equals("None")) {
                val yaw = Math.toRadians(
                    if (overrideDirectionValue.get() == "Hard") {
                        overrideDirectionYawValue.get()
                    } else {
                        mc.thePlayer.rotationYaw + overrideDirectionYawValue.get() + 90
                    }.toDouble()
                )
                val dist = sqrt((packet.motionX * packet.motionX + packet.motionZ * packet.motionZ).toDouble())
                val x = cos(yaw) * dist
                val z = sin(yaw) * dist
                packet.motionX = x.toInt()
                packet.motionZ = z.toInt()
            }

            modes.forEach {
                if(getValue(it.modeName)?.value == true) {
                    it.onVelocityPacket(event)
                }
            }
        }

    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onWorld(event)
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onMove(event)
            }
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onBlockBB(event)
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onJump(event)
            }
        }
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onStep(event)
            }
        }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        modes.forEach {
            if(getValue(it.modeName)?.value == true) {
                it.onTick(event)
            }
        }
    }
    override val tag: String
        get() = displayTag

    private val modeValue: List<Value<*>> get() = settings

    override val values = modeValue.toMutableList().also {
        modes.map { mode ->
            mode.values.forEach { value ->
                it.add(value.displayable { getValue(mode.modeName)?.value == true })
            }
        }
    }
}
