/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.special.GradientBackground;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.utils.particles.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
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
    public abstract void updateScreen();

    @Shadow
    public abstract void handleComponentHover(IChatComponent component, int x, int y);

    @Shadow
    protected abstract void drawHoveringText(List<String> textLines, int x, int y);

    @Shadow
    protected abstract void actionPerformed(GuiButton p_actionPerformed_1_);

    @Inject(method = "drawWorldBackground", at = @At("HEAD"))
    private void drawWorldBackground(final CallbackInfo callbackInfo) {
        try {
            final HUD hud = LiquidBounce.moduleManager.getModule(HUD.class);
            if (hud.getInventoryParticle().get() && mc.thePlayer != null) {
                ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
            }

            if(mc.thePlayer != null) {
                int defaultHeight1 = (this.height);
                int defaultWidth1 = (this.width);
                GL11.glPushMatrix();
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                FontLoaders.F30.DisplayFont2(FontLoaders.F30, LiquidBounce.CLIENT_NAME,defaultWidth1 - 12f - FontLoaders.F14.DisplayFontWidths(FontLoaders.F14, LiquidBounce.CLIENT_VERSION) - FontLoaders.F30.DisplayFontWidths(FontLoaders.F30, LiquidBounce.CLIENT_NAME) ,defaultHeight1 - 23.5f,new Color(255,255,255,140).getRGB(),true);
                FontLoaders.F30.DisplayFont2(FontLoaders.F14, LiquidBounce.CLIENT_VERSION,defaultWidth1 - 10f - FontLoaders.F14.DisplayFontWidths(FontLoaders.F14, LiquidBounce.CLIENT_VERSION) ,defaultHeight1 - 15f,new Color(255,255,255,140).getRGB(),true);
                GL11.glPopMatrix();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Inject(method = "drawWorldBackground", at = @At("RETURN"))
    private void drawWorldBackground2(final CallbackInfo callbackInfo) {
        try {
            if(mc.thePlayer != null) {
                int defaultHeight1 = (this.height);
                int defaultWidth1 = (this.width);
                GL11.glPushMatrix();
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                FontLoaders.F30.DisplayFont2(FontLoaders.F30, LiquidBounce.CLIENT_NAME,defaultWidth1 - 12f - FontLoaders.F14.DisplayFontWidths(FontLoaders.F14, LiquidBounce.CLIENT_VERSION) - FontLoaders.F30.DisplayFontWidths(FontLoaders.F30, LiquidBounce.CLIENT_NAME) ,defaultHeight1 - 23.5f,new Color(255,255,255,140).getRGB(),true);
                FontLoaders.F30.DisplayFont2(FontLoaders.F14, LiquidBounce.CLIENT_VERSION,defaultWidth1 - 10f - FontLoaders.F14.DisplayFontWidths(FontLoaders.F14, LiquidBounce.CLIENT_VERSION) ,defaultHeight1 - 15f,new Color(255,255,255,140).getRGB(),true);
                GL11.glPopMatrix();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_,final CallbackInfo callbackInfo) {
    }

    @ModifyVariable(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"))
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
    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void drawClientBackground(final CallbackInfo callbackInfo) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        if(GuiBackground.Companion.getEnabled()) {
            if (LiquidBounce.INSTANCE.getBackground() == null) {
                GradientBackground.INSTANCE.draw(width, height);
            }else{
                mc.getTextureManager().bindTexture(LiquidBounce.INSTANCE.getBackground());
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width, height);
            }
        
            
            GlStateManager.resetColor();
            if (GuiBackground.Companion.getParticles())
                ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
        }
    }

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void drawParticles(final CallbackInfo callbackInfo) {
        if(GuiBackground.Companion.getParticles())
            ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
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
        // idk why this make font renderer works
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    protected abstract void injectedActionPerformed(GuiButton button);
}