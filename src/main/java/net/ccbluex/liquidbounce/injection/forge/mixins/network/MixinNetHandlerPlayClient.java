/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URISyntaxException;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    @Final
    private NetworkManager netManager;

    @Shadow
    private Minecraft gameController;

    @Shadow
    private WorldClient clientWorldController;

    @Shadow
    public int currentServerMaxPlayers;

    @Shadow
    public abstract void handleEntityVelocity(S12PacketEntityVelocity packetIn);

    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private void handleResourcePack(final S48PacketResourcePackSend p_handleResourcePack_1_, final CallbackInfo callbackInfo) {
        final String url = p_handleResourcePack_1_.getURL();
        final String hash = p_handleResourcePack_1_.getHash();

        try {
            final String scheme = new URI(url).getScheme();
            final boolean isLevelProtocol = "level".equals(scheme);

            if(!"http".equals(scheme) && !"https".equals(scheme) && !isLevelProtocol)
                throw new URISyntaxException(url, "Wrong protocol");

            if(isLevelProtocol && (url.contains("..") || !url.endsWith("/resources.zip")))
                throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
        } catch(final URISyntaxException e) {
            ClientUtils.INSTANCE.logError("Failed to handle resource pack", e);
            netManager.sendPacket(new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
            callbackInfo.cancel();
        }
    }

    @Inject(method = "handleJoinGame", at = @At("HEAD"), cancellable = true)
    private void handleJoinGameWithAntiForge(S01PacketJoinGame packetIn, final CallbackInfo callbackInfo) {
        if(!AntiForge.INSTANCE.getEnabled() || !AntiForge.INSTANCE.getBlockFML() || Minecraft.getMinecraft().isIntegratedServerRunning())
            return;

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, gameController);
        this.gameController.playerController = new PlayerControllerMP(gameController, (NetHandlerPlayClient) (Object) this);
        this.clientWorldController = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.gameController.mcProfiler);
        this.gameController.gameSettings.difficulty = packetIn.getDifficulty();
        this.gameController.loadWorld(this.clientWorldController);
        this.gameController.thePlayer.dimension = packetIn.getDimension();
        this.gameController.displayGuiScreen(new GuiDownloadTerrain((NetHandlerPlayClient) (Object) this));
        this.gameController.thePlayer.setEntityId(packetIn.getEntityId());
        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
        this.gameController.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
        this.gameController.playerController.setGameType(packetIn.getGameType());
        this.gameController.gameSettings.sendSettingsToServer();
        this.netManager.sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
        callbackInfo.cancel();
    }

    /**
     * @author liulihaocai
     */
    @Overwrite
    public void handleExplosion(S27PacketExplosion packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.gameController);
        Explosion explosion = new Explosion(this.gameController.theWorld, null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
        explosion.doExplosionB(true);
        // convert it to velocity packet
        S12PacketEntityVelocity packet = new S12PacketEntityVelocity(this.gameController.thePlayer.getEntityId(),
                this.gameController.thePlayer.motionX + packetIn.func_149149_c(),
                this.gameController.thePlayer.motionY + packetIn.func_149144_d(),
                this.gameController.thePlayer.motionZ + packetIn.func_149147_e());
        PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.RECEIVE);
        LiquidBounce.eventManager.callEvent(packetEvent);
        if(!packetEvent.isCancelled()){
            handleEntityVelocity(packet);
        }
    }
}
