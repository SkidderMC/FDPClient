/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule;
import net.ccbluex.liquidbounce.features.module.modules.client.HotbarSettings;
import net.ccbluex.liquidbounce.features.module.modules.visual.VanillaTweaks;
import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;

@Mixin(GuiIngame.class)
public abstract class MixinGuiInGame extends MixinGui {

    /**
     * The constant widgetsTexPath.
     */
    @Shadow
    @Final
    protected static ResourceLocation widgetsTexPath;
    /**
     * The Overlay player list.
     */
    @Final
    @Shadow
    public GuiPlayerTabOverlay overlayPlayerList;

    /**
     * Render hotbar item.
     *
     * @param index        the index
     * @param xPos         the x pos
     * @param yPos         the y pos
     * @param partialTicks the partial ticks
     * @param player       the player
     */
    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(CallbackInfo callbackInfo) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(HUDModule.class)).getState())
            callbackInfo.cancel();
    }

    /**
     * @author liulihaocai
     * @reason Render Tool Tip
     */
    @Overwrite
    protected void renderTooltip(ScaledResolution sr, float partialTicks) {
        final EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();

        float tabHope = mc.gameSettings.keyBindPlayerList.isKeyDown() ? 1f : 0f;
        final Animations animations = Animations.INSTANCE;
        if(animations.getTabHopePercent() != tabHope) {
            animations.setLastTabSync(System.currentTimeMillis());
            animations.setTabHopePercent(tabHope);
        }
        if(animations.getTabPercent() > 0 && tabHope == 0) {
            overlayPlayerList.renderPlayerlist(sr.getScaledWidth(), mc.theWorld.getScoreboard(), mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(0));
        }

        if(MinecraftInstance.mc.getRenderViewEntity() instanceof EntityPlayer) {
            String hotbarType = Objects.requireNonNull(Objects.requireNonNull(FDPClient.moduleManager.getModule(HotbarSettings.class)).getHotbarValue().get());
            Minecraft mc = Minecraft.getMinecraft();
            GlStateManager.resetColor();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(widgetsTexPath);
            float f = this.zLevel;
            this.zLevel = -90.0F;
            GlStateManager.resetColor();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            if (hotbarType.equals("Minecraft")) {
                this.drawTexturedModalRect( (float) sr.getScaledWidth() / 2 - 91, sr.getScaledHeight() - 22, 0, 0, 182, 22);
                this.drawTexturedModalRect((((float) sr.getScaledWidth() / 2) - 91 + net.ccbluex.liquidbounce.features.module.modules.client.HotbarSettings.INSTANCE.getHotbarEasePos(entityplayer.inventory.currentItem * 20)) - 1, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
            }
            this.zLevel = f;
            RenderHelper.enableGUIStandardItemLighting();
            if(hotbarType.equals("Minecraft")){
                for (int j = 0; j < 9; ++j) {
                    this.renderHotbarItem(j, sr.getScaledWidth() / 2 - 90 + j * 20 + 2, sr.getScaledHeight() - 19, partialTicks, entityplayer);
                }
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
        FDPClient.eventManager.callEvent(new Render2DEvent(partialTicks, StaticStorage.scaledResolution));
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPumpkinOverlay(final CallbackInfo callbackInfo) {

        if(Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getState() && Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getPumpkinEffectValue().get())
            callbackInfo.cancel();
    }

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo callbackInfo) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getState() && Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getBossHealthValue().get())
            callbackInfo.cancel();
    }

    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true)
    private void injectCrosshair(CallbackInfoReturnable<Boolean> cir) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(HUDModule.class)).getState()) {
            if (Objects.requireNonNull(FDPClient.moduleManager.getModule(HUDModule.class)).getCrossHairValue().get()
                || mc.gameSettings.thirdPersonView != 0 && Objects.requireNonNull(FDPClient.moduleManager.getModule(HUDModule.class)).getNof5crossHair().get())
                cir.setReturnValue(false);
        }
    }
 }
