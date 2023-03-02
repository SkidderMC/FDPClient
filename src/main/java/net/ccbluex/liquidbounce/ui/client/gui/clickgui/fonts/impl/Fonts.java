/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.impl;

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api.FontFamily;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api.FontManager;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api.FontRenderer;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api.FontType;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.logo.info;
@SuppressWarnings("SpellCheckingInspection")
public interface Fonts {

	FontManager FONT_MANAGER = info.getFontManager();

    interface ICONFONT {

		FontFamily ICONFONT = FONT_MANAGER.fontFamily(FontType.ICONFONT);

		final class ICONFONT_16 {

            private ICONFONT_16() {
			}
		}

		final class ICONFONT_20 {
			public static final FontRenderer ICONFONT_20 = ICONFONT.ofSize(20);

			private ICONFONT_20() {
			}
		}

		final class ICONFONT_24 {
			public static final FontRenderer ICONFONT_24 = ICONFONT.ofSize(24);

			private ICONFONT_24() {
			}
		}

		final class ICONFONT_32 {

            private ICONFONT_32() {
			}
		}

		final class ICONFONT_35 {

            private ICONFONT_35() {
			}
		}

		final class ICONFONT_50 {

            private ICONFONT_50() {
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

		final class CheckFont_16 {

            private CheckFont_16() {
			}
		}

		final class CheckFont_20 {
			public static final FontRenderer CheckFont_20 = CheckFont.ofSize(20);

			private CheckFont_20() {
			}
		}

		final class CheckFont_24 {
			public static final FontRenderer CheckFont_24 = CheckFont.ofSize(24);

			private CheckFont_24() {
			}
		}

		final class CheckFont_32 {

            private CheckFont_32() {
			}
		}

		final class CheckFont_35 {

            private CheckFont_35() {
			}
		}

		final class CheckFont_50 {

            private CheckFont_50() {
			}
		}
	}

	interface SF {

		FontFamily SF = FONT_MANAGER.fontFamily(FontType.SF);

		final class SF_9 {

            private SF_9() {
			}
		}

		final class SF_11 {

            private SF_11() {
			}
		}

		final class SF_14 {
			public static final FontRenderer SF_14 = SF.ofSize(14);

			private SF_14() {
			}
		}

		final class SF_15 {

            private SF_15() {
			}
		}

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

		final class SF_19 {

            private SF_19() {
			}
		}

		final class SF_20 {
			public static final FontRenderer SF_20 = SF.ofSize(20);

			private SF_20() {
			}
		}

		final class SF_21 {

            private SF_21() {
			}
		}

		final class SF_22 {

            private SF_22() {
			}
		}

		final class SF_23 {

            private SF_23() {
			}
		}

		final class SF_24 {

            private SF_24() {
			}
		}

		final class SF_25 {

            private SF_25() {
			}
		}

		final class SF_26 {

            private SF_26() {
			}
		}

		final class SF_27 {

            private SF_27() {
			}
		}

		final class SF_28 {

            private SF_28() {
			}
		}

		final class SF_29 {

            private SF_29() {
			}
		}

		final class SF_30 {

            private SF_30() {
			}
		}

		final class SF_35 {

            private SF_35() {
			}
		}

		final class SF_31 {

            private SF_31() {
			}
		}

		final class SF_50 {

            private SF_50() {
			}
		}
	}

    interface SFBOLD {

		FontFamily SFBOLD = FONT_MANAGER.fontFamily(FontType.SFBOLD);

		final class SFBOLD_11 {

            private SFBOLD_11() {
			}
		}

		final class SFBOLD_12 {

            private SFBOLD_12() {
			}
		}

		final class SFBOLD_16 {

            private SFBOLD_16() {
			}
		}

		final class SFBOLD_18 {
			public static final FontRenderer SFBOLD_18 = SFBOLD.ofSize(18);

			private SFBOLD_18() {
			}
		}

		final class SFBOLD_20 {

            private SFBOLD_20() {
			}
		}

		final class SFBOLD_22 {

            private SFBOLD_22() {
			}
		}

		final class SFBOLD_26 {
			public static final FontRenderer SFBOLD_26 = SFBOLD.ofSize(26);

			private SFBOLD_26() {
			}
		}

		final class SFBOLD_28 {

            private SFBOLD_28() {
			}
		}

		final class SFBOLD_35 {

            private SFBOLD_35() {
			}
		}

		final class SFBOLD_50 {

            private SFBOLD_50() {
			}
		}

    }
}
