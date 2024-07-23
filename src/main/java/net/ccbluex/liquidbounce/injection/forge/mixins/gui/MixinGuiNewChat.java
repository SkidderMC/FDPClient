/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.client.ChatControl;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.glColor4f;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    @Final
    private List<ChatLine> drawnChatLines;
    @Shadow
    private int scrollPos;
    @Shadow
    private boolean isScrolled;
    @Shadow
    @Final
    private List<ChatLine> chatLines;

    @Shadow
    public abstract int getLineCount();

    @Shadow
    public abstract boolean getChatOpen();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract int getChatWidth();

    @Shadow
    public abstract void deleteChatLine(int p_deleteChatLine_1_);

    @Shadow
    public abstract void scroll(int p_scroll_1_);

    @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
    private void drawChat(int p_drawChat_1_, final CallbackInfo callbackInfo) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if (chatControl.handleEvents() && chatControl.getFontChatValue()) {
            callbackInfo.cancel();
            if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
                int lvt_2_1_ = getLineCount();
                boolean lvt_3_1_ = false;
                int lvt_4_1_ = 0;
                int lvt_5_1_ = drawnChatLines.size();
                float lvt_6_1_ = mc.gameSettings.chatOpacity * 0.9F + 0.1F;
                if (lvt_5_1_ > 0) {
                    if (getChatOpen()) {
                        lvt_3_1_ = true;
                    }

                    float lvt_7_1_ = getChatScale();
                    int lvt_8_1_ = MathHelper.ceiling_float_int((float) getChatWidth() / lvt_7_1_);
                    pushMatrix();
                    translate(2f, 20f, 0f);
                    scale(lvt_7_1_, lvt_7_1_, 1f);

                    int lvt_9_1_;
                    int lvt_11_1_;
                    int lvt_14_1_;
                    for (lvt_9_1_ = 0; lvt_9_1_ + scrollPos < drawnChatLines.size() && lvt_9_1_ < lvt_2_1_; ++lvt_9_1_) {
                        ChatLine lvt_10_1_ = drawnChatLines.get(lvt_9_1_ + scrollPos);
                        if (lvt_10_1_ != null) {
                            lvt_11_1_ = p_drawChat_1_ - lvt_10_1_.getUpdatedCounter();
                            if (lvt_11_1_ < 200 || lvt_3_1_) {
                                double lvt_12_1_ = (double) lvt_11_1_ / 200;
                                lvt_12_1_ = 1 - lvt_12_1_;
                                lvt_12_1_ *= 10;
                                lvt_12_1_ = MathHelper.clamp_double(lvt_12_1_, 0, 1);
                                lvt_12_1_ *= lvt_12_1_;
                                lvt_14_1_ = (int) (255 * lvt_12_1_);
                                if (lvt_3_1_) {
                                    lvt_14_1_ = 255;
                                }

                                lvt_14_1_ = (int) ((float) lvt_14_1_ * lvt_6_1_);
                                ++lvt_4_1_;
                                if (lvt_14_1_ > 3) {
                                    int lvt_15_1_ = 0;
                                    int lvt_16_1_ = -lvt_9_1_ * 9;
                                    Gui.drawRect(lvt_15_1_, lvt_16_1_ - 9, lvt_15_1_ + lvt_8_1_ + 4, lvt_16_1_, lvt_14_1_ / 2 << 24);
                                    String lvt_17_1_ = lvt_10_1_.getChatComponent().getFormattedText();
                                    Fonts.font40.drawStringWithShadow(lvt_17_1_, lvt_15_1_ + 2, (lvt_16_1_ - 8), 16777215 + (lvt_14_1_ << 24));
                                    glColor4f(1, 1, 1, 1);
                                    resetColor();
                                }
                            }
                        }
                    }

                    if (lvt_3_1_) {
                        lvt_9_1_ = Fonts.font40.getFontHeight();
                        translate(-3f, 0f, 0f);
                        int lvt_10_2_ = lvt_5_1_ * lvt_9_1_ + lvt_5_1_;
                        lvt_11_1_ = lvt_4_1_ * lvt_9_1_ + lvt_4_1_;
                        int lvt_12_2_ = scrollPos * lvt_11_1_ / lvt_5_1_;
                        int lvt_13_1_ = lvt_11_1_ * lvt_11_1_ / lvt_10_2_;
                        if (lvt_10_2_ != lvt_11_1_) {
                            lvt_14_1_ = lvt_12_2_ > 0 ? 170 : 96;
                            int lvt_15_2_ = isScrolled ? 13382451 : 3355562;
                            Gui.drawRect(0, -lvt_12_2_, 2, -lvt_12_2_ - lvt_13_1_, lvt_15_2_ + (lvt_14_1_ << 24));
                            Gui.drawRect(2, -lvt_12_2_, 1, -lvt_12_2_ - lvt_13_1_, 13421772 + (lvt_14_1_ << 24));
                        }
                    }

                    popMatrix();
                }
            }
        }
    }

    @ModifyConstant(method = "setChatLine", constant = @Constant(intValue = 100))
    private int fixMsgLimit(int constant) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if(chatControl.handleEvents() && chatControl.getChatClearValue()) {
            return 114514;
        } else {
            return 100;
        }
    }

    @Inject(method = "getChatComponent", at = @At("HEAD"), cancellable = true)
    private void getChatComponent(int p_getChatComponent_1_, int p_getChatComponent_2_, final CallbackInfoReturnable<IChatComponent> callbackInfo) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if (chatControl.handleEvents() && chatControl.getFontChatValue()) {
            if (getChatOpen()) {
                ScaledResolution lvt_3_1_ = new ScaledResolution(mc);
                int lvt_4_1_ = lvt_3_1_.getScaleFactor();
                float lvt_5_1_ = getChatScale();
                int lvt_6_1_ = p_getChatComponent_1_ / lvt_4_1_ - 3;
                int lvt_7_1_ = p_getChatComponent_2_ / lvt_4_1_ - 27;
                lvt_6_1_ = MathHelper.floor_float((float) lvt_6_1_ / lvt_5_1_);
                lvt_7_1_ = MathHelper.floor_float((float) lvt_7_1_ / lvt_5_1_);
                if (lvt_6_1_ >= 0 && lvt_7_1_ >= 0) {
                    int lvt_8_1_ = Math.min(getLineCount(), drawnChatLines.size());
                    if (lvt_6_1_ <= MathHelper.floor_float((float) getChatWidth() / getChatScale()) && lvt_7_1_ < Fonts.font40.getFontHeight() * lvt_8_1_ + lvt_8_1_) {
                        int lvt_9_1_ = lvt_7_1_ / Fonts.font40.getFontHeight() + scrollPos;
                        if (lvt_9_1_ >= 0 && lvt_9_1_ < drawnChatLines.size()) {
                            ChatLine lvt_10_1_ = drawnChatLines.get(lvt_9_1_);
                            int lvt_11_1_ = 0;

                            for (IChatComponent lvt_13_1_ : lvt_10_1_.getChatComponent()) {
                                if (lvt_13_1_ instanceof ChatComponentText) {
                                    lvt_11_1_ += Fonts.font40.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) lvt_13_1_).getChatComponentText_TextValue(), false));
                                    if (lvt_11_1_ > lvt_6_1_) {
                                        callbackInfo.setReturnValue(lvt_13_1_);
                                        return;
                                    }
                                }
                            }
                        }

                    }
                }

            }

            callbackInfo.setReturnValue(null);
        }
    }
}
