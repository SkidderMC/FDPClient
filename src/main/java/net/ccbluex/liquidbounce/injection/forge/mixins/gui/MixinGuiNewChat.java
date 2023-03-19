/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.client.ChatEnhance;
import net.ccbluex.liquidbounce.font.CFontRenderer;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    public abstract int getLineCount();

    @Shadow
    @Final
    private List<ChatLine> drawnChatLines;

    @Shadow
    public abstract boolean getChatOpen();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract int getChatWidth();

    @Shadow
    private int scrollPos;

    @Shadow
    private boolean isScrolled;

    @Shadow
    public abstract void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId);

    private String lastMessage;
    private int sameMessageAmount;
    private int line;

    private final HUD hud = LiquidBounce.moduleManager.getModule(HUD.class);

    private final ChatEnhance chatEnhance = LiquidBounce.moduleManager.getModule(ChatEnhance.class);

    /**
     * @author Liuli
     * @reason ChatCombine
     */
    @Overwrite
    public void printChatMessage(IChatComponent chatComponent) {
        if(!chatEnhance.getState() || !chatEnhance.getChatCombineValue().get()) {
            printChatMessageWithOptionalDeletion(chatComponent, 0);
        } else if(chatEnhance.getChatCombineValue().get()) {

            String text = fixString(chatComponent.getFormattedText());

            if (text.equals(this.lastMessage)) {
                (Minecraft.getMinecraft()).ingameGUI.getChatGUI().deleteChatLine(this.line);
                this.sameMessageAmount++;
                chatComponent.appendText(ChatFormatting.WHITE + " [" + "x" + this.sameMessageAmount + "]");
            } else {
                this.sameMessageAmount = 1;
            }

            this.lastMessage = text;
            this.line++;
            if (this.line > 256)
                this.line = 0;

            printChatMessageWithOptionalDeletion(chatComponent, this.line);
        }
    }

    @ModifyConstant(method = "setChatLine", constant = @Constant(intValue = 100))
    private int fixMsgLimit(int constant) {
        if(chatEnhance.getState() && chatEnhance.getChatClearValue().get()) {
            return 114514;
        } else {
            return 100;
        }
    }

    private String fixString(String str){
        str=str.replaceAll("\uF8FF","");//remove air chars

        StringBuilder sb=new StringBuilder();
        for(char c:str.toCharArray()){
            if((int) c > (33 + 65248) && (int) c < (128 + 65248)){
                sb.append(Character.toChars((int) c - 65248));
            }else{
                sb.append(c);
            }
        }

        return sb.toString();
    }


    /**
     * @author Liuli
     * @reason Better chat
     */
    @Overwrite
    public void drawChat(int updateCounter) {
        boolean canFont = chatEnhance.getState() && chatEnhance.getFontChatValue().get();

        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            
            int minH = 1000;
            int maxH = -1000;
            
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
            if (k > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float)this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                int i1;
                int j1;
                int l1;
                for(i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1) {
                    ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);
                    if (chatline != null) {
                        j1 = updateCounter - chatline.getUpdatedCounter();
                        if (j1 < 200 || flag) {
                            double d0 = (double)j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 *= 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 *= d0;
                            l1 = (int)(255.0D * d0);
                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int)((float)l1 * f);
                            ++j;

                            if (l1 > 3) {
                                GL11.glPushMatrix();

                                int i2 = 0;
                                int j2 = -i1 * 9;

                                if(chatEnhance.getChatAnimValue().get()&&!flag) {
                                    if (j1 <= 20) {
                                        GL11.glTranslatef((float) (-(l + 4) * EaseUtils.INSTANCE.easeInQuart(1 - ((j1+mc.timer.renderPartialTicks) / 20.0))), 0F, 0F);
                                    }
                                    if (j1 >= 180) {
                                        GL11.glTranslatef((float) (-(l + 4) * EaseUtils.INSTANCE.easeInQuart(((j1+mc.timer.renderPartialTicks) - 180) / 20.0)), 0F, 0F);
                                    }
                                }

                                if(chatEnhance.getChatRectValue().get()) {
                                    RenderUtils.drawRect(i2 - 2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
                                    if (j2 - 9 < minH) {
                                        minH = j2 - 9;
                                    }
                                    if (j2 > maxH) {
                                        maxH = j2;
                                    }
                                }
                                GlStateManager.enableBlend();
                                if(chatEnhance.getChatRectValue().get()) {
                                    if (canFont)
                                        CFontRenderer.DisplayFont(chatline.getChatComponent().getFormattedText(), (float) i2, (float) (j2 - 8), new Color(255, 255, 255).getRGB(), FontLoaders.C16);
                                    else {
                                        this.mc.fontRendererObj.drawString(chatline.getChatComponent().getFormattedText(), (float) i2, (float) (j2 - 8), 16777215 + (l1 << 24), false);
                                    }
                                }else{
                                    if (canFont)
                                        FontLoaders.C16.DisplayFont2(FontLoaders.C16,chatline.getChatComponent().getFormattedText(), (float) i2, (float) (j2 - 8), new Color(255, 255, 255).getRGB(),true);
                                    else {
                                        this.mc.fontRendererObj.drawStringWithShadow(chatline.getChatComponent().getFormattedText(), (float) i2, (float) (j2 - 8), 16777215 + (l1 << 24));
                                    }
                                }
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();

                                GL11.glPopMatrix();
                            }
                        }
                    }
                }
                
                if (chatEnhance.getBetterChatRectValue().get()) {
                    if (minH < 900) {
                        RenderUtils.drawShadow(-2f, minH, MathHelper.ceiling_float_int((float)this.getChatWidth() / f1) + 4, maxH - minH);
                    }
                }

                if (flag) {
                    i1 = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * i1 + k;
                    j1 = j * i1 + j;
                    int j3 = this.scrollPos * j1 / k;
                    int k1 = j1 * j1 / l2;
                    if (l2 != j1) {
                        l1 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        RenderUtils.drawRect(0, -j3, 2, -j3 - k1, l3 + (l1 << 24));
                        RenderUtils.drawRect(2, -j3, 1, -j3 - k1, 13421772 + (l1 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    @Inject(method = "getChatComponent", at = @At("HEAD"), cancellable = true)
    private void getChatComponent(int p_getChatComponent_1_, int p_getChatComponent_2_, final CallbackInfoReturnable<IChatComponent> callbackInfo) {
        if(chatEnhance.getState() && chatEnhance.getFontChatValue().get()) {
            if(!this.getChatOpen()) {
                callbackInfo.setReturnValue(null);
            }else{
                int lvt_4_1_ = StaticStorage.scaledResolution.getScaleFactor();
                float lvt_5_1_ = this.getChatScale();
                int lvt_6_1_ = p_getChatComponent_1_ / lvt_4_1_ - 3;
                int lvt_7_1_ = p_getChatComponent_2_ / lvt_4_1_ - 27;
                lvt_6_1_ = MathHelper.floor_float((float) lvt_6_1_ / lvt_5_1_);
                lvt_7_1_ = MathHelper.floor_float((float) lvt_7_1_ / lvt_5_1_);
                if(lvt_6_1_ >= 0 && lvt_7_1_ >= 0) {
                    int lvt_8_1_ = Math.min(this.getLineCount(), this.drawnChatLines.size());
                    if(lvt_6_1_ <= MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale()) && lvt_7_1_ < FontLoaders.C16.getHeight() * lvt_8_1_ + lvt_8_1_) {
                        int lvt_9_1_ = lvt_7_1_ / FontLoaders.C16.getHeight() + this.scrollPos;
                        if(lvt_9_1_ >= 0 && lvt_9_1_ < this.drawnChatLines.size()) {
                            ChatLine lvt_10_1_ = this.drawnChatLines.get(lvt_9_1_);
                            int lvt_11_1_ = 0;
                            Iterator lvt_12_1_ = lvt_10_1_.getChatComponent().iterator();

                            while(lvt_12_1_.hasNext()) {
                                IChatComponent lvt_13_1_ = (IChatComponent) lvt_12_1_.next();
                                if(lvt_13_1_ instanceof ChatComponentText) {
                                    lvt_11_1_ += FontLoaders.C16.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) lvt_13_1_).getChatComponentText_TextValue(), false));
                                    if(lvt_11_1_ > lvt_6_1_) {
                                        callbackInfo.setReturnValue(lvt_13_1_);
                                        return;
                                    }
                                }
                            }
                        }

                        callbackInfo.setReturnValue(null);
                    }else{
                        callbackInfo.setReturnValue(null);
                    }
                }else{
                    callbackInfo.setReturnValue(null);
                }
            }
        }
    }
}
