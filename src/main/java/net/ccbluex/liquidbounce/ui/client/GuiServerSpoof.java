package net.ccbluex.liquidbounce.ui.client;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.special.ServerSpoof;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiServerSpoof extends GuiScreen {

    private final GuiScreen prevGui;

    private GuiTextField textField;
    private GuiButton stat;

    public GuiServerSpoof(final GuiScreen prevGui) {
        this.prevGui = prevGui;
    }

    @Override
    public void initGui(){
        Keyboard.enableRepeatEvents(true);
        stat=new GuiButton(2, width / 2 - 100, height / 4 + 96, "STATUS");
        buttonList.add(stat);
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 120, "Back"));

        textField = new GuiTextField(2, Fonts.font40, width / 2 - 100, 60, 200, 20);
        textField.setFocused(true);
        textField.setText(ServerSpoof.address);
        textField.setMaxStringLength(114514);

        updateButtonStat();
    }

    private void updateButtonStat(){
        stat.displayString="Status: "+(ServerSpoof.enable?"§aOn":"§cOff");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        Gui.drawRect(30, 30, width - 30, height - 30, Integer.MIN_VALUE);

        drawCenteredString(Fonts.font40, "Server Spoof", width / 2, 34, 0xffffff);

        textField.drawTextBox();
        if(textField.getText().isEmpty() && !textField.isFocused())
            drawString(Fonts.font40, "§7Server Address", width / 2 - 100, 66, 0xffffff);

        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: {
                ServerSpoof.address=textField.getText();
                mc.displayGuiScreen(prevGui);
                break;
            }
            case 2:{
                ServerSpoof.enable=!ServerSpoof.enable;
                break;
            }
        }

        updateButtonStat();
        LiquidBounce.configManager.save(true);
    }

    @Override
    public void onGuiClosed() {
        ServerSpoof.address=textField.getText();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui);
            return;
        }

        if(textField.isFocused())
            textField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        textField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        textField.updateCursorCounter();
        super.updateScreen();
    }
}
