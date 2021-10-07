/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager.sub;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.ui.elements.GuiPasswordField;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.login.MinecraftAccount;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.Proxy;

public class GuiAdd extends GuiScreen {

    private final GuiAltManager prevGui;

    private GuiButton addButton;
    private GuiButton clipboardButton;
    private GuiTextField username;
    private GuiPasswordField password;

    private String status = "§7Idle...";

    public GuiAdd(final GuiAltManager gui) {
        this.prevGui = gui;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.add(addButton = new GuiButton(1, width / 2 - 100, height / 4 + 72, "%ui.alt.add%"));
        buttonList.add(clipboardButton = new GuiButton(2, width / 2 - 100, height / 4 + 96, "%ui.alt.clipBoardLogin%"));
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 120, "%ui.back%"));
        username = new GuiTextField(2, Fonts.font40, width / 2 - 100, 60, 200, 20);
        username.setFocused(true);
        username.setMaxStringLength(Integer.MAX_VALUE);
        password = new GuiPasswordField(3, Fonts.font40, width / 2 - 100, 85, 200, 20);
        password.setMaxStringLength(Integer.MAX_VALUE);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        drawCenteredString(Fonts.font40, "%ui.alt.add%", width / 2, 34, 0xffffff);
        drawCenteredString(Fonts.font35, status == null ? "" : status, width / 2, height / 4 + 60, 0xffffff);

        username.drawTextBox();
        password.drawTextBox();

        if(username.getText().isEmpty() && !username.isFocused())
            drawCenteredString(Fonts.font40, "§7%ui.alt.loginUsername%", width / 2 - 55, 66, 0xffffff);

        if(password.getText().isEmpty() && !password.isFocused())
            drawCenteredString(Fonts.font40, "§7%ui.alt.loginPassword%", width / 2 - 74, 91, 0xffffff);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled)
            return;

        switch(button.id) {
            case 0:
                mc.displayGuiScreen(prevGui);
                break;
            case 1:
                if (LiquidBounce.fileManager.accountsConfig.getAltManagerMinecraftAccounts().stream().anyMatch(account -> account.getName().equals(username.getText()))) {
                    status = "§c%ui.alt.alreadyAdded%";
                    break;
                }

                addAccount(username.getText(), password.getText());
                break;
            case 2:
                try{
                    final String clipboardData = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                            .getData(DataFlavor.stringFlavor);
                    final String[] accountData = clipboardData.split(":", 2);

                    if (!clipboardData.contains(":") || accountData.length != 2) {
                        status = "§c%ui.alt.invalidClipData%";
                        return;
                    }

                    addAccount(accountData[0], accountData[1]);
                }catch(final UnsupportedFlavorException e) {
                    status = "§c%ui.alt.readFailed%";
                    ClientUtils.getLogger().error("Failed to read data from clipboard.", e);
                }
                break;
        }

        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        switch (keyCode) {
            case Keyboard.KEY_ESCAPE:
                mc.displayGuiScreen(prevGui);
                return;
            case Keyboard.KEY_RETURN:
                actionPerformed(addButton);
                return;
        }

        if(username.isFocused())
            username.textboxKeyTyped(typedChar, keyCode);

        if(password.isFocused())
            password.textboxKeyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        username.mouseClicked(mouseX, mouseY, mouseButton);
        password.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        username.updateCursorCounter();
        password.updateCursorCounter();

        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    private void addAccount(final String name, final String password) {
        if (LiquidBounce.fileManager.accountsConfig.getAltManagerMinecraftAccounts().stream()
                .anyMatch(account -> account.getName().equals(name))) {
            status = "§c%ui.alt.alreadyAdded%";
            return;
        }

        addButton.enabled = clipboardButton.enabled = false;

        final MinecraftAccount account = new MinecraftAccount(name, password);

        new Thread(() -> {
            if (!account.isCracked()) {
                status = "§a%ui.alt.checking%";

                try {
                    final YggdrasilUserAuthentication userAuthentication = (YggdrasilUserAuthentication)
                            new YggdrasilAuthenticationService(Proxy.NO_PROXY, "")
                                    .createUserAuthentication(Agent.MINECRAFT);

                    userAuthentication.setUsername(account.getName());
                    userAuthentication.setPassword(account.getPassword());

                    userAuthentication.logIn();
                    account.setAccountName(userAuthentication.getSelectedProfile().getName());
                } catch (NullPointerException | AuthenticationException e) {
                    status = "§c%ui.alt.notWorking%";
                    addButton.enabled = clipboardButton.enabled = true;
                    return;
                }
            }


            LiquidBounce.fileManager.accountsConfig.getAltManagerMinecraftAccounts().add(account);
            LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig);

            status = "§a%ui.alt.alreadyAdded%";
            prevGui.status = status;
            mc.displayGuiScreen(prevGui);
        }).start();
    }
}