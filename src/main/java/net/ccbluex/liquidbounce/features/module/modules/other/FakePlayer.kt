/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.entity.EntityOtherPlayerMP

object FakePlayer : Module("FakePlayer", Category.OTHER, hideModule = false) {

    // Stores the reference to the fake player
    private var fakePlayer: EntityOtherPlayerMP? = null

    /**
     * Initializes the fake player when the module is enabled.
     */
    override fun onEnable() {
        // Create an instance of EntityOtherPlayerMP using the current player's profile
        fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile).apply {
            // Clone the current player's properties
            clonePlayer(mc.thePlayer, true)
            rotationYawHead = mc.thePlayer.rotationYawHead
            // Copy the current player's location and angles
            copyLocationAndAnglesFrom(mc.thePlayer)
        }

        // Add the fake player to the world with a unique negative ID
        mc.theWorld.addEntityToWorld(-1000, fakePlayer)
    }

    /**
     * Removes the fake player when the module is disabled.
     */
    override fun onDisable() {
        fakePlayer?.let {
            // Remove the fake player from the world using its entity ID
            mc.theWorld.removeEntityFromWorld(it.entityId)
        }
        // Clear the reference to the fake player
        fakePlayer = null
    }
}