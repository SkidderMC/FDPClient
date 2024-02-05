/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.ui.client.gui.GuiTeleportation;
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.GuiTheme;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.ServerUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public abstract class MixinGuiIngameMenu extends MixinGuiScreen {

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        if(!this.mc.isIntegratedServerRunning()) {
            this.buttonList.add(new GuiButton(1337, this.width / 2 - 100, this.height / 4 + 128, "Reconnect"));
            this.buttonList.add(new GuiButton(1068,this.width / 2 - 100,this.height / 4 + 128 + 24,"Switcher"));
            this.buttonList.add(new GuiButton(1078,this.width / 2 - 100,this.height / 4 + 128 + 140,"Key Bind Manager"));
            this.buttonList.add(new GuiButton(16578,this.width / 2 - 100,this.height / 4 + 128 + 115,"Client Color"));
            this.buttonList.add(new GuiButton(1000, 4, height - 24, 68, 20, "Misc"));
        } else {
            this.buttonList.add(new GuiButton(1068,this.width / 2 - 100,this.height / 4 + 128,"Switcher"));
            this.buttonList.add(new GuiButton(1078,this.width / 2 - 100,this.height / 4 + 128 + 105,"Key Bind Manager"));
            this.buttonList.add(new GuiButton(16578,this.width / 2 - 100,this.height / 4 + 128 + 80,"Client Color"));
        }
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_,CallbackInfo callbackInfo) {

    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreen(CallbackInfo callbackInfo) {
        Fonts.minecraftFont.drawStringWithShadow(
                "§" + HUD.INSTANCE.getColorGuiInGameValue().getValue() + "Username : §a" + mc.getSession().getUsername(),
                6f,
                6f,
                0xffffff);
        if (!mc.isIntegratedServerRunning()) {
            Fonts.minecraftFont.drawStringWithShadow(
                    "§" + HUD.INSTANCE.getColorGuiInGameValue().getValue() + "Server : §a" + mc.getCurrentServerData().serverIP,
                    6f,
                    16f,
                    0xffffff);
            Fonts.minecraftFont.drawStringWithShadow(
                    "§" + HUD.INSTANCE.getColorGuiInGameValue().getValue() + "Brand : §a" + mc.getCurrentServerData().gameVersion,
                    6f,
                    26f,
                    0xffffff);
            Fonts.minecraftFont.drawStringWithShadow(
                    "§" + HUD.INSTANCE.getColorGuiInGameValue().getValue() + "Protocol : §a" + mc.getCurrentServerData().version,
                    6f,
                    36f,
                    0xffffff);
            Fonts.minecraftFont.drawStringWithShadow(
                    "§" + HUD.INSTANCE.getColorGuiInGameValue().getValue() + "Ping : §a" + mc.getCurrentServerData().pingToServer,
                    6f,
                    46f,
                    0xffffff);
            Fonts.minecraftFont.drawStringWithShadow(
                    "§" + HUD.INSTANCE.getColorGuiInGameValue().getValue() +"Players : §a" + mc.getCurrentServerData().populationInfo,
                    6f,
                    56f,
                    0xffffff);
            Fonts.minecraftFont.drawStringWithShadow(
                    "§" + HUD.INSTANCE.getColorGuiInGameValue().getValue() + "Health : §a" + mc.thePlayer.getHealth(),
                    6f,
                    66f,
                    0xffffff);
        }
    }
    
    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        if(button.id == 1337) {
            mc.theWorld.sendQuittingDisconnectingPacket();
            ServerUtils.connectToLastServer();
        }
        if(button.id == 16578) {
            mc.displayGuiScreen(new GuiTheme());
        }
        if (button.id == 1068) {
            mc.displayGuiScreen(new GuiMultiplayer((GuiScreen) (Object) this));
        }

        if (button.id == 1078) {
            mc.displayGuiScreen(FDPClient.keyBindManager);
        }

        if (button.id == 1000) {
            mc.displayGuiScreen(new GuiTeleportation());
        }
    }
}
