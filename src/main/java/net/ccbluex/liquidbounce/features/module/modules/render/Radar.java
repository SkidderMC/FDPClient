/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.render.Colors;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

import java.awt.*;

@ModuleInfo(name = "Radar", category = ModuleCategory.RENDER)
public class Radar extends Module
{
    private boolean dragging;
    float hue;
    public final FloatValue scale = new FloatValue("scale", 2.0f, 1.0f, 3.0f);
    public final FloatValue x = new FloatValue("x", 10.0f, 0.0f, 2000.0f);
    public final FloatValue y = new FloatValue("y", 10.0f, 0.0f, 2000.0f);
    public final FloatValue size = new FloatValue("size", 10.0f, 0.0f, 2000.0f);


    @EventTarget
    public void onRender2D(Render2DEvent e) {
        ScaledResolution sr = new ScaledResolution(mc);
        int size1 = this.size.getValue().intValue();
        float xOffset = this.x.getValue().floatValue();
        float yOffset = this.y.getValue().floatValue();
        float playerOffsetX = (float)mc.thePlayer.posX;
        float playerOffSetZ = (float)mc.thePlayer.posZ;
        int var141 = sr.getScaledWidth();
        int var151 = sr.getScaledHeight();
        int mouseX = Mouse.getX() * var141 / mc.displayWidth;
        int mouseY = var151 - Mouse.getY() * var151 / mc.displayHeight - 1;
        if (mouseX >= xOffset && mouseX <= xOffset + size1 && mouseY >= yOffset - 3f && mouseY <= yOffset + 10.0f && Mouse.getEventButton() == 0) {
            this.dragging = !this.dragging;
        }
        if (!this.dragging || !(mc.currentScreen instanceof GuiChat)) {
            this.dragging = false;
        }
        if (this.hue > 255.0f) {
            this.hue = 0.0f;
        }
        float h = this.hue;
        float h2 = this.hue + 85.0f;
        float h3 = this.hue + 170.0f;
        if (h > 255.0f) {
            h = 0.0f;
        }
        if (h2 > 255.0f) {
            h2 -= 255.0f;
        }
        if (h3 > 255.0f) {
            h3 -= 255.0f;
        }
        Color color33 = Color.getHSBColor(h / 255.0f, 0.9f, 1.0f);
        Color color332 = Color.getHSBColor(h2 / 255.0f, 0.9f, 1.0f);
        Color color333 = Color.getHSBColor(h3 / 255.0f, 0.9f, 1.0f);
        int color1 = color33.getRGB();
        int color2 = color332.getRGB();
        int color3 = color333.getRGB();
        this.hue += 0.1f;


        RenderUtils.rectangleBordered(xOffset + 3.0, yOffset + 3.0, xOffset + size1 - 3.0, yOffset + size1 - 3.0, 0.5, new Color(0,0,0,150).getRGB(), new Color(0,0,0,150).getRGB());
        RenderUtils.drawGradientSideways(xOffset + 3.0, yOffset + 2.6, xOffset + size1 / 2.0, yOffset + 3.0, color1, color2);
        RenderUtils.drawGradientSideways(xOffset + size1 / 2.0, yOffset + 2.6, xOffset + size1 - 3.0, yOffset + 3.0, color2, color3);
        RenderUtils.rectangle(xOffset + size1 / 2.0 - 0.5, yOffset + 3.5, xOffset + size1 / 2.0 + 0.5, yOffset + size1 - 3.5, Colors.getColor(255, 80));
        RenderUtils.rectangle(xOffset + 3.5, yOffset + size1 / 2.0 - 0.5, xOffset + size1 - 3.5, yOffset + size1 / 2.0 + 0.5, Colors.getColor(255, 80));
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            EntityPlayer ent;
            if (!(o instanceof EntityPlayer) || !(ent = (EntityPlayer)o).isEntityAlive() || ent == mc.thePlayer || ent.isInvisible() || ent.isInvisibleToPlayer((EntityPlayer)mc.thePlayer)) continue;
            float pTicks = mc.timer.renderPartialTicks;
            float posX = (float)((ent.posX + (ent.posX - ent.lastTickPosX) * pTicks - playerOffsetX) * this.scale.getValue());
            float posZ = (float)((ent.posZ + (ent.posZ - ent.lastTickPosZ) * pTicks - playerOffSetZ) * this.scale.getValue());
            String formattedText = ent.getDisplayName().getFormattedText();
            int color = mc.thePlayer.canEntityBeSeen((Entity)ent) ? new Color(255,255,255).getRGB() : new Color(120,120,120).getRGB();
            int i = 0;
            while (i < formattedText.length()) {
                if (formattedText.charAt(i) == '\u00a7' && i + 1 < formattedText.length()) {
                    int index = "0123456789abcdefklmnorg".indexOf(Character.toLowerCase(formattedText.charAt(i + 1)));
                    if (index < 16) {
                        try {
                            Color color21 = new Color(this.mc.fontRendererObj.getColorCode("0123456789abcdef".toCharArray()[index]));
                            color = this.getColor(color21.getRed(), color21.getGreen(), color21.getBlue(), 255);
                        }
                        catch (ArrayIndexOutOfBoundsException ex) {

                        }
                    }
                }
                ++i;
            }
            if(ent.hurtTime > 0) {
                color = new Color(255,0,0).getRGB();
            }
            float cos = -(float)Math.cos(mc.thePlayer.rotationYaw * 0.017453292519943295);
            float sin = (float)Math.sin(mc.thePlayer.rotationYaw * 0.017453292519943295);
            float rotY = -((- posZ) * cos - posX * sin);
            float rotX = -((- posX) * cos + posZ * sin);
            if (rotY > size1 / 2f - 9) {
                rotY = size1 / 2f - 9;
            } else if (rotY < size1 / -2f + 2) {
                rotY = size1 / -2f + 2;
            }
            if (rotX > size1 / 2f - 9) {
                rotX = size1 / 2f - 9;
            } else if (rotX < size1 / -2f + 2) {
                rotX = size1 / -2f + 2;
            }
            RenderUtils.rectangleBordered(xOffset + 4 + size1 / 2.0 + rotX - 1.5, yOffset + 4 + size1 / 2.0 + rotY - 1.5, xOffset + 4 + size1 / 2.0 + rotX + 1.5, yOffset + 4 + size1 / 2.0 + rotY + 1.5, 0.5, color, Colors.getColor(46));
        }
    }

    public int getColor(int p_clamp_int_0_, int p_clamp_int_0_2, int p_clamp_int_0_3, int p_clamp_int_0_4) {
        return MathHelper.clamp_int(p_clamp_int_0_4, 0, 255) << 24 | MathHelper.clamp_int(p_clamp_int_0_, 0, 255) << 16 | MathHelper.clamp_int(p_clamp_int_0_2, 0, 255) << 8 | MathHelper.clamp_int(p_clamp_int_0_3, 0, 255);
    }

    private float findAngle(float x, float x2, float y, float y2) {
        return (float)(Math.atan2(y2 - y, x2 - x) * 180.0 / 3.141592653589793);
    }
}
