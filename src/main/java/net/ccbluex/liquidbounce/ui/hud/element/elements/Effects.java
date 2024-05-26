/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.hud.element.Border;
import net.ccbluex.liquidbounce.ui.hud.element.Element;
import net.ccbluex.liquidbounce.ui.hud.element.ElementInfo;
import net.ccbluex.liquidbounce.ui.realpha;
import net.ccbluex.liquidbounce.utils.render.Colors;
import net.ccbluex.liquidbounce.utils.render.PotionData;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.Translate;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FontValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;

@ElementInfo(name = "Effects")
public class Effects extends Element {

    private final ListValue modeValue = new ListValue("Mode", new String[]{"FDP", "Default"}, "FDP");
    public static FontValue font = new FontValue("Font", Fonts.minecraftFont);
    public final BoolValue shadow = new BoolValue("Shadow", true);
    public final BoolValue iconValue = new BoolValue("Icon", true);
    public final BoolValue nameValue = new BoolValue("Name", true);
    public final BoolValue colorValue = new BoolValue("Color", false);

    private final float x = 10.0f;
    private final float y = 100.0f;
    private final Map<Potion, PotionData> potionMap = new HashMap<>();
    
    protected Border draw() {
        if (modeValue.get().equals("FDP")) {
        GlStateManager.pushMatrix();
        int y = 0;
        for (final PotionEffect potionEffect : mc.thePlayer.getActivePotionEffects()) {
            final Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
            final String name = I18n.format(potion.getName());
            final PotionData potionData;
            if(potionMap.containsKey(potion) && potionMap.get(potion).level == potionEffect.getAmplifier())
                potionData = potionMap.get(potion);
            else
                potionMap.put(potion, (potionData = new PotionData(new Translate(0, - 40F + y), potionEffect.getAmplifier())));
            boolean flag = true;
            for(final PotionEffect checkEffect : mc.thePlayer.getActivePotionEffects())
                if (checkEffect.getAmplifier() == potionData.level) {
                    flag = false;
                    break;
                }
            if(flag) potionMap.remove(potion);
            int potionTime,potionMaxTime;
            try {
                potionTime = Integer.parseInt(Potion.getDurationString(potionEffect).split(":")[0]);
                potionMaxTime = Integer.parseInt(Potion.getDurationString(potionEffect).split(":")[1]);
            } catch(Exception ignored) {
                potionTime = 100;
                potionMaxTime = 1000;
            }
            final int lifeTime = (potionTime * 60 + potionMaxTime);
            if (potionData.getMaxTimer() == 0 || lifeTime > (double)potionData.getMaxTimer()) potionData.maxTimer = lifeTime;
            float state = 0.0F;
            if (lifeTime >= 0.0D) state = (float)(lifeTime / (double)((float)potionData.getMaxTimer()) * 100.0D);
            final int position = Math.round(potionData.translate.getY() + 5);
            state = Math.max(state, 2.0F);
            potionData.translate.interpolate(0, y, 0.1);
            potionData.animationX = (float) getAnimationState(potionData.getAnimationX(), 1.2F * state, Math.max(10.0F, Math.abs(potionData.animationX - 1.2F * state) * 15.0F) * 0.3F);

            RenderUtils.drawRect(0, potionData.translate.getY(), 120, potionData.translate.getY() + 30F, realpha.reAlpha(Colors.GREY.c, 0.1F));
            RenderUtils.drawRect(0, potionData.translate.getY(), potionData.animationX, potionData.translate.getY() + 30F, realpha.reAlpha((new Color(34, 24, 20)).brighter().getRGB(), 0.3F));
            RenderUtils.drawShadow(0, Math.round(potionData.translate.getY()), 120, 30);
            float posY = potionData.translate.getY() + 13F;
            Fonts.font40.drawString(name + " " + intToRomanByGreedy(potionEffect.getAmplifier() + 1), 29F, posY - mc.fontRendererObj.FONT_HEIGHT, realpha.reAlpha(Colors.WHITE.c, 0.8F));
            Fonts.font35.drawString(Potion.getDurationString(potionEffect), 29F, posY + 4.0F, realpha.reAlpha((new Color(200, 200, 200)).getRGB(), 0.5F));
            if (potion.hasStatusIcon()) {
                GlStateManager.pushMatrix();
                GL11.glDisable(2929);
                GL11.glEnable(3042);
                GL11.glDepthMask(false);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                int statusIconIndex = potion.getStatusIconIndex();
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                mc.ingameGUI.drawTexturedModalRect(6F, (float)(position + 1), statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
                GL11.glDepthMask(true);
                GL11.glDisable(3042);
                GL11.glEnable(2929);
                GlStateManager.popMatrix();
            }
            y -= 35;
        }
        GlStateManager.popMatrix();
        return new Border(0F, 0F, 120F, 30F);
    }
        if (modeValue.get().equals("Default")) {
            int xOffset = 21;
            int yOffset = 14;

            Collection<PotionEffect> activePotions = mc.thePlayer.getActivePotionEffects();
            ArrayList<PotionEffect> sortedPotions = new ArrayList<>(activePotions);
            sortedPotions.sort(Comparator.comparingInt(potion -> -potion.getDuration()));

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableLighting();

            FontRenderer fontRenderer = font.get();

            for (PotionEffect potion : sortedPotions) {
                Potion effect = Potion.potionTypes[potion.getPotionID()];
                assert (effect != null);

                if (effect.hasStatusIcon() && iconValue.get()) {
                    drawStatusIcon(xOffset, yOffset, effect.getStatusIconIndex() % 8 * 18, 198 + effect.getStatusIconIndex() / 8 * 18);
                }

                if (nameValue.get()) {
                    drawPotionName(potion, effect, xOffset, yOffset, fontRenderer);
                }

                if (nameValue.get()) {
                    drawPotionDuration(potion, xOffset, yOffset, fontRenderer);
                }

                drawPotionDuration(potion, xOffset, yOffset, fontRenderer);

                yOffset += fontRenderer.FONT_HEIGHT * 2 + 4; // Add some space between the effects
            }

            float height = yOffset - 4;

            float width = 100.0f;
            return new Border(0, 0, width, height);
        }
        return null;
    }

    private String intToRomanByGreedy(int num) {
        final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < values.length && num >= 0; i++)
            while (values[i] <= num){
                num -= values[i];
                stringBuilder.append(symbols[i]);
            }

        return stringBuilder.toString();
    }
    
    private double getAnimationState(double animation, double finalState, double speed) {
        float add = (float) (0.01 * speed);
        if (animation < finalState) {
            if (animation + add < finalState)
                animation += add;
            else
                animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }

    private void drawStatusIcon(int xOffset, int yOffset, int textureX, int textureY) {
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
        RenderUtils.drawTexturedModalRect((int) x + xOffset - 20, (int) y + yOffset, textureX, textureY, 18, 18, 0);
    }

    private void drawPotionName(PotionEffect potion, Potion effect, int xOffset, int yOffset, FontRenderer fontRenderer) {
        String level = I18n.format(effect.getName());
        if (potion.getAmplifier() > 0) {
            int amplifier = Math.min(potion.getAmplifier() + 1, 4);
            level += " " + I18n.format("enchantment.level." + amplifier);
        }

        int potionColor = colorValue.get() ? getPotionColor(potion) : Color.WHITE.getRGB(); // Use white color when colorValue is false
        fontRenderer.drawString(level, x + xOffset, y + yOffset, potionColor, shadow.get());
    }

    private void drawPotionDuration(PotionEffect potion, int xOffset, int yOffset, FontRenderer fontRenderer) {
        String durationString = Potion.getDurationString(potion);
        int potionColor = colorValue.get() ? getPotionColor(potion) : Color.WHITE.getRGB(); // Use white color when colorValue is false
        fontRenderer.drawString(durationString, x + xOffset, y + yOffset + (nameValue.get() ? fontRenderer.FONT_HEIGHT : (float) fontRenderer.FONT_HEIGHT / 2), potionColor, shadow.get());
    }

    private int getPotionColor(PotionEffect potion) {
        if (potion.getDuration() < 200) {
            return new Color(215, 59, 59).getRGB();
        } else if (potion.getDuration() < 400) {
            return new Color(231, 143, 32).getRGB();
        } else {
            return new Color(172, 171, 171).getRGB();
        }
    }


    @Override
    public Border drawElement(float partialTicks) {
        return draw();
    }
}
