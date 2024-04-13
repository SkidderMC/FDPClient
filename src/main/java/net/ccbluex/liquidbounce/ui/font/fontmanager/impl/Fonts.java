/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.impl;


import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontFamily;
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontManager;
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer;
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontType;

@SuppressWarnings("SpellCheckingInspection")
public interface Fonts {

	FontManager fontManager = SimpleFontManager.create();
	static FontManager getFontManager() {
		return fontManager;
	}

	FontManager FONT_MANAGER = getFontManager();

	interface ICONFONT {

		FontFamily ICONFONT = FONT_MANAGER.fontFamily(FontType.ICONFONT);

		final class ICONFONT_16 { public static final FontRenderer ICONFONT_16 = ICONFONT.ofSize(16); private ICONFONT_16() {} }
		final class ICONFONT_17 { public static final FontRenderer ICONFONT_17 = ICONFONT.ofSize(17); private ICONFONT_17() {} }
		final class ICONFONT_18 { public static final FontRenderer ICONFONT_18 = ICONFONT.ofSize(18); private ICONFONT_18() {} }
		final class ICONFONT_20 { public static final FontRenderer ICONFONT_20 = ICONFONT.ofSize(20); private ICONFONT_20() {} }
		final class ICONFONT_24 { public static final FontRenderer ICONFONT_24 = ICONFONT.ofSize(24); private ICONFONT_24() {} }
		final class ICONFONT_32 { public static final FontRenderer ICONFONT_32 = ICONFONT.ofSize(32); private ICONFONT_32() {} }
		final class ICONFONT_35 { public static final FontRenderer ICONFONT_35 = ICONFONT.ofSize(35); private ICONFONT_35() {} }
		final class ICONFONT_50 { public static final FontRenderer ICONFONT_50 = ICONFONT.ofSize(50); private ICONFONT_50() {} }
	}

	interface CheckFont {

		FontFamily CheckFont = FONT_MANAGER.fontFamily(FontType.Check);

		final class CheckFont_16 { public static final FontRenderer CheckFont_16 = CheckFont.ofSize(16); private CheckFont_16() {} }
		final class CheckFont_17 { public static final FontRenderer CheckFont_17 = CheckFont.ofSize(17); private CheckFont_17() {} }

		final class CheckFont_18 { public static final FontRenderer CheckFont_18 = CheckFont.ofSize(18); private CheckFont_18() {} }
		final class CheckFont_20 { public static final FontRenderer CheckFont_20 = CheckFont.ofSize(20); private CheckFont_20() {} }
		final class CheckFont_24 { public static final FontRenderer CheckFont_24 = CheckFont.ofSize(24); private CheckFont_24() {} }
		final class CheckFont_32 { public static final FontRenderer CheckFont_32 = CheckFont.ofSize(32); private CheckFont_32() {} }
		final class CheckFont_35 { public static final FontRenderer CheckFont_35 = CheckFont.ofSize(35); private CheckFont_35() {} }
		final class CheckFont_50 { public static final FontRenderer CheckFont_50 = CheckFont.ofSize(50); private CheckFont_50() {} }

	}


	interface SF {

		FontFamily SF = FONT_MANAGER.fontFamily(FontType.SF);
		final class SF_9 { public static final FontRenderer SF_9 = SF.ofSize(9); private SF_9() {} }
		final class SF_11 { public static final FontRenderer SF_11 = SF.ofSize(11); private SF_11() {} }
		final class SF_14 { public static final FontRenderer SF_14 = SF.ofSize(14); private SF_14() {} }
		final class SF_15 { public static final FontRenderer SF_15 = SF.ofSize(15); private SF_15() {} }
		final class SF_16 { public static final FontRenderer SF_16 = SF.ofSize(16); private SF_16() {} }
		final class SF_17 { public static final FontRenderer SF_17 = SF.ofSize(17); private SF_17() {} }
		final class SF_18 { public static final FontRenderer SF_18 = SF.ofSize(18); private SF_18() {} }
		final class SF_19 { public static final FontRenderer SF_19 = SF.ofSize(19); private SF_19() {} }
		final class SF_20 { public static final FontRenderer SF_20 = SF.ofSize(20); private SF_20() {} }
		final class SF_21 { public static final FontRenderer SF_21 = SF.ofSize(21); private SF_21() {} }
		final class SF_22 { public static final FontRenderer SF_22 = SF.ofSize(22); private SF_22() {} }
		final class SF_23 { public static final FontRenderer SF_23 = SF.ofSize(23); private SF_23() {} }
		final class SF_24 { public static final FontRenderer SF_24 = SF.ofSize(24); private SF_24() {} }
		final class SF_25 { public static final FontRenderer SF_25 = SF.ofSize(25); private SF_25() {} }
		final class SF_26 { public static final FontRenderer SF_26 = SF.ofSize(26); private SF_26() {} }
		final class SF_27 { public static final FontRenderer SF_27 = SF.ofSize(27); private SF_27() {} }
		final class SF_28 { public static final FontRenderer SF_28 = SF.ofSize(28); private SF_28() {} }
		final class SF_29 { public static final FontRenderer SF_29 = SF.ofSize(29); private SF_29() {} }
		final class SF_30 { public static final FontRenderer SF_30 = SF.ofSize(30); private SF_30() {} }
		final class SF_31 { public static final FontRenderer SF_31 = SF.ofSize(31); private SF_31() {} }
		final class SF_40 { public static final FontRenderer SF_40 = SF.ofSize(40); private SF_40() {} }
		final class SF_50 { public static final FontRenderer SF_50 = SF.ofSize(42); private SF_50() {} }
	}

	interface SFBOLD {

		FontFamily SFBOLD = FONT_MANAGER.fontFamily(FontType.SFBOLD);
		final class SFBOLD_26 { public static final FontRenderer SFBOLD_26 = SFBOLD.ofSize(26); private SFBOLD_26() {} }
		final class SFBOLD_25 { public static final FontRenderer SFBOLD_25 = SFBOLD.ofSize(25); private SFBOLD_25() {} }

		final class SFBOLD_10 { public static final FontRenderer SFBOLD_10 = SFBOLD.ofSize(10); private SFBOLD_10() {} }
		final class SFBOLD_11 { public static final FontRenderer SFBOLD_11 = SFBOLD.ofSize(11); private SFBOLD_11() {} }
		final class SFBOLD_12 { public static final FontRenderer SFBOLD_12 = SFBOLD.ofSize(12); private SFBOLD_12() {} }
		final class SFBOLD_13 { public static final FontRenderer SFBOLD_13 = SFBOLD.ofSize(13); private SFBOLD_13() {} }
		final class SFBOLD_15 { public static final FontRenderer SFBOLD_15 = SFBOLD.ofSize(15); private SFBOLD_15() {} }
		final class SFBOLD_14 { public static final FontRenderer SFBOLD_14 = SFBOLD.ofSize(14); private SFBOLD_14() {} }
		final class SFBOLD_16 { public static final FontRenderer SFBOLD_16 = SFBOLD.ofSize(16); private SFBOLD_16() {} }
		final class SFBOLD_17 { public static final FontRenderer SFBOLD_17 = SFBOLD.ofSize(17); private SFBOLD_17() {} }
		final class SFBOLD_18 { public static final FontRenderer SFBOLD_18 = SFBOLD.ofSize(18); private SFBOLD_18() {} }
		final class SFBOLD_19 { public static final FontRenderer SFBOLD_19 = SFBOLD.ofSize(19); private SFBOLD_19() {} }
		final class SFBOLD_20 { public static final FontRenderer SFBOLD_20 = SFBOLD.ofSize(20); private SFBOLD_20() {} }
		final class SFBOLD_22 { public static final FontRenderer SFBOLD_22 = SFBOLD.ofSize(22); private SFBOLD_22() {} }



		final class SFBOLD_28 { public static final FontRenderer SFBOLD_28 = SFBOLD.ofSize(28); private SFBOLD_28() {} }
		final class SFBOLD_35 { public static final FontRenderer SFBOLD_35 = SFBOLD.ofSize(35); private SFBOLD_35() {} }
	}

}
