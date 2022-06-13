/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.impl;

import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.WbxMain;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontFamily;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontManager;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontRenderer;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontType;

@SuppressWarnings("SpellCheckingInspection")
public interface Fonts {

	FontManager FONT_MANAGER = WbxMain.getFontManager();

	interface OXIDE {
		FontFamily OXIDE = FONT_MANAGER.fontFamily(FontType.OXIDE);

		final class OXIDE_55 { public static final FontRenderer OXIDE_55 = OXIDE.ofSize(40); private OXIDE_55() {} }
		final class OXIDE_18 { public static final FontRenderer OXIDE_18 = OXIDE.ofSize(16); private OXIDE_18() {} }
	}

	interface ICONFONT {

		FontFamily ICONFONT = FONT_MANAGER.fontFamily(FontType.ICONFONT);

		final class ICONFONT_16 { public static final FontRenderer ICONFONT_16 = ICONFONT.ofSize(16); private ICONFONT_16() {} }
		final class ICONFONT_20 { public static final FontRenderer ICONFONT_20 = ICONFONT.ofSize(20); private ICONFONT_20() {} }
		final class ICONFONT_24 { public static final FontRenderer ICONFONT_24 = ICONFONT.ofSize(24); private ICONFONT_24() {} }
		final class ICONFONT_32 { public static final FontRenderer ICONFONT_32 = ICONFONT.ofSize(32); private ICONFONT_32() {} }
		final class ICONFONT_35 { public static final FontRenderer ICONFONT_35 = ICONFONT.ofSize(35); private ICONFONT_35() {} }
		final class ICONFONT_50 { public static final FontRenderer ICONFONT_50 = ICONFONT.ofSize(50); private ICONFONT_50() {} }
	}
	interface FluxICONFONT {

		FontFamily FluxICONFONT = FONT_MANAGER.fontFamily(FontType.FluxICONFONT);

		final class FluxICONFONT_40 { public static final FontRenderer FluxICONFONT_40 = FluxICONFONT.ofSize(40); private FluxICONFONT_40() {} }
		final class FluxICONFONT_18 { public static final FontRenderer FluxICONFONT_18 = FluxICONFONT.ofSize(18); private FluxICONFONT_18() {} }
		final class FluxICONFONT_10 { public static final FontRenderer FluxICONFONT_10 = FluxICONFONT.ofSize(10); private FluxICONFONT_10() {} }
	}
	interface TenacityBold {
		FontFamily TenacityBold = FONT_MANAGER.fontFamily(FontType.TenacityBold);
		final class TenacityBold40 { public static final FontRenderer TenacityBold40 = TenacityBold.ofSize(40); private TenacityBold40() {} }
	}
	interface CheckFont {

		FontFamily CheckFont = FONT_MANAGER.fontFamily(FontType.Check);

		final class CheckFont_16 { public static final FontRenderer CheckFont_16 = CheckFont.ofSize(16); private CheckFont_16() {} }
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
		final class SF_50 { public static final FontRenderer SF_50 = SF.ofSize(45); private SF_50() {} }
	}

	interface CHINESE {

		FontFamily CHINESE = FONT_MANAGER.fontFamily(FontType.CHINESE);

		final class CHINESE_14 { public static final FontRenderer CHINESE_14 = CHINESE.ofSize(14); private CHINESE_14() {} }
		final class CHINESE_15 { public static final FontRenderer CHINESE_15 = CHINESE.ofSize(15); private CHINESE_15() {} }
		final class CHINESE_16 { public static final FontRenderer CHINESE_16 = CHINESE.ofSize(16); private CHINESE_16() {} }
		final class CHINESE_17 { public static final FontRenderer CHINESE_17 = CHINESE.ofSize(17); private CHINESE_17() {} }
		final class CHINESE_18 { public static final FontRenderer CHINESE_18 = CHINESE.ofSize(18); private CHINESE_18() {} }
		final class CHINESE_19 { public static final FontRenderer CHINESE_19 = CHINESE.ofSize(19); private CHINESE_19() {} }
		final class CHINESE_20 { public static final FontRenderer CHINESE_20 = CHINESE.ofSize(20); private CHINESE_20() {} }
		final class CHINESE_21 { public static final FontRenderer CHINESE_21 = CHINESE.ofSize(21); private CHINESE_21() {} }
		final class CHINESE_22 { public static final FontRenderer CHINESE_22 = CHINESE.ofSize(22); private CHINESE_22() {} }
		final class CHINESE_23 { public static final FontRenderer CHINESE_23 = CHINESE.ofSize(23); private CHINESE_23() {} }
		final class CHINESE_24 { public static final FontRenderer CHINESE_24 = CHINESE.ofSize(24); private CHINESE_24() {} }
		final class CHINESE_25 { public static final FontRenderer CHINESE_25 = CHINESE.ofSize(25); private CHINESE_25() {} }
		final class CHINESE_26 { public static final FontRenderer CHINESE_26 = CHINESE.ofSize(26); private CHINESE_26() {} }
		final class CHINESE_27 { public static final FontRenderer CHINESE_27 = CHINESE.ofSize(27); private CHINESE_27() {} }
		final class CHINESE_28 { public static final FontRenderer CHINESE_28 = CHINESE.ofSize(28); private CHINESE_28() {} }
		final class CHINESE_29 { public static final FontRenderer CHINESE_29 = CHINESE.ofSize(29); private CHINESE_29() {} }
		final class CHINESE_30 { public static final FontRenderer CHINESE_30 = CHINESE.ofSize(30); private CHINESE_30() {} }
		final class CHINESE_31 { public static final FontRenderer CHINESE_31 = CHINESE.ofSize(31); private CHINESE_31() {} }
		final class CHINESE_50 { public static final FontRenderer CHINESE_50 = CHINESE.ofSize(45); private CHINESE_50() {} }
	}
	interface SFTHIN {

		FontFamily SFTHIN = FONT_MANAGER.fontFamily(FontType.SFTHIN);

		final class SFTHIN_10 { public static final FontRenderer SFTHIN_10 = SFTHIN.ofSize(10); private SFTHIN_10() {} }
		final class SFTHIN_12 { public static final FontRenderer SFTHIN_12 = SFTHIN.ofSize(12); private SFTHIN_12() {} }
		final class SFTHIN_16 { public static final FontRenderer SFTHIN_16 = SFTHIN.ofSize(16); private SFTHIN_16() {} }
		final class SFTHIN_17 { public static final FontRenderer SFTHIN_17 = SFTHIN.ofSize(17); private SFTHIN_17() {} }
		final class SFTHIN_18 { public static final FontRenderer SFTHIN_18 = SFTHIN.ofSize(18); private SFTHIN_18() {} }
		final class SFTHIN_19 { public static final FontRenderer SFTHIN_19 = SFTHIN.ofSize(19); private SFTHIN_19() {} }
		final class SFTHIN_20 { public static final FontRenderer SFTHIN_20 = SFTHIN.ofSize(20); private SFTHIN_20() {} }
		final class SFTHIN_28 { public static final FontRenderer SFTHIN_28 = SFTHIN.ofSize(28); private SFTHIN_28() {} }
	}

	interface SFBOLD {

		FontFamily SFBOLD = FONT_MANAGER.fontFamily(FontType.SFBOLD);

		final class SFBOLD_11 { public static final FontRenderer SFBOLD_11 = SFBOLD.ofSize(11); private SFBOLD_11() {} }
		final class SFBOLD_12 { public static final FontRenderer SFBOLD_12 = SFBOLD.ofSize(12); private SFBOLD_12() {} }
		final class SFBOLD_16 { public static final FontRenderer SFBOLD_16 = SFBOLD.ofSize(16); private SFBOLD_16() {} }
		final class SFBOLD_18 { public static final FontRenderer SFBOLD_18 = SFBOLD.ofSize(18); private SFBOLD_18() {} }
		final class SFBOLD_20 { public static final FontRenderer SFBOLD_20 = SFBOLD.ofSize(20); private SFBOLD_20() {} }
		final class SFBOLD_22 { public static final FontRenderer SFBOLD_22 = SFBOLD.ofSize(22); private SFBOLD_22() {} }
		final class SFBOLD_26 { public static final FontRenderer SFBOLD_26 = SFBOLD.ofSize(26); private SFBOLD_26() {} }
		final class SFBOLD_28 { public static final FontRenderer SFBOLD_28 = SFBOLD.ofSize(28); private SFBOLD_28() {} }
		final class SFBOLD_35 { public static final FontRenderer SFBOLD_35 = SFBOLD.ofSize(35); private SFBOLD_35() {} }
	}
	interface TahomaBold {

		FontFamily TahomaBold = FONT_MANAGER.fontFamily(FontType.TahomaBold);

		final class TahomaBold_11 { public static final FontRenderer TahomaBold_11 = TahomaBold.ofSize(11); private TahomaBold_11() {} }
		final class TahomaBold_12 { public static final FontRenderer TahomaBold_12 = TahomaBold.ofSize(12); private TahomaBold_12() {} }
		final class TahomaBold_14 { public static final FontRenderer TahomaBold_14 = TahomaBold.ofSize(14); private TahomaBold_14() {} }
		final class TahomaBold_16 { public static final FontRenderer TahomaBold_16 = TahomaBold.ofSize(16); private TahomaBold_16() {} }
		final class TahomaBold_18 { public static final FontRenderer TahomaBold_18 = TahomaBold.ofSize(18); private TahomaBold_18() {} }
		final class TahomaBold_20 { public static final FontRenderer TahomaBold_20 = TahomaBold.ofSize(20); private TahomaBold_20() {} }
		final class TahomaBold_22 { public static final FontRenderer TahomaBold_22 = TahomaBold.ofSize(22); private TahomaBold_22() {} }
		final class TahomaBold_26 { public static final FontRenderer TahomaBold_26 = TahomaBold.ofSize(26); private TahomaBold_26() {} }
		final class TahomaBold_28 { public static final FontRenderer TahomaBold_28 = TahomaBold.ofSize(28); private TahomaBold_28() {} }
		final class TahomaBold_35 { public static final FontRenderer TahomaBold_35 = TahomaBold.ofSize(35); private TahomaBold_35() {} }

	}
	interface Tahoma {

		FontFamily Tahoma = FONT_MANAGER.fontFamily(FontType.Tahoma);

		final class Tahoma_11 { public static final FontRenderer Tahoma_11 = Tahoma.ofSize(11); private Tahoma_11() {} }
		final class Tahoma_12 { public static final FontRenderer Tahoma_12 = Tahoma.ofSize(12); private Tahoma_12() {} }
		final class Tahoma_14 { public static final FontRenderer Tahoma_14 = Tahoma.ofSize(14); private Tahoma_14() {} }
		final class Tahoma_16 { public static final FontRenderer Tahoma_16 = Tahoma.ofSize(16); private Tahoma_16() {} }
		final class Tahoma_18 { public static final FontRenderer Tahoma_18 = Tahoma.ofSize(18); private Tahoma_18() {} }
		final class Tahoma_20 { public static final FontRenderer Tahoma_20 = Tahoma.ofSize(20); private Tahoma_20() {} }
		final class Tahoma_22 { public static final FontRenderer Tahoma_22 = Tahoma.ofSize(22); private Tahoma_22() {} }
		final class Tahoma_26 { public static final FontRenderer Tahoma_26 = Tahoma.ofSize(26); private Tahoma_26() {} }
		final class Tahoma_28 { public static final FontRenderer Tahoma_28 = Tahoma.ofSize(28); private Tahoma_28() {} }
		final class Tahoma_35 { public static final FontRenderer Tahoma_35 = Tahoma.ofSize(35); private Tahoma_35() {} }

	}
}
