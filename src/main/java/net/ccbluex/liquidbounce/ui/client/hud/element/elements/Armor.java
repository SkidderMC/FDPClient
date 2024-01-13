/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements;

import net.ccbluex.liquidbounce.ui.client.hud.element.Border;
import net.ccbluex.liquidbounce.ui.client.hud.element.Element;
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import java.awt.*;

@ElementInfo(name = "Armor")
public class Armor extends Element {
    private final BoolValue enchantValue = new BoolValue("Enchant", true);
    private final ListValue modeValue = new ListValue("Alignment", new String[]{"Horizontal", "Vertical"}, "Vertical");
    private final ListValue showAttributes = new ListValue("Attributes", new String[]{"None", "Value", "Percentage", "All"}, "Percentage");
    private final BoolValue minimalMode = new BoolValue("Minimal Mode", false);
    private final IntegerValue percentageY = new IntegerValue("PositionY", -19, -50, 50);
    private final IntegerValue percentageX = new IntegerValue("PositionX", 21, -50, 50);
    public static IntegerValue a2 = new IntegerValue("Border", 0, 0, 255);
    public static FloatValue ra = new FloatValue("Radius", 4.5f, 0.1f, 8.0f);
    private final IntegerValue r = new IntegerValue("Red", 255, 0, 255);
    private final IntegerValue g = new IntegerValue("Green", 255, 0, 255);
    private final IntegerValue b = new IntegerValue("Blue", 255, 0, 255);
    public static IntegerValue r2 = new IntegerValue("Red-2", 0, 0, 255);
    public static IntegerValue g2 = new IntegerValue("Green-2", 19, 0, 255);
    public static IntegerValue b2 = new IntegerValue("Blue-2", 0, 0, 255);
    private final IntegerValue a = new IntegerValue("Alpha", 255, 0, 255);
    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public Border drawElement(float partialTicks) {
        String mode = modeValue.get();
        EntityPlayerSP player = mc.thePlayer;

        if (mc.playerController.isInCreativeMode()) {
            return "Horizontal".equalsIgnoreCase(mode) ? new Border(0.0f, 0.0f, 72.0f, 17.0f) : new Border(0.0f, 0.0f, 18.0f, 72.0f);
        }

        int color = new Color(r.get(), g.get(), b.get(), a.get()).getRGB();
        boolean isInsideWater = mc.thePlayer.isInsideOfMaterial(Material.water);
        int x = 1;
        int y = isInsideWater ? -10 : 0;

        if (mode.equalsIgnoreCase("Horizontal")) {
            GlStateManager.enableCull();
            drawBackgroundWithContour(x, y, color);
            drawArmorItems(x, y, color, player);
        } else if (mode.equalsIgnoreCase("Vertical")) {
            GlStateManager.enableCull();
            drawBackgroundWithContourVertical(x, y, color);
            drawArmorItemsVertical(x, y, color, player);
        }

        return mode.equalsIgnoreCase("Horizontal") ? new Border(0.0f, 0.0f, 72.0f, 17.0f) : new Border(0.0f, 0.0f, 18.0f, 72.0f);
    }

    private void drawBackgroundWithContour(int x, int y, int color) {
        Color color1 = new Color(r.get(), g.get(), b.get(), a.get());
        Color color2 = new Color(r.get(), g.get(), b.get(), a.get());
        Color color3 = new Color(r2.get(), g2.get(), b2.get(), a2.get());
        Color color4 = new Color(r2.get(), g2.get(), b2.get(), a2.get());

        RoundedUtil.drawGradientRound(x - 2.0f, -12.0f, 75.0f, 40.0f, ra.get(), ColorUtils.applyOpacity(color4, 0.85f), color1, color3, color2);

        Fonts.fontSFUI32.drawString("Armor", x, -8.0f, color);
    }

    private void drawArmorItems(int x, int y, int color, EntityPlayerSP player) {
        RenderItem renderItem = mc.getRenderItem();

        for (int i = 3; i >= 0; --i) {
            ItemStack stack = player.inventory.armorInventory[i];
            if (stack == null) continue;

            renderItem.renderItemIntoGUI(stack, x, y);
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y);

            GlStateManager.pushMatrix();
            if (showAttributes.get().equals("Value")) {
                // Exibe o valor numérico
                float percentageXOffset = (float) percentageX.get();
                float percentageYOffset = (float) percentageY.get();
                Fonts.fontSFUI32.drawString(String.valueOf(stack.getMaxDamage() - stack.getItemDamage()), x + percentageXOffset, y + 15.0f + Fonts.fontSFUI32.getHeight() + percentageYOffset, color);
            } else if (showAttributes.get().equals("Percentage")) {
                // Exibe a porcentagem
                float percentageXOffset = (float) percentageX.get();
                float percentageYOffset = (float) percentageY.get();
                float percentage = (float) (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage() * 100.0f;
                String percentageText;
                if (minimalMode.get()) {
                    // Display percentage with two decimal places
                    percentageText = String.format("%.2f%%", percentage);
                } else {
                    // Display percentage with whole numbers only
                    percentageText = String.format("%.0f%%", percentage);
                }
                Fonts.fontSFUI32.drawString(percentageText, x + percentageXOffset, y + 15.0f + Fonts.fontSFUI35.getHeight() + percentageYOffset, color);
            }  else if (showAttributes.equals("All")) {
                float percentageXOffset = (float) percentageX.get();
                float percentageYOffset = (float) percentageY.get();
                int value = stack.getMaxDamage() - stack.getItemDamage();
                float percentage = (float) value / (float) stack.getMaxDamage() * 100.0f;
                String damageText = String.format("%d/%d (%.0f%%)", value, stack.getMaxDamage(), percentage);
                Fonts.fontSFUI35.drawString(damageText, x + percentageXOffset, y + 15.0f + Fonts.fontSFUI35.getHeight() + percentageYOffset, color);
            }

            if (enchantValue.get()) {
                RenderUtils.drawExhiEnchants(stack, x, y);
            }
            GlStateManager.popMatrix();

            x += 18;
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
    }

    private void drawBackgroundWithContourVertical(int x, int y, int color) {
        Color color1 = new Color(r.get(), g.get(), b.get(), a.get());
        Color color2 = new Color(r.get(), g.get(), b.get(), a.get());
        Color color3 = new Color(r2.get(), g2.get(), b2.get(), a2.get());
        Color color4 = new Color(r2.get(), g2.get(), b2.get(), a2.get());

        RoundedUtil.drawGradientRound(x - 2.0f, y - 12.0f, 18.0f, 72.0f, ra.get(), ColorUtils.applyOpacity(color4, 0.85f), color1, color3, color2);

        Fonts.fontSFUI32.drawString("Armor", x, y - 8.0f, color);
    }


    private void drawArmorItemsVertical(int x, int y, int color, EntityPlayerSP player) {
        RenderItem renderItem = mc.getRenderItem();

        for (int i = 3; i >= 0; --i) {
            ItemStack stack = player.inventory.armorInventory[i];
            if (stack == null) continue;

            renderItem.renderItemIntoGUI(stack, x, y);
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y);

            GlStateManager.pushMatrix();
            if (showAttributes.get().equals("Value")) {
                // Exibe o valor numérico
                float percentageXOffset = (float) percentageX.get();
                float percentageYOffset = (float) percentageY.get();
                Fonts.fontSFUI32.drawString(String.valueOf(stack.getMaxDamage() - stack.getItemDamage()), x + percentageXOffset, y + 15.0f + Fonts.fontSFUI32.getHeight() + percentageYOffset, color);
            } else if (showAttributes.get().equals("Percentage")) {
                // Exibe a porcentagem
                float percentageXOffset = (float) percentageX.get();
                float percentageYOffset = (float) percentageY.get();
                float percentage = (float) (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage() * 100.0f;
                String percentageText;
                if (minimalMode.get()) {
                    // Display percentage with two decimal places
                    percentageText = String.format("%.2f%%", percentage);
                } else {
                    // Display percentage with whole numbers only
                    percentageText = String.format("%.0f%%", percentage);
                }
                Fonts.fontSFUI32.drawString(percentageText, x + percentageXOffset, y + 15.0f + Fonts.fontSFUI32.getHeight() + percentageYOffset, color);
            }  else if (showAttributes.equals("All")) {
                // Exibe número e porcentagem juntos
                float percentageXOffset = (float) percentageX.get();
                float percentageYOffset = (float) percentageY.get();
                int value = stack.getMaxDamage() - stack.getItemDamage();
                float percentage = (float) value / (float) stack.getMaxDamage() * 100.0f;
                String damageText = String.format("%d/%d (%.0f%%)", value, stack.getMaxDamage(), percentage);
                Fonts.fontSFUI32.drawString(damageText, x + percentageXOffset, y + 15.0f + Fonts.fontSFUI32.getHeight() + percentageYOffset, color);
            }

            if (enchantValue.get()) {
                RenderUtils.drawExhiEnchants(stack, x, y);
            }
            GlStateManager.popMatrix();

            y += 18;
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
    }
}