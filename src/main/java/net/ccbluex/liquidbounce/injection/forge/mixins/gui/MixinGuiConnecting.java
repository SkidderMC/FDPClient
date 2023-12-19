/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.ServerUtils;
import net.ccbluex.liquidbounce.utils.extensions.RendererExtensionKt;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(GuiConnecting.class)
public abstract class MixinGuiConnecting extends GuiScreen {

    @Shadow
    @Final
    private static Logger logger;
    @Shadow
    @Final
    private static AtomicInteger CONNECTION_ID;
    @Shadow
    private NetworkManager networkManager;
    @Shadow
    private boolean cancel;
    @Shadow
    @Final
    private GuiScreen previousGuiScreen;

    @Inject(method = "connect", at = @At("HEAD"))
    private void headConnect(final String ip, final int port, CallbackInfo callbackInfo) {
        ServerUtils.serverData = new ServerData("", ip + ":" + port, false);
    }

    /**
     * @reason Fix Connect
     */
    @Overwrite
    private void connect(final String ip, final int port) {
        logger.info("Authenticating to " + ip + ", " + port);

        new Thread(() -> {
            InetAddress inetaddress = null;

            try {
                if (cancel) {
                    return;
                }

                inetaddress = InetAddress.getByName(ip);
                networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());
                networkManager.setNetHandler(new NetHandlerLoginClient(networkManager, mc, previousGuiScreen));
                networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN, true));
                networkManager.sendPacket(new C00PacketLoginStart(mc.getSession().getProfile()));
            } catch (UnknownHostException unknownhostexception) {
                if (cancel)
                    return;

                logger.error("Couldn't connect to server", unknownhostexception);
                mc.displayGuiScreen(new GuiDisconnected(previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", "Unknown host")));
            } catch (Exception exception) {
                if (cancel) {
                    return;
                }

                logger.error("Couldn't connect to server", exception);
                String s = exception.toString();

                if (inetaddress != null) {
                    String s1 = inetaddress + ":" + port;
                    s = s.replaceAll(s1, "");
                }

                mc.displayGuiScreen(new GuiDisconnected(previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", s)));
            }
        }, "Server Connector #" + CONNECTION_ID.incrementAndGet()).start();
    }

    /**
     * @author As_pw
     * @reason Logging in
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(MinecraftInstance.mc);

        this.drawDefaultBackground();

        String ip = "Unknown";

        final ServerData serverData = mc.getCurrentServerData();
        if (serverData != null)
            ip = serverData.serverIP;

        drawCenteredString(fontRendererObj, "Logging in to", scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 4 + 102, 16777215);
        drawCenteredString(fontRendererObj, "§7" + ip, scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 4 + 115, 16777215);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}