package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdatePlayerEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.utils.PacketUtils
import com.google.common.eventbus.Subscribe
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

@ModuleInfo(name = "YesFall", category = ModuleCategory.PLAYER)
class YesFall : Module() {

    @Subscribe
    fun onMove(e: UpdatePlayerEvent?) {
        if (Math.round(mc.thePlayer.fallDistance) % 3 === 0) {
            // rip i couldn't make it :((( :cry:
            //i am gaming :sunglasses:
            PacketUtils.sendPacketNoEvent(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY + mc.thePlayer.fallDistance,
                    mc.thePlayer.posZ,
                    false
                )
            )
        }
    } /*@Override
    public void onEnable() {
        choices.getAllChoices().forEach(ClientUtils::fancyMessage);
        ClientUtils.fancyMessage("all choices ^");
        ClientUtils.fancyMessage(" ");
        choices.getSelectedChoices().forEach(ClientUtils::fancyMessage);
        ClientUtils.fancyMessage("selected choices ^");
        ClientUtils.fancyMessage(" ");
        choices.toggleChoice(choices.getAllChoices().get(0));
        choices.getSelectedChoices().forEach(ClientUtils::fancyMessage);
        ClientUtils.fancyMessage("selected choices ^");
    }*/
}