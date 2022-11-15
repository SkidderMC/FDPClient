/*
 * ZAVZ Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.patcher.bugfixes;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ServerList.class)
public abstract class MixinServerList {
    private static final Logger patcher$logger = LogManager.getLogger("Patcher/ServerList");

    @Shadow @Final
    private List<ServerData> servers;

    @Shadow public abstract void saveServerList();

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public ServerData getServerData(int index) {
        try {
            return this.servers.get(index);
        } catch (Exception e) {
            patcher$logger.error("Failed to get server data.", e);
            return null;
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void removeServerData(int index) {
        try {
            this.servers.remove(index);
        } catch (Exception e) {
            patcher$logger.error("Failed to remove server data.", e);
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void addServerData(ServerData server) {
        try {
            this.servers.add(server);
        } catch (Exception e) {
            patcher$logger.error("Failed to add server data.", e);
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void swapServers(int p_78857_1_, int p_78857_2_) {
        try {
            ServerData serverdata = this.getServerData(p_78857_1_);
            this.servers.set(p_78857_1_, this.getServerData(p_78857_2_));
            this.servers.set(p_78857_2_, serverdata);
            this.saveServerList();
        } catch (Exception e) {
            patcher$logger.error("Failed to swap servers.", e);
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void func_147413_a(int index, ServerData server) {
        try {
            this.servers.set(index, server);
        } catch (Exception e) {
            patcher$logger.error("Failed to set server data.", e);
        }
    }
}
