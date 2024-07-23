/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.button;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static net.ccbluex.liquidbounce.ui.font.Fonts.fontSmall;
import static net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius;
import static net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundOutline;

public class QuitButton extends ImageButton {

	public QuitButton(int x, int y) {
		super("QUIT", new ResourceLocation("fdpclient/mainmenu/exit.png"), x, y);
	}

	@Override
	public void drawButton(int mouseX, int mouseY) {
		boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

		if (hovered) {
			if (getHoverFade() < 40) setHoverFade(getHoverFade() + 10);
			drawHoverEffect();
		} else {
			if (getHoverFade() > 0) setHoverFade(getHoverFade() - 10);
		}

		drawCustomShapeWithRadius(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, 2, new Color(30, 30, 30, 60));
		drawCustomShapeWithRadius(getX(), getY(), getWidth(), getHeight(), 2, new Color(255, 255 - getHoverFade() * 4, 255 - getHoverFade() * 4, 38 + getHoverFade()));

		drawRoundOutline(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 2, 3, new Color(255, 255, 255, 30).getRGB());

		int color = new Color(232, 232, 232, 183).getRGB();
		float f1 = (color >> 24 & 0xFF) / 255.0F;
		float f2 = (color >> 16 & 0xFF) / 255.0F;
		float f3 = (color >> 8 & 0xFF) / 255.0F;
		float f4 = (color & 0xFF) / 255.0F;
		GL11.glColor4f(f2, f3, f4, f1);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();

		Minecraft.getMinecraft().getTextureManager().bindTexture(image);
		Gui.drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 6, 6, 6, 6);

		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}

	//@Override
	protected void drawHoverEffect() {
		int w = (int) (Fonts.font35.getStringWidth(this.text));
		drawCustomShapeWithRadius(this.x + (float) (this.width - w) / 2, this.y + 17, w, 7, 2, new Color(0, 0, 0, 126));
		fontSmall.drawCenteredTextScaled(this.text, this.x + this.width / 2, this.y + 18, new Color(255, 255, 255, 135).getRGB(), 0.9F);
	}
}
