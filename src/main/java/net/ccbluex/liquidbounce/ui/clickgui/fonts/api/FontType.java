/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.clickgui.fonts.api;
@SuppressWarnings("SpellCheckingInspection")
public enum FontType {
	ICONFONT("stylesicons.ttf"),

	Check("check.ttf"),
	SF("SF.ttf"),
	MAINMENU("mainmenu.ttf");

	private final String fileName;

	FontType(String fileName) {
		this.fileName = fileName;
	}

	public String fileName() { return fileName; }
}
