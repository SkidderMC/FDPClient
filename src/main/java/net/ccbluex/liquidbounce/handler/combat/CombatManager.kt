/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

object CombatManager : MinecraftInstance, Listenable {
    private val lastAttackTimer = MSTimer()

    private var inCombat = false
    var target: EntityLivingBase? = null
        private set
    private val attackedEntityList = mutableListOf<EntityLivingBase>()
    val focusedPlayerList = mutableListOf<EntityPlayer>()

    val onHitEntityListeners = mutableListOf<(EntityLivingBase) -> Unit>()

    val onUpdate = handler<UpdateEvent> {
        if (mc.thePlayer == null) return@handler
        MovementUtils.updateBlocksPerSecond()

        // bypass java.util.ConcurrentModificationException
        val entitiesToRemove = mutableListOf<EntityLivingBase>()

        attackedEntityList.forEach {
            if (it.isDead) {
                EventManager.call(EntityKilledEvent(it))
                entitiesToRemove.add(it)
            }
        }
        attackedEntityList.removeAll(entitiesToRemove)


        inCombat =  lastAttackTimer.hasTimePassed(500).not()

        if (target != null && !inCombat) {
            if (mc.thePlayer.getDistanceToEntity(target) > 7 || target!!.isDead) {
                target = null
            } else {
                inCombat = true
            }
        }
    }

    val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity

        if (target is EntityLivingBase && EntityUtils.isSelected(target, true)) {
            this.target = target
            if (!attackedEntityList.contains(target)) {
                attackedEntityList.add(target)
            }

            onHitEntityListeners.forEach { listener ->
                listener(target)
            }
        }
        lastAttackTimer.reset()
    }


    val onWorld = handler<WorldEvent> {
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