/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.BoolValue;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.Color;

import static org.lwjgl.opengl.GL11.*;
@ModuleInfo(name = "Crosshair", category = ModuleCategory.RENDER)
public class Crosshair extends Module {

    //Color
    public ListValue colorModeValue = new ListValue("Color", new String[]{ "Custom", "Slowly" }, "Custom");
    public IntegerValue colorRedValue = new IntegerValue("Red", 0, 0, 255);
    public IntegerValue colorGreenValue = new IntegerValue("Green", 0, 0, 255);
    public IntegerValue colorBlueValue = new IntegerValue("Blue", 0, 0, 255);

    //Rainbow thingy
    private final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F);
	private final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F);
	private final IntegerValue mixerSecondsValue = new IntegerValue("Mixer-Seconds", 2, 1, 10);

    //Size, width, hitmarker
    public FloatValue widthVal = new FloatValue("Width", 2, 0.25F, 10);
    public FloatValue sizeVal = new FloatValue("Size/Length", 7, 0.25F, 15);
    public FloatValue gapVal = new FloatValue("Gap", 5, 0.25F, 15);
    public BoolValue dynamicVal = new BoolValue("Dynamic", true);
    public BoolValue hitMarkerVal = new BoolValue("HitMarker", true);
    public BoolValue noVanillaCH = new BoolValue("NoVanillaCrossHair", true);


    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final ScaledResolution scaledRes = new ScaledResolution(mc);
        float width = widthVal.get();
        float size = sizeVal.get();
        float gap = gapVal.get();

        glPushMatrix();
        RenderUtils.drawBorderedRect(scaledRes.getScaledWidth() / 2F - width, scaledRes.getScaledHeight() / 2F - gap - size - (this.isMoving() ? 2 : 0), scaledRes.getScaledWidth() / 2F + 1.0f + width, scaledRes.getScaledHeight() / 2F - gap - (this.isMoving() ? 2 : 0), 0.5F, new Color(0, 0, 0).getRGB(), getCrosshairColor().getRGB());
        RenderUtils.drawBorderedRect(scaledRes.getScaledWidth() / 2F - width, scaledRes.getScaledHeight() / 2F + gap + 1 + (this.isMoving() ? 2 : 0) - 0.15F, scaledRes.getScaledWidth() / 2F + 1.0f + width, scaledRes.getScaledHeight() / 2F + 1 + gap + size + (this.isMoving() ? 2 : 0) - 0.15F, 0.5F, new Color(0, 0, 0).getRGB(), getCrosshairColor().getRGB());
        RenderUtils.drawBorderedRect(scaledRes.getScaledWidth() / 2F - gap - size - (this.isMoving() ? 2 : 0) + 0.15F, scaledRes.getScaledHeight() / 2F - width, scaledRes.getScaledWidth() / 2F - gap - (this.isMoving() ? 2 : 0) + 0.15F, scaledRes.getScaledHeight() / 2 + 1.0f + width, 0.5F, new Color(0, 0, 0).getRGB(), getCrosshairColor().getRGB());
        RenderUtils.drawBorderedRect(scaledRes.getScaledWidth() / 2F + 1 + gap + (this.isMoving() ? 2 : 0), scaledRes.getScaledHeight() / 2F - width, scaledRes.getScaledWidth() / 2F + size + gap + 1.0F + (this.isMoving() ? 2 : 0), scaledRes.getScaledHeight() / 2 + 1.0f + width, 0.5F, new Color(0, 0, 0).getRGB(), getCrosshairColor().getRGB());
        glPopMatrix();

        GlStateManager.resetColor();
        //glColor4f(0F, 0F, 0F, 0F)

        EntityLivingBase target = ((KillAura)LiquidBounce.moduleManager.getModule(KillAura.class)).getTarget();

        if (hitMarkerVal.get() && target != null && target.hurtTime > 0) {
            glPushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

            glColor4f(1, 1, 1, (float)target.hurtTime / (float)target.maxHurtTime);
            glEnable(GL_LINE_SMOOTH);
            glLineWidth(1F);

            glBegin(3);

            glVertex2f(scaledRes.getScaledWidth() / 2F + gap, scaledRes.getScaledHeight() / 2F + gap);
            glVertex2f(scaledRes.getScaledWidth() / 2F + gap + size, scaledRes.getScaledHeight() / 2F + gap + size);

            glEnd();

            glBegin(3);

            glVertex2f(scaledRes.getScaledWidth() / 2F - gap, scaledRes.getScaledHeight() / 2F - gap);
            glVertex2f(scaledRes.getScaledWidth() / 2F - gap - size, scaledRes.getScaledHeight() / 2F - gap - size);

            glEnd();

            glBegin(3);

            glVertex2f(scaledRes.getScaledWidth() / 2F - gap, scaledRes.getScaledHeight() / 2F + gap);
            glVertex2f(scaledRes.getScaledWidth() / 2F - gap - size, scaledRes.getScaledHeight() / 2F + gap + size);

            glEnd();

            glBegin(3);

            glVertex2f(scaledRes.getScaledWidth() / 2F + gap, scaledRes.getScaledHeight() / 2F - gap);
            glVertex2f(scaledRes.getScaledWidth() / 2F + gap + size, scaledRes.getScaledHeight() / 2F - gap - size);

            glEnd();

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            glPopMatrix();
        }
    }

    private boolean isMoving() {
        return dynamicVal.get() && MovementUtils.isMoving();
    }

    private Color getCrosshairColor() {
        switch (colorModeValue.get()) {
			case "Custom":
				return new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
			case "Slowly":
				return ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get());
		}
    }

}
