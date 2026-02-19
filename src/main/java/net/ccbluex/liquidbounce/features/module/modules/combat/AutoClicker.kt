/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityPitch
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityYaw
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isBlock
import net.ccbluex.liquidbounce.utils.extensions.isLookingOnEntity
import net.minecraft.item.ItemBlock
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.EnumAction
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.Entity
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextBoolean
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat

/**
 * AutoClicker module - Automatically clicks for you
 *
 * Automatically clicks the left or right mouse button when holding it down.
 * Features randomization (Jitter) and smart clicking logic.
 *
 * Revised by @itsakc-me
 */
object AutoClicker : Module("AutoClicker", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val simulateDoubleClicking by boolean("SimulateDoubleClicking", false)
    private val cps by intRange("CPS", 5..8, 1..50)

    private val hurtTime by int("HurtTime", 10, 0..10) { left }

    private val right by boolean("Right", true)
    private val left by boolean("Left", true)
    private val jitter by boolean("Jitter", false)

    private val requiresNoInput by boolean("RequiresNoInput", false) { left }
    private val maxAngleDifference by float("MaxAngleDifference", 30f, 10f..180f) { left && requiresNoInput }
    private val range by float("Range", 3f, 0.1f..5f) { left && requiresNoInput }

    private val onlyBlocks by boolean("OnlyBlocks", true) { right }

    private var rightDelay = generateNewClickTime()
    private var rightLastSwing = 0L
    private var leftDelay = generateNewClickTime()
    private var leftLastSwing = 0L

    private var lastBlocking = 0L

    val canClick
        get() = mc.thePlayer.capabilities.isCreativeMode || !mc.objectMouseOver.typeOfHit.isBlock

    private var shouldJitter = false

    private var target: EntityLivingBase? = null

    override fun onDisable() {
        rightLastSwing = 0L
        leftLastSwing = 0L
        lastBlocking = 0L
        target = null
    }

    val onAttack = handler<AttackEvent> { event ->
        if (!left) return@handler
        target = event.targetEntity as EntityLivingBase
    }

    val onRender3D = handler<Render3DEvent> {
        mc.thePlayer?.let { thePlayer ->
            val time = System.currentTimeMillis()
            val doubleClick = if (simulateDoubleClicking) RandomUtils.nextInt(-1, 1) else 0

            if (right && mc.gameSettings.keyBindUseItem.isKeyDown && time - rightLastSwing >= rightDelay
                && (!onlyBlocks || thePlayer.heldItem?.item is ItemBlock)) {
                handleRightClick(time, doubleClick)
            }

            if (requiresNoInput && left && canClick && time - leftLastSwing >= leftDelay) {
                val nearbyEntity = getNearestEntityInRange() ?: return@handler
                if (!thePlayer.isLookingOnEntity(nearbyEntity, maxAngleDifference.toDouble())) return@handler

                handleLeftClick(time, doubleClick)
            } else if (left && mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown && canClick && time - leftLastSwing >= leftDelay) {
                handleLeftClick(time, doubleClick)
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        mc.thePlayer?.let { thePlayer ->

            shouldJitter = !mc.objectMouseOver.typeOfHit.isBlock &&
                    (thePlayer.isSwingInProgress || mc.gameSettings.keyBindAttack.pressTime != 0)

            if (jitter && ((left && canClick && shouldJitter)
                        || (right && !thePlayer.isUsingItem && mc.gameSettings.keyBindUseItem.isKeyDown
                        && ((onlyBlocks && thePlayer.heldItem.item is ItemBlock) || !onlyBlocks)))
            ) {

                if (nextBoolean()) thePlayer.fixedSensitivityYaw += nextFloat(-1F, 1F)
                if (nextBoolean()) thePlayer.fixedSensitivityPitch += nextFloat(-1F, 1F)
            }
        }
    }

    private val entities by EntityLookup<EntityLivingBase> {
        isSelected(it, true) && mc.thePlayer.getDistanceToEntityBox(it) <= range
    }

    private fun getNearestEntityInRange(): Entity? {
        val player = mc.thePlayer ?: return null

        return entities.minByOrNull { player.getDistanceToEntityBox(it) }
    }

    fun canItemBlock() = mc.thePlayer.heldItem?.itemUseAction in arrayOf(EnumAction.BLOCK)

    private fun handleLeftClick(time: Long, doubleClick: Int) {
        if (target != null && target!!.hurtTime > hurtTime) return

        repeat(1 + doubleClick) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)

            leftLastSwing = time
            leftDelay = generateNewClickTime()
        }
    }

    private fun handleRightClick(time: Long, doubleClick: Int) {
        repeat(1 + doubleClick) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)

            rightLastSwing = time
            rightDelay = generateNewClickTime()
        }
    }

    fun generateNewClickTime() = randomClickDelay(cps.first, cps.last)
}
