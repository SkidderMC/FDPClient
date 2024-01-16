/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.smoth;

@SuppressWarnings("SpellCheckingInspection")
public interface Fonts {

	FontManager FONT_MANAGER = info.getFontManager();

	interface MAINMENU {
		FontFamily MAINMENU = FONT_MANAGER.fontFamily(FontType.MAINMENU);
		final class MAINMENU30 {
			public static final FontRenderer MAINMENU30 = MAINMENU.ofSize(30);

			private MAINMENU30() {
			}
		}

	}
}
