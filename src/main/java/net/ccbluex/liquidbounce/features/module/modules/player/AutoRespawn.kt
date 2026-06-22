/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.exploit.Ghost
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.gui.GuiGameOver

object AutoRespawn : Module("AutoRespawn", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val instant by boolean("Instant", true)
        .describe("Respawn instantly on death without waiting.")
    private val delay by int("Delay", 0, 0..5000, "ms")
        .describe("Delay before respawning in milliseconds.")

    private val deathTimer = MSTimer()
    private var waiting = false

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer

        if (thePlayer == null || Ghost.handleEvents())
            return@handler

        val ready = if (instant) thePlayer.health == 0F || thePlayer.isDead else mc.currentScreen is GuiGameOver && (mc.currentScreen as GuiGameOver).enableButtonsTimer >= 20

        if (!ready) {
            waiting = false
            return@handler
        }

        if (!waiting) {
            waiting = true
            deathTimer.reset()
        }

        if (deathTimer.hasTimePassed(delay)) {
            thePlayer.respawnPlayer()
            mc.displayGuiScreen(null)
            waiting = false
        }
    }
}