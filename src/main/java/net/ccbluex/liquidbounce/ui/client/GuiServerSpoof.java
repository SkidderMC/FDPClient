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
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120, "Save"));
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 144, "Back"));

        textField = new GuiTextField(2, Fonts.font40, width / 2 - 100, 60, 200, 20);
        textField.setText(ServerSpoof.ip);

        updateButtonStat();
    }

    private void updateButtonStat(){
        stat.displayString="Status: "+(ServerSpoof.enable?"§aOn":"§cOff");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        System.out.println("W="+width+", H="+height);
        drawBackground(0);
        Gui.drawRect(30, 30, width - 30, height - 30, Integer.MIN_VALUE);

        for(GuiButton button:buttonList) {
            button.drawButton(mc,mouseX,mouseY);
        }

        drawCenteredString(Fonts.font40, "Server Spoof", width / 2, 34, 0xffffff);
        textField.drawTextBox();

        if(textField.getText().isEmpty() && !textField.isFocused())
            drawString(Fonts.font40, "§7Server Address", width / 2 - 100, 66, 0xffffff);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: {
                mc.displayGuiScreen(prevGui);
                break;
            }
            case 1: {
                ServerSpoof.ip=textField.getText();
                mc.displayGuiScreen(prevGui);
                break;
            }
            case 2:{
                ServerSpoof.enable=!ServerSpoof.enable;
                break;
            }
        }

        updateButtonStat();
        if(button.id!=0) {
            LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.valuesConfig);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }
}
