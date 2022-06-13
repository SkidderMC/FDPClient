/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api;

public interface FontRenderer {
	float drawString(CharSequence text, float x, float y, int color, boolean dropShadow);
	float drawString(CharSequence text, double x, double y, int color, boolean dropShadow);
	String trimStringToWidth(CharSequence text, int width, boolean reverse);
	int stringWidth(CharSequence text);
	float charWidth(char ch);

	String getName();
	int getHeight();
	boolean isAntiAlias();
	boolean isFractionalMetrics();

	//region default methods
	default float drawString(CharSequence text, float x, float y, int color) {
		return drawString(text, x, y, color, false);
	}
	default float drawString(CharSequence text, int x, int y, int color) {
		return drawString(text, x, y, color, false);
	}
	default String trimStringToWidth(CharSequence text, int width) {
		return trimStringToWidth(text, width, false);
	}

	default float drawCenteredString(CharSequence text, float x, float y, int color, boolean dropShadow) {
		return drawString(text, x - stringWidth(text) / 2.0F, y, color, dropShadow);
	}
	 default float getMiddleOfBox(float boxHeight) {
		return boxHeight / 2f - getHeight() / 2f;
	}

	default float drawCenteredString(CharSequence text, float x, float y, int color) {
		return drawCenteredString(text, x, y, color, false);
	}
	//endregion
}
