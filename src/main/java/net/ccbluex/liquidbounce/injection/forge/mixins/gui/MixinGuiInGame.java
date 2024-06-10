/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import me.zywl.fdpclient.FDPClient;
import me.zywl.fdpclient.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule;
import net.ccbluex.liquidbounce.features.module.modules.client.HotbarSettings;
import net.ccbluex.liquidbounce.features.module.modules.visual.VanillaTweaks;
import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.ccbluex.liquidbounce.utils.ClassUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.SpoofItemUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
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
        HUDModule hudModule = FDPClient.moduleManager.getModule(HUDModule.class);

        if (Objects.requireNonNull(hudModule).getInventoryOnHotbar().get()){
            GlStateManager.pushMatrix();
            int scaledWidth = sr.getScaledWidth();
            int scaledHeight = sr.getScaledHeight();
            GlStateManager.translate((float) scaledWidth / 2 - 90, (float) scaledHeight - 25, 0);
            RenderUtils.drawBorderedRect(0, 1, 180, -58, 1, new Color(0,0,0,255).getRGB(), new Color(0,0,0,130).getRGB());
            RenderHelper.enableGUIStandardItemLighting();

            int initialSlot = 9;
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 9; column++) {
                    int slot = initialSlot + row * 9 + column;
                    int x = 1 + column * 20;
                    int y = -16 - row * 20;
                    renderItem(slot, x, y, mc.thePlayer);
                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }

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
            boolean spoofing = SpoofItemUtils.INSTANCE.getSpoofing();
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
                this.drawTexturedModalRect((((float) sr.getScaledWidth() / 2) - 91 + net.ccbluex.liquidbounce.features.module.modules.client.HotbarSettings.INSTANCE.getHotbarEasePos((HotbarSettings.INSTANCE.getSpoofHotbar().get() && spoofing ? entityplayer.inventory.currentItem : spoofing ? SpoofItemUtils.INSTANCE.getSlot() : entityplayer.inventory.currentItem) * 20)) - 1, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
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

        if(Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getState() && Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getAntiBlindValue().get() && Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getPumpkinEffectValue().get())
            callbackInfo.cancel();
    }

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo callbackInfo) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getState() && Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getAntiBlindValue().get() && Objects.requireNonNull(FDPClient.moduleManager.getModule(VanillaTweaks.class)).getBossHealthValue().get())
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

    @Inject(method = "renderTooltip", at = @At("RETURN"))
    private void renderTooltipPost(ScaledResolution sr, float partialTicks, CallbackInfo callbackInfo) {
        if (!ClassUtils.hasClass("net.labymod.api.LabyModAPI")) {
            FDPClient.eventManager.callEvent(new Render2DEvent(partialTicks, sr));
        }
    }

    private void renderItem(int i, int x, int y , EntityPlayer player) {
        ItemStack itemstack = player.inventory.mainInventory[i];
        if (itemstack != null) {
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, x, y);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemstack, x-1, y-1);
        }
    }
 }