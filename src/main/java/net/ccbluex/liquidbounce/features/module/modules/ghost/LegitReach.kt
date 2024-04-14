/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.world.WorldSettings
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "LegitReach", category = ModuleCategory.GHOST)
class LegitReach : Module() {

    var fakePlayer: EntityOtherPlayerMP? = null
    private val aura = BoolValue("Aura", false)
    private val mode = ListValue("Mode", arrayOf("FakePlayer", "IntaveTest", "IncomingBlink"), "IncomingBlink")
    private val pulseDelayValue = IntegerValue("BacktrackPulse", 200, 50, 1000)
    private val maxDelayValue = IntegerValue("MaxBacktrackLength", 700, 50, 2000)
    private val incomingBlink = BoolValue("IncomingBlink", true). displayable { mode.equals("IncomingBlink") }
    private val velocityValue = BoolValue("StopOnVelocity", true). displayable { mode.equals("IncomingBlink") }
    private val outgoingBlink = BoolValue("OutgoingBlink", true).displayable { mode.equals("IncomingBlink") }
    private val attackValue = BoolValue("ReleaseOnAttack", true). displayable { mode.equals("IncomingBlink") }
    private val intavetesthurttime = IntegerValue("Packets", 5, 0, 30).displayable { mode.equals("IntaveTest") }
    
    private val pulseTimer = MSTimer()
    private val maxTimer = MSTimer()
    var currentTarget: EntityLivingBase? = null
    private var shown = false
    
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()

    private var comboCounter = 0
    private var backtrack = false

    private var releasing = false

    override fun onEnable() {
        backtrack = false
        releasing = false
        if (mode.equals("IncomingBlink") && outgoingBlink.get()) {
            BlinkUtils.setBlinkState(off = true, release = true)
        }
    }
    
    override fun onDisable() {
        removeFakePlayer()
        clearPackets()
        if (mode.equals("IncomingBlink") && outgoingBlink.get()) {
            BlinkUtils.setBlinkState(off = true, release = true)
        }
    }

    private fun removeFakePlayer() {
        if (fakePlayer == null) return
        currentTarget = null
        mc.theWorld.removeEntity(fakePlayer)
        fakePlayer = null
    }
    
    private fun clearPackets() {
        releasing = true
        while (!packets.isEmpty()) {
            PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
        }
        releasing = false
        if (outgoingBlink.get())  {
            BlinkUtils.releasePacket()
            if (!backtrack) BlinkUtils.setBlinkState(off = true, release = true)
        }
    }



    private fun attackEntity(entity: EntityLivingBase) {
        val thePlayer = mc.thePlayer ?: return
        thePlayer.swingItem()
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
        if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
            thePlayer.attackTargetEntityWithCurrentItem(entity)
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        comboCounter ++
        if ( mode.equals("FakePlayer") || mode.equals("IntaveTest") ) {
            clearPackets()
            if (fakePlayer == null) {
                currentTarget = event.targetEntity as EntityLivingBase?
                val faker = EntityOtherPlayerMP(
                        mc.theWorld ?: return,
                        mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile ?: return
                )

                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.renderYawOffset = (currentTarget ?: return).renderYawOffset
                faker.copyLocationAndAnglesFrom(currentTarget ?: return)
                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.health = (currentTarget ?: return).health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                    faker.setCurrentItemOrArmor(index, equipmentInSlot)
                }
                (mc.theWorld ?: return).addEntityToWorld(-1337, faker)

                fakePlayer = faker
                shown = true
            } else {
                if (event.targetEntity == fakePlayer) {
                    attackEntity(currentTarget ?: return)
                    event.cancelEvent()
                } else {
                    fakePlayer = null
                    currentTarget = event.targetEntity as EntityLivingBase?
                    shown = false
                }
            }
        } else {
            if (event.targetEntity != currentTarget) {
                clearPackets()
                currentTarget = event.targetEntity as EntityLivingBase?
            }
            currentTarget?.let {
                if (mc.thePlayer.getDistanceToEntityBox(it) > 2.6f) {
                    if (comboCounter >= 2) {
                        if (outgoingBlink.get()) BlinkUtils.setBlinkState(all = true)
                        backtrack = true
                        maxTimer.reset()
                    }
                }
            }
            if (attackValue.get() && outgoingBlink.get()) {
                BlinkUtils.releasePacket()
            }
        }
    }

    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent?) {
            if (!FDPClient.combatManager.inCombat) {
               removeFakePlayer()
            }
        if ( mode.equals("FakePlayer") || mode.equals("IntaveTest") ) {
            if (aura.get() && !FDPClient.moduleManager[KillAura::class.java]!!.state) {
                removeFakePlayer()
            }
            if (mc.thePlayer == null) return
            if (fakePlayer != null && EntityUtils.isRendered(fakePlayer ?: return) && ((currentTarget ?: return).isDead || !EntityUtils.isRendered(
                            currentTarget ?: return
                    ))
            ) {
                removeFakePlayer()
            }
            if (currentTarget != null && fakePlayer != null) {
                (fakePlayer ?: return).health = (currentTarget ?: return).health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                    (fakePlayer ?: return).setCurrentItemOrArmor(index, equipmentInSlot)
                }
            }
            if (mode.equals("IntaveTest") && mc.thePlayer.ticksExisted % intavetesthurttime.get() == 0) {
                if (fakePlayer != null) {
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                    (fakePlayer ?: return).renderYawOffset = (currentTarget ?: return).renderYawOffset
                    (fakePlayer ?: return).copyLocationAndAnglesFrom(currentTarget ?: return)
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                }
                pulseTimer.reset()
            } else if (mode.equals("FakePlayer") && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
                if (fakePlayer != null) {
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                    (fakePlayer ?: return).renderYawOffset = (currentTarget ?: return).renderYawOffset
                    (fakePlayer ?: return).copyLocationAndAnglesFrom(currentTarget ?: return)
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                }
                pulseTimer.reset()
            }

            if (!shown && currentTarget != null && (currentTarget ?: return).uniqueID != null && mc.netHandler.getPlayerInfo(
                            (currentTarget ?: return).uniqueID ?: return
                    ) != null && mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile != null
            ) {
                val faker = EntityOtherPlayerMP(
                        mc.theWorld ?: return,
                        mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile ?: return
                )

                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.renderYawOffset = (currentTarget ?: return).renderYawOffset
                faker.copyLocationAndAnglesFrom(currentTarget ?: return)
                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.health = (currentTarget ?: return).health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                    faker.setCurrentItemOrArmor(index, equipmentInSlot)
                }
                (mc.theWorld ?: return).addEntityToWorld(-1337, faker)

                fakePlayer = faker
                shown = true
            }
        } else {
            if (pulseTimer.hasTimePassed(pulseDelayValue.get().toLong()) && backtrack) {
                pulseTimer.reset()
                clearPackets()
            }
            if (maxTimer.hasTimePassed(maxDelayValue.get().toLong()) && backtrack) {
                clearPackets()
                backtrack = false
            }
        }
    }
    
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (aura.get() && !FDPClient.moduleManager[KillAura::class.java]!!.state && !FDPClient.moduleManager[LegitAura::class.java]!!.state || !backtrack) {
            clearPackets()
            backtrack = false
            return
        }
        
        if (mode.equals("IncomingBlink") && backtrack) {
            if (packet.javaClass.simpleName.startsWith("S", ignoreCase = true)) {
                if (mc.thePlayer.ticksExisted < 20) return
                if (incomingBlink.get()) {
                    if (packet is S12PacketEntityVelocity && velocityValue.get()) {
                        comboCounter = 0
                        event.cancelEvent()
                        packets.add(packet as Packet<INetHandlerPlayClient>)
                        clearPackets()
                        return 
                    }
                    event.cancelEvent()
                    packets.add(packet as Packet<INetHandlerPlayClient>)
                }
            }
        }
    }
}
