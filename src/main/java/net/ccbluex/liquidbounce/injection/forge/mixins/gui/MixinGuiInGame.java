/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.SnakeGame;
import net.ccbluex.liquidbounce.features.module.modules.visual.AntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule;
import net.ccbluex.liquidbounce.features.module.modules.visual.SilentHotbarModule;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.utils.client.ClassUtils;
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar;
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils;
import net.ccbluex.liquidbounce.utils.render.ColorSettingsKt;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientShader;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

@Mixin(GuiIngame.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiInGame extends Gui {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Shadow
    @Final
    protected Minecraft mc;

    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true)
    private void showCrosshair(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final HUDModule hud = HUDModule.INSTANCE;
        final SnakeGame snakeGame = SnakeGame.INSTANCE;

        if (snakeGame.getState() || hud.handleEvents() && hud.getCsgoCrosshairValue() || mc.gameSettings.thirdPersonView != 0)
            callbackInfoReturnable.setReturnValue(false);
    }

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(CallbackInfo callbackInfo) {
        if (HUDModule.INSTANCE.handleEvents())
            callbackInfo.cancel();
    }

    @Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack hookSilentHotbarHighlightedName(InventoryPlayer instance) {
        SilentHotbarModule module = SilentHotbarModule.INSTANCE;

        int slot = SilentHotbar.INSTANCE.renderSlot(module.handleEvents() && module.getKeepHighlightedName());

        return instance.getStackInSlot(slot);
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void injectCustomHotbar(ScaledResolution resolution, float delta, CallbackInfo ci) {
        final HUDModule hud = HUDModule.INSTANCE;
        final RenderUtils render = RenderUtils.INSTANCE;

        if (mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) mc.getRenderViewEntity();
            float slot = entityPlayer.inventory.currentItem;

            if (hud.handleEvents() && hud.getCustomHotbar()) {
                if (hud.getSmoothHotbarSlot()) {
                    slot = InventoryUtils.INSTANCE.getLerpedSlot();
                }

                int middleScreen = resolution.getScaledWidth() / 2;
                int height = resolution.getScaledHeight() - 1;

                float gradientOffset = (System.currentTimeMillis() % 10000) / 10000f;

                float gradientX = (hud.getGradientX() == 0f) ? 0f : 1f / hud.getGradientX();
                float gradientY = (hud.getGradientY() == 0f) ? 0f : 1f / hud.getGradientY();

                float rainbowOffset = (System.currentTimeMillis() % 10000) / 10000f;
                float rainbowX = (hud.getRainbowX() == 0f) ? 0f : 1f / hud.getRainbowX();
                float rainbowY = (hud.getRainbowY() == 0f) ? 0f : 1f / hud.getRainbowY();

                List<float[]> gradientColors = ColorSettingsKt.toColorArray(hud.getBgGradColors(), hud.getMaxHotbarGradientColors());

                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_DEPTH_TEST);

                boolean isGradient = hud.getHotbarMode().equals("Gradient");
                boolean isRainbow = hud.getHotbarMode().equals("Rainbow");

                AWTFontRenderer.Companion.setAssumeNonVolatile(true);

                if (isGradient) {
                    GradientShader.begin(
                            true,
                            gradientX,
                            gradientY,
                            gradientColors,
                            hud.getGradientHotbarSpeed(),
                            gradientOffset
                    );
                }

                if (isRainbow) {
                    RainbowShader.begin(true, rainbowX, rainbowY, rainbowOffset);
                }

                // Inner - Background
                render.drawRoundedRectInt(
                        middleScreen - 91, height - 22,
                        middleScreen + 91, height,
                        hud.getHbBackgroundColors().color().getRGB(),
                        hud.getRoundedHotbarRadius(),
                        RenderUtils.RoundedCorners.ALL
                );

                if (isRainbow) {
                    RainbowShader.INSTANCE.stopShader();
                }
                if (isGradient) {
                    GradientShader.INSTANCE.stopShader();
                }

                // Inner - Highlight
                render.drawRoundedRect(
                        middleScreen - 91 - 1 + slot * 20 + 1, height - 22,
                        middleScreen - 91 - 1 + slot * 20 + 23, height - 23 - 1 + 24,
                        hud.getHbHighlightColors().color().getRGB(),
                        hud.getRoundedHotbarRadius(),
                        RenderUtils.RoundedCorners.ALL
                );

                // Border - Background
                render.drawRoundedBorder(
                        middleScreen - 91, height - 21.55F,
                        middleScreen + 91 + 0.1F, height - 0.5F,
                        hud.getHbBackgroundBorder(),
                        hud.getHbBackgroundBorderColors().color().getRGB(),
                        hud.getRoundedHotbarRadius()
                );

                // Border - Highlight
                render.drawRoundedBorder(
                        middleScreen - 91 - 1 + slot * 20 + 1, height - 21.5F,
                        middleScreen - 91 - 1 + slot * 20 + 23.15F, height - 23 - 1 + 23.5F,
                        hud.getHbHighlightBorder(),
                        hud.getHbHighlightBorderColors().color().getRGB(),
                        hud.getRoundedHotbarRadius()
                );

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();

                enableRescaleNormal();
                glEnable(GL_BLEND);
                tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();

                for (int j = 0; j < 9; ++j) {
                    int l = height - 16 - 3;
                    int k = middleScreen - 90 + j * 20 + 2;
                    renderHotbarItem(j, k, l, delta, entityPlayer);
                }

                RenderHelper.disableStandardItemLighting();
                disableRescaleNormal();
                disableBlend();

                AWTFontRenderer.Companion.setAssumeNonVolatile(false);

                ci.cancel();
            }
        }
        liquidBounce$injectRender2DEvent(delta);
    }

    @Inject(method = "renderTooltip", at = @At("RETURN"))
    private void renderTooltipPost(ScaledResolution sr, float delta, CallbackInfo callbackInfo) {
        liquidBounce$injectRender2DEvent(delta);
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPumpkinOverlay(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = AntiBlind.INSTANCE;

        if (antiBlind.handleEvents() && antiBlind.getPumpkinEffect())
            callbackInfo.cancel();
    }

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = AntiBlind.INSTANCE;

        if (antiBlind.handleEvents() && antiBlind.getBossHealth())
            callbackInfo.cancel();
    }

    @Unique
    private void liquidBounce$injectRender2DEvent(float delta) {
        if (!ClassUtils.INSTANCE.hasClass("net.labymod.api.LabyModAPI")) {
            EventManager.INSTANCE.call(new Render2DEvent(delta));
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
