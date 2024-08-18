/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

object CombatManager : MinecraftInstance(), Listenable {
    private val lastAttackTimer = MSTimer()

    var inCombat = false
        private set
    var target: EntityLivingBase? = null
        private set
    private val attackedEntityList = mutableListOf<EntityLivingBase>()
    val focusedPlayerList = mutableListOf<EntityPlayer>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer == null) return
        MovementUtils.updateBlocksPerSecond()

        // bypass java.util.ConcurrentModificationException
        attackedEntityList.map { it }.forEach {
            if (it.isDead) {
                FDPClient.eventManager.callEvent(EntityKilledEvent(it))
                attackedEntityList.remove(it)
            }
        }

        inCombat = false

        if (!lastAttackTimer.hasTimePassed(500)) {
            inCombat = true
            return
        }

        if (target != null) {
            if (mc.thePlayer.getDistanceToEntity(target) > 7 || !inCombat || target!!.isDead) {
                target = null
            } else {
                inCombat = true
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity

        if (target is EntityLivingBase && EntityUtils.isSelected(target, true)) {
            this.target = target
            if (!attackedEntityList.contains(target)) {
                attackedEntityList.add(target)
            }
        }
        lastAttackTimer.reset()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        inCombat = false
        target = null
        attackedEntityList.clear()
        focusedPlayerList.clear()
    }

    fun isFocusEntity(entity: EntityPlayer): Boolean {
        if (focusedPlayerList.isEmpty()) {
            return true // no need 2 focus
        }

        return focusedPlayerList.contains(entity)
    }

    override fun handleEvents() = true
}