/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.other.BetterInventory;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.*;

@Mixin(GuiContainer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiContainerHighlight extends MixinGuiScreen {

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void drawSlot(Slot slot, CallbackInfo ci) {
        final BetterInventory betterInventory = BetterInventory.INSTANCE;

        if (!betterInventory.handleEvents() || !betterInventory.getHighlightClicked()) {
            return;
        }

        if (slot.slotNumber != betterInventory.getClickedSlot()) {
            return;
        }

        int x = slot.xDisplayPosition;
        int y = slot.yDisplayPosition;
        int color = betterInventory.getHighlightColor().getRGB();

        glPushMatrix();
        glPushAttrib(GL_ENABLE_BIT);
        glDisable(GL_LIGHTING);

        if ("Fill".equals(betterInventory.getHighlightMode())) {
            RenderUtils.INSTANCE.drawRect(x, y, x + 16, y + 16, color);
        } else {
            RenderUtils.INSTANCE.drawBorder(x, y, x + 16, y + 16, betterInventory.getBorderWidth(), color);
        }

        glPopAttrib();
        glPopMatrix();
    }
}
