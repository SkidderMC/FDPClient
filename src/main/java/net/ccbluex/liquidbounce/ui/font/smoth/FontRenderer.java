/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.smoth;

public interface FontRenderer {
	float drawString(CharSequence text, float x, float y, int color, boolean dropShadow);

    String trimStringToWidth(CharSequence text, int width, boolean reverse);
	int stringWidth(CharSequence text);

    int getHeight();

    //region default methods
	default float drawString(CharSequence text, float x, float y, int color) {
		return drawString(text, x, y, color, false);
	}
	default void drawString(CharSequence text, int x, int y, int color) {
        drawString(text, x, y, color, false);
    }
	default float drawCenteredString(CharSequence text, float x, float y, int color, boolean dropShadow) {
		return drawString(text, x - stringWidth(text) / 2.0F, y, color, dropShadow);
	}
	default void drawCenteredString(CharSequence text, float x, float y, int color) {
        drawCenteredString(text, x, y, color, false);
    }
}
