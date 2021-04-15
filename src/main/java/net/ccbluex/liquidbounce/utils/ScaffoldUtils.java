package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class ScaffoldUtils extends MinecraftInstance {
    public static int getBlocksAmount() {
        int amount = 0;

        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock
                    && !InventoryUtils.BLOCK_BLACKLIST.contains(((ItemBlock) itemStack.getItem()).getBlock()))
                amount += itemStack.stackSize;
        }

        return amount;
    }

    public static void drawTip(){
        GlStateManager.pushMatrix();

        final String info = "Blocks: " + getBlocksAmount();
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int width=scaledResolution.getScaledWidth();
        final int height=scaledResolution.getScaledHeight();
        final int fWidth= Fonts.font40.getStringWidth(info)/2;
        final int fHeight= (int) (Fonts.font40.FONT_HEIGHT*1.2F);
        RenderUtils.drawRect(width/2-fWidth,(int)(height*0.7-fHeight),width/2+fWidth, (int) (height*0.7+fHeight), Color.GRAY.getRGB());
        RenderUtils.drawFilledCircle(width/2-fWidth, (int) (height*0.7),fHeight,Color.GRAY);
        RenderUtils.drawFilledCircle(width/2+fWidth, (int) (height*0.7),fHeight,Color.GRAY);
        Fonts.font40.drawCenteredString(info, width/2F, height*0.7F-1, Color.WHITE.getRGB(),false);

        GlStateManager.popMatrix();
    }
}
