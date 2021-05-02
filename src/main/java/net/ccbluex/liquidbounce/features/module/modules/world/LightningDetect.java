/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;

@ModuleInfo(name = "LightningDetect", description = "check lighining spawn a where", category = ModuleCategory.WORLD)
public class LightningDetect extends Module {
    @EventTarget
    public void onPacket(PacketEvent event){
        if(event.getPacket() instanceof S2CPacketSpawnGlobalEntity && ((S2CPacketSpawnGlobalEntity) event.getPacket()).func_149053_g() == 1){
            S2CPacketSpawnGlobalEntity entity = ((S2CPacketSpawnGlobalEntity) event.getPacket());
            ClientUtils.displayChatMessage("Detect Lightning in a X:" + entity.func_149051_d() + " Y:" + entity.func_149050_e() + " Z:" + entity.func_149049_f());
        }
    }
}
