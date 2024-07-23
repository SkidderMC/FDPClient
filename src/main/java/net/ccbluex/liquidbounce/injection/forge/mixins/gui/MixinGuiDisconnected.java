/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import com.google.gson.JsonObject;

import me.liuli.elixir.account.MinecraftAccount;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.SessionEvent;
import net.ccbluex.liquidbounce.handler.other.AutoReconnect;
import net.ccbluex.liquidbounce.handler.payload.ClientFixes;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.GuiLoginProgress;
import net.ccbluex.liquidbounce.ui.client.gui.GuiInfo;
import net.ccbluex.liquidbounce.utils.APIConnecter;
import net.ccbluex.liquidbounce.utils.ServerUtils;
import net.ccbluex.liquidbounce.utils.misc.MiscUtils;
import net.ccbluex.liquidbounce.utils.misc.RandomUtils;
import net.minecraft.client.gui.*;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

@Mixin(GuiDisconnected.class)
public abstract class MixinGuiDisconnected extends MixinGuiScreen {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0");

    @Shadow
    private int field_175353_i;

    private GuiButton reconnectButton;
    private GuiSlider autoReconnectDelaySlider;
    private GuiButton forgeBypassButton;
    private int reconnectTimer;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        reconnectTimer = 0;
        buttonList.add(reconnectButton = new GuiButton(1, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 22, 98, 20, "Reconnect"));
        drawReconnectDelaySlider();

        buttonList.add(new GuiButton(4, width / 2 + 2, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 44, 98, 20, "Random Account"));
        buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 2 + field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 66, 200, 20, "Donate Now"));
        buttonList.add(forgeBypassButton = new GuiButton(6, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 86, "Bypass AntiForge: " + (ClientFixes.INSTANCE.getFmlFixesEnabled() ? "On" : "Off")));
        buttonList.add(new GuiButton(998, width - 94, 5, 88, 20, "Alt Manager"));
        buttonList.add(new GuiButton(8, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 44, 98, 20, "Settings"));

        updateSliderText();
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) throws IOException {
        switch (button.id) {
            case 1:
                ServerUtils.INSTANCE.connectToLastServer();
                break;
            case 3:
                final List<MinecraftAccount> accounts = FileManager.INSTANCE.getAccountsConfig().getAccounts();
                if (accounts.isEmpty())
                    break;
                final MinecraftAccount minecraftAccount = accounts.get(new Random().nextInt(accounts.size()));

                mc.displayGuiScreen(new GuiLoginProgress(minecraftAccount, () -> {
                    mc.addScheduledTask(() -> {
                        EventManager.INSTANCE.callEvent(new SessionEvent());
                        ServerUtils.INSTANCE.connectToLastServer();
                    });
                    return null;
                }, e -> {
                    mc.addScheduledTask(() -> {
                        final JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("text", e.getMessage());

                        mc.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), e.getMessage(), IChatComponent.Serializer.jsonToComponent(jsonObject.toString())));
                    });
                    return null;
                }, () -> null));

                break;
            case 4:
                RandomUtils.INSTANCE.randomAccount();
                ServerUtils.INSTANCE.connectToLastServer();
                break;
            case 5:
                MiscUtils.INSTANCE.showURL(APIConnecter.INSTANCE.getDonate());
                break;
            case 6:
                ClientFixes.INSTANCE.setFmlFixesEnabled(!ClientFixes.INSTANCE.getFmlFixesEnabled());
                forgeBypassButton.displayString = "Bypass AntiForge: " + (ClientFixes.INSTANCE.getFmlFixesEnabled() ? "On" : "Off");
                FileManager.INSTANCE.getValuesConfig().saveConfig();
                break;
            case 998:
                mc.displayGuiScreen(new GuiAltManager((GuiScreen) (Object) this));
                break;
            case 8:
                mc.displayGuiScreen(new GuiInfo((GuiScreen) (Object) this));
        }
    }

    @Override
    public void updateScreen() {
        if (AutoReconnect.INSTANCE.isEnabled()) {
            reconnectTimer++;
            if (reconnectTimer > AutoReconnect.INSTANCE.getDelay() / 50)
                ServerUtils.INSTANCE.connectToLastServer();
        }
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreen(CallbackInfo callbackInfo) {
        if (AutoReconnect.INSTANCE.isEnabled()) {
            updateReconnectButton();
        }
    }

    private void drawReconnectDelaySlider() {
        buttonList.add(autoReconnectDelaySlider =
                new GuiSlider(2, width / 2 + 2, height / 2 + field_175353_i / 2
                        + fontRendererObj.FONT_HEIGHT + 22, 98, 20, "AutoReconnect: ",
                        "ms", AutoReconnect.MIN, AutoReconnect.MAX, AutoReconnect.INSTANCE.getDelay(), false, true,
                        guiSlider -> {
                            AutoReconnect.INSTANCE.setDelay(guiSlider.getValueInt());

                            reconnectTimer = 0;
                            updateReconnectButton();
                            updateSliderText();
                        }));
    }

    private void updateSliderText() {
        if (autoReconnectDelaySlider == null)
            return;

        if (!AutoReconnect.INSTANCE.isEnabled()) {
            autoReconnectDelaySlider.displayString = "AutoReconnect: Off";
        } else {
            autoReconnectDelaySlider.displayString = "AutoReconnect: " + DECIMAL_FORMAT.format(AutoReconnect.INSTANCE.getDelay() / 1000.0) + "s";
        }
    }

    private void updateReconnectButton() {
        if (reconnectButton != null)
            reconnectButton.displayString = "Reconnect" + (AutoReconnect.INSTANCE.isEnabled() ? " (" + (AutoReconnect.INSTANCE.getDelay() / 1000 - reconnectTimer / 20) + ")" : "");
    }
}