package net.ccbluex.liquidbounce.utils.misc;

public class HoveringUtil {

    public static boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
    
}
