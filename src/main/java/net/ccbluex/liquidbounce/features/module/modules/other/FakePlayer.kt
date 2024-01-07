/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.entity.EntityOtherPlayerMP


@ModuleInfo(name = "FakePlayer", category = ModuleCategory.OTHER)
object FakePlayer : Module() {

    private val amountValue = IntegerValue("Summon", 1, 0, 20, "x")

    private var fakePlayer: EntityOtherPlayerMP? = null

    override fun onEnable() {
        for (i in 0 until amountValue.get()) {
            val fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
            fakePlayer.clonePlayer(mc.thePlayer, true)
            fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead
            fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer)
            mc.theWorld.addEntityToWorld(-1000 - i, fakePlayer)
        }
    }

    override fun onDisable() {
        for (i in 0 until amountValue.get()) {
            mc.theWorld.removeEntityFromWorld(-1000 - i)
        }
        mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
        fakePlayer = null
    }
}