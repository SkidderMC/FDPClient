/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S0BPacketAnimation

@ModuleInfo(name = "Criticals", category = ModuleCategory.COMBAT, autoDisable = EnumAutoDisableType.FLAG)
object Criticals : Module() {

    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.criticals", CriticalMode::class.java)
        .map { it.newInstance() as CriticalMode }
        .sortedBy { it.modeName }

    private val mode: CriticalMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Packet") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val delayValue = IntegerValue("Delay", 0, 0, 500)
    val s08FlagValue = BoolValue("FlagPause", true)
    private val s08DelayValue = IntegerValue("FlagPause-Time", 100, 100, 5000).displayable { s08FlagValue.get() }
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val CritTiming = ListValue("CritTiming", arrayOf("Always", "OnGround", "OffGround"), "Always")
    private val lookValue = BoolValue("UseC06Packet", false)
    private val debugValue = BoolValue("DebugMessage", false)
    private val msTimer = MSTimer()
    private val flagTimer = MSTimer()
    private val syncTimer = MSTimer()

    var target = 0

    var antiDesync = false

    override fun onEnable() {
        target = 0
        msTimer.reset()
        flagTimer.reset()
        syncTimer.reset()
        mode.onEnable()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
        mode.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(!state) return
        when (CritTiming.get().lowercase()) {
            "always" -> null
            "onground" -> if (!mc.thePlayer.onGround) return
            "offground" -> if (mc.thePlayer.onGround) return
        }
        mode.onUpdate(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(!state) return
        when (CritTiming.get().lowercase()) {
            "always" -> null
            "onground" -> if (!mc.thePlayer.onGround) return
            "offground" -> if (mc.thePlayer.onGround) return
        }
        mode.onMotion(event)
        if(event.eventState != EventState.PRE) return
        mode.onPreMotion(event)
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if(!state) return
        when (CritTiming.get().lowercase()) {
            "always" -> null
            "onground" -> if (!mc.thePlayer.onGround) return
            "offground" -> if (mc.thePlayer.onGround) return
        }
        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity
            target = entity.entityId
            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                !msTimer.hasTimePassed(delayValue.get().toLong())
            ) {
                return
            }

            if (s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
                return

            antiDesync = true

            mode.onAttack(event)

            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if(!state) return

        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            flagTimer.reset()
            antiDesync = false
            if (debugValue.get()) {
                alert("FLAG")
            }
            /*
            if (s08FlagValue.get()) {
                jState = 0
            }

             */
        }

        if (packet is C03PacketPlayer && (MovementUtils.isMoving() || syncTimer.hasTimePassed(1000L) || msTimer.hasTimePassed(
                ((delayValue.get() / 5) + 75).toLong()
            ))
        )
            antiDesync = false

        if (s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
            return

        mode.onPacket(event)

        if (packet is S0BPacketAnimation && debugValue.get()) {
            if (packet.animationType == 4 && packet.entityID == target) {
                alert("CRIT")
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if(!state) return
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if(!state) return
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if(!state) return
        mode.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        if(!state) return
        mode.onStep(event)
    }

    override val tag: String
        get() = modeValue.get()

    fun sendCriticalPacket(
        xOffset: Double = 0.0,
        yOffset: Double = 0.0,
        zOffset: Double = 0.0,
        ground: Boolean
    ) {
        val x = mc.thePlayer.posX + xOffset
        val y = mc.thePlayer.posY + yOffset
        val z = mc.thePlayer.posZ + zOffset
        if (lookValue.get()) {
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    x,
                    y,
                    z,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch,
                    ground
                )
            )
        } else {
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(x, y, z, ground))
        }
    }

    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                //it.add(value.displayable { modeValue.equals(mode.modeName) })
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}
