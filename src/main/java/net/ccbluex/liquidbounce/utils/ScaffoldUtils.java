package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.GLUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.awt.*;

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

    public static void drawTip(int slot){
        GlStateManager.pushMatrix();

        final String info = "Blocks > " + getBlocksAmount();
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int width=scaledResolution.getScaledWidth();
        final int height=scaledResolution.getScaledHeight();

        ItemStack stack=mc.thePlayer.inventory.getStackInSlot(slot);
        GLUtils.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemIntoGUI((stack==null||!(stack.getItem() instanceof ItemBlock))?new ItemStack(Item.getItemById(166),0,0):stack
                , width / 2 - Fonts.font40.getStringWidth(info), (int) (height * 0.6 - Fonts.font40.FONT_HEIGHT * 0.5));
        GLUtils.disableStandardItemLighting();

        Fonts.font40.drawCenteredString(info, width/2F, height*0.6F, Color.WHITE.getRGB(),false);

        GlStateManager.popMatrix();
    }
}
