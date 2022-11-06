/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventState;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.MotionEvent;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.Colors;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ModuleInfo(name = "Effects", category = ModuleCategory.RENDER)
public class Effects extends Module {
    public static final FloatValue bordRad = new FloatValue("BorderRadius",6F,0F,8F);
    public static final IntegerValue r = new IntegerValue("R", 0, 0, 255);
    public static final IntegerValue g = new IntegerValue("G", 160, 0, 255);
    public static final IntegerValue b = new IntegerValue("B", 255, 0, 255);

    public Effects() {
        setState(true);
    }
    @EventTarget
    public void onRender2D(final Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        float width1 = sr.getScaledWidth();
        float height1 = sr.getScaledHeight();
        this.renderPotionStatus((int) width1, (int) height1);

        if (mc.currentScreen instanceof GuiHudDesigner)
            return;
    }

    @EventTarget
    public void onMotion(final MotionEvent event) {
        if (event.getEventState() == EventState.POST) {
            double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        }
    }

    Map<net.minecraft.potion.Potion, Double> timerMap = new HashMap<net.minecraft.potion.Potion, Double>();

    private int x;

    public void renderPotionStatus(int width, int height) {
        x = 0;
        ScaledResolution sr = new ScaledResolution(mc);
        for (PotionEffect effect : (Collection<PotionEffect>) this.mc.thePlayer.getActivePotionEffects()) {
            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
            String PType = I18n.format(potion.getName());
            int minutes = -1;
            int seconds = -2;

            try {
                minutes = Integer.parseInt(potion.getDurationString(effect).split(":")[0]);
                seconds = Integer.parseInt(potion.getDurationString(effect).split(":")[1]);
            } catch (Exception ex) {
                minutes = 0;
                seconds = 0;
            }

            double total = (minutes * 60) + seconds;

            if (!timerMap.containsKey(potion)) {
                timerMap.put(potion, total);
            }

            if (timerMap.get(potion) == 0 || total > timerMap.get(potion)) {
                timerMap.replace(potion, total);
            }

            switch (effect.getAmplifier()) {
                case 0:
                    PType = PType + " I";
                    break;
                case 1:
                    PType = PType + " II";
                    break;
                case 2:
                    PType = PType + " III";
                    break;
                case 3:
                    PType = PType + " IV";
                    break;
                case 4:
                    PType = PType + " V";
                    break;
                case 5:
                    PType = PType + " VI";
                    break;
                case 6:
                    PType = PType + " VII";
                    break;
                case 7:
                    PType = PType + " VIII";
                    break;
                case 8:
                    PType = PType + " IX";
                    break;
                case 9:
                    PType = PType + " X";
                    break;
                case 10:
                    PType = PType + " X+";
                    break;
                default:
                    break;
            }

            int color = Colors.WHITE.c;

            if (effect.getDuration() < 600 && effect.getDuration() > 300) {
                color = Colors.YELLOW.c;
            } else if (effect.getDuration() < 300) {
                color = Colors.RED.c;
            } else if (effect.getDuration() > 600) {
                color = Colors.WHITE.c;
            }

            int x1 = (int) ((width - 6) * 1.33f);
            int y1 = (int) ((height - 52 - this.mc.fontRendererObj.FONT_HEIGHT + x + 5) * 1.33F);

            RenderUtils.drawRoundedRect(width - 120, height - 60 + x, width - 10, height - 30 + x, bordRad.get(),
                    RenderUtils.reAlpha(Colors.BLACK.c, 0.41f));

            if (potion.hasStatusIcon()) {
                GlStateManager.pushMatrix();

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDepthMask(false);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GL11.glColor4f(1, 1, 1, 1);
                int index = potion.getStatusIconIndex();
                ResourceLocation location = new ResourceLocation("textures/gui/container/inventory.png");
                mc.getTextureManager().bindTexture(location);
                GlStateManager.scale(0.75, 0.75, 0.75);
                mc.ingameGUI.drawTexturedModalRect(x1 - 138, y1 + 8, 0 + index % 8 * 18, 198 + index / 8 * 18, 18, 18);

                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GlStateManager.popMatrix();
            }

            int y = (height - this.mc.fontRendererObj.FONT_HEIGHT + x) - 38;
            Fonts.font35.drawString(PType.replaceAll("\247.", ""), (float) width - 91f,
                    y - this.mc.fontRendererObj.FONT_HEIGHT + 1, potion.getLiquidColor());

            Fonts.font35.drawString(net.minecraft.potion.Potion.getDurationString(effect).replaceAll("\247.", ""),
                    width - 91f, y + 4, RenderUtils.reAlpha(-1, 0.8f));

            x -= 35;
        }
    }
}