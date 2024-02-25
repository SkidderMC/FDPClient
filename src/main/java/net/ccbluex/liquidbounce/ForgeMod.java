package net.ccbluex.liquidbounce;

import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;


@Mod(modid = ForgeMod.modId, name = FDPClient.CLIENT_NAME, version = "unknown", clientSideOnly = true)
public class ForgeMod {
    public static final String modId = "fdpclient";

    @Mod.Instance(ForgeMod.modId)
    public static ForgeMod instance;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        ClientUtils.getLogger().info("FML Init");
    }
}
