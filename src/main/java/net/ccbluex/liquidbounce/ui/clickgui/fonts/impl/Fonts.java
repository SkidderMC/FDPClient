/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.clickgui.fonts.impl;

import net.ccbluex.liquidbounce.ui.clickgui.fonts.api.FontFamily;
import net.ccbluex.liquidbounce.ui.clickgui.fonts.api.FontManager;
import net.ccbluex.liquidbounce.ui.clickgui.fonts.api.FontRenderer;
import net.ccbluex.liquidbounce.ui.clickgui.fonts.api.FontType;
import net.ccbluex.liquidbounce.ui.clickgui.fonts.logo.info;
@SuppressWarnings("SpellCheckingInspection")
public interface Fonts {

	FontManager FONT_MANAGER = info.getFontManager();

    interface ICONFONT {

		FontFamily ICONFONT = FONT_MANAGER.fontFamily(FontType.ICONFONT);

		final class ICONFONT_24 {
			public static final FontRenderer ICONFONT_24 = ICONFONT.ofSize(24);

			private ICONFONT_24() {
			}
		}

	}

	interface MAINMENU {
		FontFamily MAINMENU = FONT_MANAGER.fontFamily(FontType.MAINMENU);
		final class MAINMENU30 {
			public static final FontRenderer MAINMENU30 = MAINMENU.ofSize(30);

			private MAINMENU30() {
			}
		}

	}

    interface CheckFont {
		FontFamily CheckFont = FONT_MANAGER.fontFamily(FontType.Check);

		final class CheckFont_24 {
			public static final FontRenderer CheckFont_24 = CheckFont.ofSize(24);

			private CheckFont_24() {
			}
		}
	}

	interface SF {

		FontFamily SF = FONT_MANAGER.fontFamily(FontType.SF);

		final class SF_16 {
			public static final FontRenderer SF_16 = SF.ofSize(16);

			private SF_16() {
			}
		}

		final class SF_17 {
			public static final FontRenderer SF_17 = SF.ofSize(17);

			private SF_17() {
			}
		}

		final class SF_18 {
			public static final FontRenderer SF_18 = SF.ofSize(18);

			private SF_18() {
			}
		}

		final class SF_20 {
			public static final FontRenderer SF_20 = SF.ofSize(20);

			private SF_20() {
			}
		}
	}
}
