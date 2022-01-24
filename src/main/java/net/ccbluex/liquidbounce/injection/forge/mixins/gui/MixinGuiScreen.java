/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.ParticleUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    public abstract void updateScreen();

    @Shadow
    public abstract void handleComponentHover(IChatComponent component, int x, int y);

    @Shadow
    protected abstract void drawHoveringText(List<String> textLines, int x, int y);

    @Shadow
    protected abstract void actionPerformed(GuiButton p_actionPerformed_1_);

    @Inject(method = "drawWorldBackground", at = @At("HEAD"))
    private void drawWorldBackground(final CallbackInfo callbackInfo) {
        final HUD hud = LiquidBounce.moduleManager.getModule(HUD.class);

        if(hud.getInventoryParticle().get() && mc.thePlayer != null) {
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final int width = scaledResolution.getScaledWidth();
            final int height = scaledResolution.getScaledHeight();
            ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
        }
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
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawClientBackground(final CallbackInfo callbackInfo) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();

        if(GuiBackground.Companion.getEnabled()) {
            if (LiquidBounce.INSTANCE.getBackground() == null) {
                RenderUtils.glColor(ColorUtils.INSTANCE.hslRainbow(1,0.41f,0.58f,300,4000, 0.7f,1f));
                mc.getTextureManager().bindTexture(new ResourceLocation(LiquidBounce.CLIENT_NAME.toLowerCase() + "/misc/bg.png"));
            }else{
                mc.getTextureManager().bindTexture(LiquidBounce.INSTANCE.getBackground());
            }

            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width, height);

//            if (LiquidBounce.INSTANCE.getBackground() == null) {
//                BlurUtils.INSTANCE.blurAll(60f);
//            }
            GlStateManager.resetColor();

            if (GuiBackground.Companion.getParticles())
                ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
            callbackInfo.cancel();
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
}