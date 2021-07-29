/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiAdd;
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiDirectLogin;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager;
import net.ccbluex.liquidbounce.utils.login.LoginUtils;
import net.ccbluex.liquidbounce.utils.login.MinecraftAccount;
import net.ccbluex.liquidbounce.utils.login.UserUtils;
import net.ccbluex.liquidbounce.utils.misc.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class GuiAltManager extends GuiScreen {

    private final GuiScreen prevGui;
    public String status = "§7%ui.alt.idle%";
    private GuiButton loginButton;
    private GuiButton randomAltButton;
    private GuiList altsList;

    public GuiAltManager(final GuiScreen prevGui) {
        this.prevGui = prevGui;
    }

    public static String login(final MinecraftAccount minecraftAccount) {
        if (minecraftAccount == null)
            return "";

        if (minecraftAccount.isCracked()) {
            LoginUtils.loginCracked(minecraftAccount.getName());
            return LanguageManager.INSTANCE.getAndFormat("ui.alt.nameChanged",minecraftAccount.getName());
        }

        LoginUtils.LoginResult result = LoginUtils.login(minecraftAccount.getName(), minecraftAccount.getPassword());
        if (result == LoginUtils.LoginResult.LOGGED) {
            String userName = Minecraft.getMinecraft().getSession().getUsername();
            minecraftAccount.setAccountName(userName);
            LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig);
            return LanguageManager.INSTANCE.getAndFormat("ui.alt.nameChanged",userName);
        }

        if (result == LoginUtils.LoginResult.WRONG_PASSWORD)
            return "§c%ui.alt.wrongPassword%";

        if (result == LoginUtils.LoginResult.NO_CONTACT)
            return "§c%ui.alt.noContact%";

        if (result == LoginUtils.LoginResult.INVALID_ACCOUNT_DATA)
            return "§c%ui.alt.invalidData%";

        if (result == LoginUtils.LoginResult.MIGRATED)
            return "§c%ui.alt.migrated%";

        return "";
    }

    public void initGui() {
        altsList = new GuiList(this);
        altsList.registerScrollButtons(7, 8);

        int index = -1;

        for (int i = 0; i < LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.size(); i++) {
            MinecraftAccount minecraftAccount = LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.get(i);

            if (minecraftAccount != null && (
                    ((
                            // When password is empty, the account is cracked
                            minecraftAccount.getPassword() == null || minecraftAccount.getPassword().isEmpty()) && minecraftAccount.getName() != null && minecraftAccount.getName().equals(mc.session.getUsername()))
                            // When the account is a premium account match the IGN
                            || minecraftAccount.getAccountName() != null && minecraftAccount.getAccountName().equals(mc.session.getUsername())
            )) {
                index = i;
                break;
            }
        }

        altsList.elementClicked(index, false, 0, 0);
        altsList.scrollBy(index * altsList.slotHeight);

        int j = 22;
        this.buttonList.add(new GuiButton(1, width - 80, j + 24, 70, 20, "%ui.alt.add%"));
        this.buttonList.add(new GuiButton(2, width - 80, j + 24 * 2, 70, 20, "%ui.alt.remove%"));
        this.buttonList.add(new GuiButton(7, width - 80, j + 24 * 3, 70, 20, "%ui.alt.import%"));
        this.buttonList.add(new GuiButton(8, width - 80, j + 24 * 4, 70, 20, "%ui.alt.copy%"));

        this.buttonList.add(new GuiButton(0, width - 80, height - 65, 70, 20, "%ui.back%"));

        this.buttonList.add(loginButton = new GuiButton(3, 5, j + 24, 90, 20, "%ui.alt.login%"));
        this.buttonList.add(randomAltButton = new GuiButton(4, 5, j + 24 * 2, 90, 20, "%ui.disconnect.randomAlt%"));
        this.buttonList.add(new GuiButton(89, 5, j + 24 * 3, 90, 20, "%ui.disconnect.randomOffline%"));
        this.buttonList.add(new GuiButton(6, 5, j + 24 * 4, 90, 20, "%ui.alt.directLogin%"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        altsList.drawScreen(mouseX, mouseY, partialTicks);

        Fonts.font40.drawCenteredString("%ui.altmanager%", width / 2, 6, 0xffffff);
        Fonts.font35.drawCenteredString( LanguageManager.INSTANCE.getAndFormat("ui.alt.alts",LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.size()), width / 2, 18, 0xffffff);
        Fonts.font35.drawCenteredString(status, width / 2, 32, 0xffffff);
        Fonts.font35.drawStringWithShadow(LanguageManager.INSTANCE.getAndFormat("ui.alt.username",mc.getSession().getUsername()), 6, 6, 0xffffff);
        Fonts.font35.drawStringWithShadow(LanguageManager.INSTANCE.getAndFormat("ui.alt.type",(UserUtils.INSTANCE.isValidTokenOffline(mc.getSession().getToken()) ? "%ui.alt.premium%" : "%ui.alt.cracked%")), 6, 15, 0xffffff);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;

        switch (button.id) {
            case 0:
                mc.displayGuiScreen(prevGui);
                break;
            case 1:
                mc.displayGuiScreen(new GuiAdd(this));
                break;
            case 2:
                if (altsList.getSelectedSlot() != -1 && altsList.getSelectedSlot() < altsList.getSize()) {
                    LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.remove(altsList.getSelectedSlot());
                    LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig);
                    status = "§a%ui.alt.removed%";
                } else
                    status = "§c%ui.alt.needSelect%";
                break;
            case 3:
                if (altsList.getSelectedSlot() != -1 && altsList.getSelectedSlot() < altsList.getSize()) {
                    loginButton.enabled = randomAltButton.enabled = false;

                    final Thread thread = new Thread(() -> {
                        final MinecraftAccount minecraftAccount = LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.get(altsList.getSelectedSlot());
                        status = "§aLogging in...";
                        status = login(minecraftAccount);

                        loginButton.enabled = randomAltButton.enabled = true;
                    }, "AltLogin");
                    thread.start();
                } else
                    status = "§c%ui.alt.needSelect%";
                break;
            case 4:
                if (LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.size() <= 0) {
                    status = "§c%ui.alt.emptyList%";
                    return;
                }

                final int randomInteger = new Random().nextInt(LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.size());

                if (randomInteger < altsList.getSize())
                    altsList.selectedSlot = randomInteger;

                loginButton.enabled = randomAltButton.enabled = false;

                final Thread thread = new Thread(() -> {
                    final MinecraftAccount minecraftAccount = LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.get(randomInteger);
                    status = "§a%ui.alt.loggingIn%";
                    status = login(minecraftAccount);

                    loginButton.enabled = randomAltButton.enabled = true;
                }, "AltLogin");
                thread.start();
                break;
            case 6:
                mc.displayGuiScreen(new GuiDirectLogin(this));
                break;
            case 7:
                final File file = MiscUtils.openFileChooser();

                if (file == null)
                    return;

                final FileReader fileReader = new FileReader(file);
                final BufferedReader bufferedReader = new BufferedReader(fileReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    final String[] accountData = line.split(":", 2);

                    boolean alreadyAdded = false;

                    for (final MinecraftAccount registeredMinecraftAccount : LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts) {
                        if (registeredMinecraftAccount.getName().equalsIgnoreCase(accountData[0])) {
                            alreadyAdded = true;
                            break;
                        }
                    }

                    if (!alreadyAdded) {
                        if (accountData.length > 1)
                            LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.add(new MinecraftAccount(accountData[0], accountData[1]));
                        else
                            LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.add(new MinecraftAccount(accountData[0]));
                    }
                }

                fileReader.close();
                bufferedReader.close();
                LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig);
                status = "§a%ui.alt.imported%";
                break;
            case 8:
                if (altsList.getSelectedSlot() != -1 && altsList.getSelectedSlot() < altsList.getSize()) {
                    final MinecraftAccount minecraftAccount = LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.get(altsList.getSelectedSlot());

                    if (minecraftAccount == null)
                        break;

                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(minecraftAccount.getName() + ":" + minecraftAccount.getPassword()), null);
                    status = "§a%ui.alt.copied%";
                } else
                    status = "§c%ui.alt.needSelect%";
                break;
            case 89:
                new Thread(LoginUtils::randomCracked).start();
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        switch (keyCode) {
            case Keyboard.KEY_ESCAPE:
                mc.displayGuiScreen(prevGui);
                return;
            case Keyboard.KEY_UP: {
                int i = altsList.getSelectedSlot() - 1;
                if (i < 0)
                    i = 0;
                altsList.elementClicked(i, false, 0, 0);
                break;
            }
            case Keyboard.KEY_DOWN: {
                int i = altsList.getSelectedSlot() + 1;
                if (i >= altsList.getSize())
                    i = altsList.getSize() - 1;
                altsList.elementClicked(i, false, 0, 0);
                break;
            }
            case Keyboard.KEY_RETURN: {
                altsList.elementClicked(altsList.getSelectedSlot(), true, 0, 0);
                break;
            }
            case Keyboard.KEY_NEXT: {
                altsList.scrollBy(height - 100);
                break;
            }
            case Keyboard.KEY_PRIOR: {
                altsList.scrollBy(-height + 100);
                return;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        altsList.handleMouseInput();
    }

    private class GuiList extends GuiSlot {

        private int selectedSlot;

        GuiList(GuiScreen prevGui) {
            super(GuiAltManager.this.mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30);
        }

        @Override
        protected boolean isSelected(int id) {
            return selectedSlot == id;
        }

        int getSelectedSlot() {
            if (selectedSlot > LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.size())
                selectedSlot = -1;
            return selectedSlot;
        }

        public void setSelectedSlot(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        @Override
        protected int getSize() {
            return LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.size();
        }

        @Override
        protected void elementClicked(int var1, boolean doubleClick, int var3, int var4) {
            selectedSlot = var1;

            if (doubleClick) {
                if (altsList.getSelectedSlot() != -1 && altsList.getSelectedSlot() < altsList.getSize() && loginButton.enabled) {
                    loginButton.enabled = randomAltButton.enabled = false;

                    new Thread(() -> {
                        MinecraftAccount minecraftAccount = LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.get(altsList.getSelectedSlot());
                        status = "§a%ui.alt.loggingIn%";
                        status = "§c" + login(minecraftAccount);

                        loginButton.enabled = randomAltButton.enabled = true;
                    }, "AltManagerLogin").start();
                } else
                    status = "§c%ui.alt.needSelect%";
            }
        }

        @Override
        protected void drawSlot(int id, int x, int y, int var4, int var5, int var6) {
            final MinecraftAccount minecraftAccount = LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.get(id);
            Fonts.font40.drawCenteredString(minecraftAccount.getAccountName() == null ? minecraftAccount.getName() : minecraftAccount.getAccountName(), (width / 2), y + 2, Color.WHITE.getRGB(), true);
            Fonts.font40.drawCenteredString(minecraftAccount.isCracked() ? "%ui.alt.type.cracked%" : (minecraftAccount.getAccountName() == null ? "%ui.alt.type.premium%" : minecraftAccount.getName()), (width / 2), y + 15, minecraftAccount.isCracked() ? Color.GRAY.getRGB() : (minecraftAccount.getAccountName() == null ? Color.GREEN.getRGB() : Color.LIGHT_GRAY.getRGB()), true);
        }

        @Override
        protected void drawBackground() {
        }
    }

}
