package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.client.button.AbstractButtonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiButtonExt.class)
public abstract class MixinGuiButtonExt extends MixinGuiButton {

    private AbstractButtonRenderer buttonRenderer = LiquidBounce.moduleManager.getModule(HUD.class).getButtonRenderer((GuiButton)(Object)this);

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition &&
                    mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);

            buttonRenderer.render(mouseX, mouseY, mc);

            mc.getTextureManager().bindTexture(buttonTextures);
            mouseDragged(mc, mouseX, mouseY);

            buttonRenderer.drawButtonText(mc);
            GlStateManager.resetColor();
        }
    }
}