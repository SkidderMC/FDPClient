/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.ccbluex.liquidbounce.features.module.modules.movement.Freeze
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeLook
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemFireball
import net.minecraft.client.settings.KeyBinding

class FireballFly : FlyMode("Fireball") {


    private val boostValue = FloatValue("${valuePrefix}BoostAmount", 1.2f, 1f, 2f)
    private val jumpValue = BoolValue("${valuePrefix}Jump", true)
    private val jumpFreeze = BoolValue("${valuePrefix}Freeze", false).displayable { jumpValue.get() }
    private val hypixelValue = BoolValue("${valuePrefix}HypixelBypass", false)

    private var velocitypacket = false
    private var ticks = 0
    private var beforeVelo = false

    private var startingSlot = 0
    private var veloStatus = false
    private var start = false

    override fun onEnable() {
        val fbSlot = getFBSlot()
        if (fbSlot == -1) {
            ClientUtils.displayChatMessage("§8[§c§lFireball-Flight§8] §aYou need a fireball to fly.")
            fly.state = false
        } else {
            veloStatus = FDPClient.moduleManager[Velocity::class.java]!!.state
            FDPClient.moduleManager[Velocity::class.java]!!.state = false
            velocitypacket = false
            beforeVelo = true
            ticks = 50
            startingSlot = mc.thePlayer.inventory.currentItem
            mc.thePlayer.inventory.currentItem = fbSlot
            start = false
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = 1.0f
        if (beforeVelo) {
            if (mc.thePlayer.onGround && !start) {
                mc.gameSettings.keyBindForward.pressed = true
                if (jumpValue.get()) {
                    mc.thePlayer.jump()
                    MovementUtils.strafe(0.46f)
                }
                FDPClient.moduleManager[FreeLook::class.java]!!.enable()
                ticks = 0
                start = true
            } else if (ticks == 1) {
                mc.thePlayer.rotationYaw += 180f
                mc.thePlayer.rotationPitch = 80f
                mc.gameSettings.keyBindBack.pressed = true
                mc.gameSettings.keyBindForward.pressed = false
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
            } else if (ticks == 3) {
                mc.thePlayer.rotationYaw += 180f
                mc.thePlayer.rotationPitch = 30f
                FDPClient.moduleManager[FreeLook::class.java]!!.disable()
                mc.gameSettings.keyBindForward.pressed = true
                mc.gameSettings.keyBindBack.pressed = false
                if (jumpValue.get() && jumpFreeze.get()) {
                    FDPClient.moduleManager[Freeze::class.java]!!.state = true
                }
            }
        } else {
            if (ticks > 6) {
                mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
                if (!hypixelValue.get()) fly.state = false
            }
            if (ticks < 25 && hypixelValue.get()) {
                // raven b4 thanks
                mc.thePlayer.motionY = 0.7
            } else if (hypixelValue.get() && ticks > 24) {
                fly.state = false
            }
        }

        ticks ++
        
        if(velocitypacket) {
            mc.thePlayer.motionX *=  boostValue.get().toDouble()
            mc.thePlayer.motionZ *=  boostValue.get().toDouble()
            velocitypacket = false
            beforeVelo = false
            ticks = 0
            FDPClient.moduleManager[Velocity::class.java]!!.state = veloStatus
            if (jumpValue.get() && jumpFreeze.get()) {
                FDPClient.moduleManager[Freeze::class.java]!!.state = false
            }
        }
    }

    override fun onDisable() {
        FDPClient.moduleManager[Velocity::class.java]!!.state = veloStatus
        mc.timer.timerSpeed = 1f
        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
        mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        mc.thePlayer.inventory.currentItem = startingSlot
        FDPClient.moduleManager[FreeLook::class.java]!!.disable()
        FDPClient.moduleManager[Freeze::class.java]!!.state = false
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S27PacketExplosion ) {
            velocitypacket = true
            FDPClient.moduleManager[Freeze::class.java]!!.state = false
        }
    }

    private fun getFBSlot(): Int {
        for(i in 36..45) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && stack.item is ItemFireball) {
                return i - 36
            }
        }
        return -1
    }
}
