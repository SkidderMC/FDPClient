/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.protocol;

import net.ccbluex.liquidbounce.handler.protocol.api.VFPlatform;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "FDPClient", version = "Release")
public class ProtocolMod implements VFPlatform {

    public static final ProtocolMod PLATFORM = new ProtocolMod();

    @Override
    public int getGameVersion() {
        return RealmsSharedConstants.NETWORK_PROTOCOL_VERSION;
    }
}