/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api;
@SuppressWarnings("SpellCheckingInspection")
public enum FontType {
	DM("diramight.ttf"),
	FIXEDSYS("tahoma.ttf"),
	ICONFONT("stylesicons.ttf"),
	FluxICONFONT("flux.ttf"),
	Check("check.ttf"),
	TenacityBold("Tenacity.ttf"),
	SF("SF.ttf"),
	SFBOLD("SFBOLD.ttf"),
	CHINESE("black.ttf"),
	Tahoma("Tahoma.ttf"),
	TahomaBold("Tahoma-Bold.ttf"),
	SFTHIN("SFREGULAR.ttf"),
	MAINMENU("mainmenu.ttf"),
	OXIDE("oxide.ttf");


	private final String fileName;

	FontType(String fileName) {
		this.fileName = fileName;
	}

	public String fileName() { return fileName; }
}
