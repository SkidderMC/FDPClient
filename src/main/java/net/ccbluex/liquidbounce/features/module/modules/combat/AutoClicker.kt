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
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isLookingOnEntities
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityPitch
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityYaw
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isBlock
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.EnumAction
import net.minecraft.item.ItemBlock
import kotlin.random.Random.Default.nextBoolean

object AutoClicker : Module("AutoClicker", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val simulateDoubleClicking by boolean("SimulateDoubleClicking", false)
    private val cps by intRange("CPS", 5..8, 1..50)

    private val hurtTime by int("HurtTime", 10, 0..10) { left }

    private val right by boolean("Right", true)
    private val left by boolean("Left", true)
    private val jitter by boolean("Jitter", false)
    private val block by boolean("AutoBlock", false) { left }
    private val blockDelay by int("BlockDelay", 50, 0..100) { block }

    private val requiresNoInput by boolean("RequiresNoInput", false) { left }
    private val maxAngleDifference by float("MaxAngleDifference", 30f, 10f..180f) { left && requiresNoInput }
    private val range by float("Range", 3f, 0.1f..5f) { left && requiresNoInput }

    private val onlyBlocks by boolean("OnlyBlocks", true) { right }

    private var rightDelay = generateNewClickTime()
    private var rightLastSwing = 0L
    private var leftDelay = generateNewClickTime()
    private var leftLastSwing = 0L

    private var lastBlocking = 0L

    private val shouldAutoClick
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
        val targetEntity = event.targetEntity as EntityLivingBase

        target = targetEntity
    }

    val onRender3D = handler<Render3DEvent> {
        mc.thePlayer?.let { thePlayer ->
            val time = System.currentTimeMillis()
            val doubleClick = if (simulateDoubleClicking) RandomUtils.nextInt(-1, 1) else 0

            if (block && thePlayer.swingProgress > 0 && !mc.gameSettings.keyBindUseItem.isKeyDown) {
                mc.gameSettings.keyBindUseItem.pressTime = 0
            }

            if (right && mc.gameSettings.keyBindUseItem.isKeyDown && time - rightLastSwing >= rightDelay) {
                if (!onlyBlocks || thePlayer.heldItem?.item is ItemBlock) {
                    handleRightClick(time, doubleClick)
                }
            }

            if (requiresNoInput) {
                val nearbyEntity = getNearestEntityInRange() ?: return@handler
                if (!isLookingOnEntities(nearbyEntity, maxAngleDifference.toDouble())) return@handler

                if (left && shouldAutoClick && time - leftLastSwing >= leftDelay) {
                    handleLeftClick(time, doubleClick)
                } else if (block && !mc.gameSettings.keyBindUseItem.isKeyDown && shouldAutoClick && shouldAutoRightClick() && mc.gameSettings.keyBindAttack.pressTime != 0) {
                    handleBlock(time)
                }
            } else {
                if (left && mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown && shouldAutoClick && time - leftLastSwing >= leftDelay) {
                    handleLeftClick(time, doubleClick)
                } else if (block && mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown && shouldAutoClick && shouldAutoRightClick() && mc.gameSettings.keyBindAttack.pressTime != 0) {
                    handleBlock(time)
                }
            }
        }
    }

    val onTick = handler<UpdateEvent> {
        mc.thePlayer?.let { thePlayer ->

            shouldJitter = !mc.objectMouseOver.typeOfHit.isBlock &&
                    (thePlayer.isSwingInProgress || mc.gameSettings.keyBindAttack.pressTime != 0)

            if (jitter && ((left && shouldAutoClick && shouldJitter)
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

    private fun shouldAutoRightClick() = mc.thePlayer.heldItem?.itemUseAction in arrayOf(EnumAction.BLOCK)

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

    private fun handleBlock(time: Long) {
        if (time - lastBlocking >= blockDelay) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)

            lastBlocking = time
        }
    }

    fun generateNewClickTime() = randomClickDelay(cps.first, cps.last)
}