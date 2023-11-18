/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.world.WorldSettings
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "LegitReach", category = ModuleCategory.COMBAT)
object LegitReach : Module() {

    var fakePlayer: EntityOtherPlayerMP? = null
    private val aura = BoolValue("Aura", false)
    private val mode = ListValue("Mode", arrayOf("FakePlayer", "IntaveTest", "AllIncomingPackets", "TargetPackets"), "FakePlayer")
    private val pulseDelayValue = IntegerValue("PulseDelay", 200, 50, 500)
    private val intavetesthurttime = IntegerValue("Packets", 5, 0, 30).displayable { mode.equals("IntaveTest") }
    
    private val pulseTimer = MSTimer()
    var currentTarget: EntityLivingBase? = null
    private var shown = false
    
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()


    override fun onEnable() {
        if (mode.equals("AllIncomingPackets")) {
            BlinkUtils.setBlinkState(all = true)
        }
    }
    override fun onDisable() {
        removeFakePlayer()
        clearPackets()
        if (mode.equals("AllIncomingPackets")) {
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
        while (!packets.isEmpty()) {
            PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
        }
        BlinkUtils.releasePacket()
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
            if (pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
                pulseTimer.reset()
                clearPackets()
            }
        }
    }
    
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (aura.get() && !FDPClient.moduleManager[KillAura::class.java]!!.state) return
        
        if (mode.equals("TargetPackets")) {
            if (packet is S14PacketEntity && FDPClient.combatManager.inCombat) {
                if (packet.getEntity(mc.theWorld) == currentTarget) {
                    event.cancelEvent()
                    packets.add(packet as Packet<INetHandlerPlayClient>)
                }
            }
        } else if (mode.equals("AllIncomingPackets")) {
            if (packet.javaClass.simpleName.startsWith("S", ignoreCase = true) && FDPClient.combatManager.inCombat) {
                if (mc.thePlayer.ticksExisted < 20) return
                event.cancelEvent()
                packets.add(packet as Packet<INetHandlerPlayClient>)
            }
        }
    }
}

