/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    @Shadow
    public Minecraft mc;

    @Shadow
    protected List<GuiButton> buttonList;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected FontRenderer fontRendererObj;

    @Shadow
    public abstract void handleComponentHover(IChatComponent component, int x, int y);

    @Shadow
    protected abstract void drawHoveringText(List<String> textLines, int x, int y);

    @Shadow
    protected abstract void actionPerformed(GuiButton p_actionPerformed_1_);

    @Inject(method = "drawWorldBackground", at = @At("HEAD"), cancellable = true)
    private void drawWorldBackground(final CallbackInfo callbackInfo) {
        if (!fDPClient$shouldRenderBackground()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_,final CallbackInfo callbackInfo) {
    }

    @ModifyVariable(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), argsOnly = true)
    private String sendChatMessage(String p_sendChatMessage_1_){
        if(p_sendChatMessage_1_.length()>100){
            return p_sendChatMessage_1_.substring(0,100);
        }
        return p_sendChatMessage_1_;
    }

    @Inject(method = "drawDefaultBackground", at = @At("HEAD"), cancellable = true)
    private void drawDefaultBackground(final CallbackInfo callbackInfo){
        if(mc.currentScreen instanceof GuiContainer){
            callbackInfo.cancel();
        }
    }

    /**
     * @author CCBlueX
     */
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawClientBackground(final CallbackInfo callbackInfo) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();

        RenderUtils.drawImage(
                new ResourceLocation("fdpclient/gui/design/background.png"), 0, 0,
                width, height
        );

        callbackInfo.cancel();
    }

    @Inject(method = "handleComponentHover", at = @At("HEAD"))
    private void handleHoverOverComponent(IChatComponent component, int x, int y, final CallbackInfo callbackInfo) {
        if (component == null || component.getChatStyle().getChatClickEvent() == null)
            return;

        final ChatStyle chatStyle = component.getChatStyle();

        final ClickEvent clickEvent = chatStyle.getChatClickEvent();
        final HoverEvent hoverEvent = chatStyle.getChatHoverEvent();

        drawHoveringText(Collections.singletonList("§c§l" + clickEvent.getAction().getCanonicalName().toUpperCase() + ": §a" + clickEvent.getValue()), x, y - (hoverEvent != null ? 17 : 0));
    }

    @Inject(method = "drawHoveringText*", at = @At("HEAD"))
    private void drawHoveringText(CallbackInfo ci) {
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    /**
     * Inject action performed.
     *
     * @param button       the button
     * @param callbackInfo the callback info
     */
    @Inject(method = "actionPerformed", at = @At("RETURN"))
    protected void injectActionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        this.fDPClient$injectedActionPerformed(button);
    }


    /**
     * Should render background boolean.
     *
     * @return the boolean
     */
    @Unique
    protected boolean fDPClient$shouldRenderBackground() {
        return true;
    }

    /**
     * Injected action performed.
     *
     * @param button the button
     */
    @Unique
    protected void fDPClient$injectedActionPerformed(GuiButton button) {

    }
}