/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.api;

@SuppressWarnings("SpellCheckingInspection")
public enum FontType {

	SF("sf.ttf"),
	SFBOLD("sfbold.ttf"),
	SFTHIN("SFREGULAR.ttf"),
	Check("check.ttf"),
	ICONFONT("stylesicons.ttf");

	private final String fileName;

	FontType(String fileName) {
		this.fileName = fileName;
	}

	public String fileName() { return fileName; }
}
