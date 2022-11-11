package net.ccbluex.liquidbounce.ui.client.gui.clickgui.files.normal;

public interface Screen extends Utils {

    void initGui();

    void keyTyped(char typedChar, int keyCode);

    void drawScreen(int mouseX, int mouseY);

    void mouseClicked(int mouseX, int mouseY, int button);

    void mouseReleased(int mouseX, int mouseY, int state);

}
