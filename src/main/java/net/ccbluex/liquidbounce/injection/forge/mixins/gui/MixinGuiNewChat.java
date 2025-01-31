/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
    public abstract int getLineCount();

    @Shadow
    public abstract boolean getChatOpen();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract int getChatWidth();

    /**
     * Redirects access to the FONT_HEIGHT field to allow customization of the font renderer.
     */
    @Redirect(method = {"getChatComponent", "drawChat"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/FontRenderer;FONT_HEIGHT:I"))
    private int injectFontChat(FontRenderer instance) {
        return ChatControl.INSTANCE.shouldModifyChatFont() ? Fonts.fontSemibold40.getHeight() : instance.FONT_HEIGHT;
    }

    /**
     * Redirects the drawStringWithShadow method to use a custom font if necessary.
     */
    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int injectFontChatB(FontRenderer instance, String text, float x, float y, int color) {
        return ChatControl.INSTANCE.shouldModifyChatFont() ? Fonts.fontSemibold40.drawStringWithShadow(text, x, y, color) : instance.drawStringWithShadow(text, x, y, color);
    }

    /**
     * Redirects the getStringWidth method to use the custom font's string width if necessary.
     */
    @Redirect(method = "getChatComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I"))
    private int injectFontChatC(FontRenderer instance, String text) {
        return ChatControl.INSTANCE.shouldModifyChatFont() ? Fonts.fontSemibold40.getStringWidth(text) : instance.getStringWidth(text);
    }

    /**
     * Injection to customize chat rendering, maintaining all functionalities from the old code.
     */
    @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
    private void drawChat(int renderPartialTicks, final CallbackInfo callbackInfo) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if (chatControl.shouldModifyChatFont()) { 
            callbackInfo.cancel();
            if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
                int lineCount = getLineCount();
                boolean isChatOpen = getChatOpen();
                int renderedLines = 0;
                int totalDrawn = drawnChatLines.size();
                float chatOpacity = mc.gameSettings.chatOpacity * 0.9F + 0.1F;
                if (totalDrawn > 0) {
                    float chatScale = getChatScale();
                    int chatWidth = MathHelper.ceiling_float_int((float) getChatWidth() / chatScale);
                    pushMatrix();
                    translate(2f, 20f, 0f);
                    scale(chatScale, chatScale, 1f);

                    for (int i = 0; i + scrollPos < drawnChatLines.size() && i < lineCount; ++i) {
                        ChatLine chatLine = drawnChatLines.get(i + scrollPos);
                        if (chatLine != null) {
                            int time = renderPartialTicks - chatLine.getUpdatedCounter();
                            if (time < 200 || isChatOpen) {
                                double opacityFactor = (double) time / 200;
                                opacityFactor = 1 - opacityFactor;
                                opacityFactor *= 10;
                                opacityFactor = MathHelper.clamp_double(opacityFactor, 0, 1);
                                opacityFactor *= opacityFactor;
                                int alpha = (int) (255 * opacityFactor);
                                if (isChatOpen) {
                                    alpha = 255;
                                }

                                alpha = (int) ((float) alpha * chatOpacity);
                                renderedLines++;
                                if (alpha > 3) {
                                    int x = 0;
                                    int y = -i * 9;
                                    Gui.drawRect(x, y - 9, x + chatWidth + 4, y, (alpha / 2) << 24);
                                    String text = chatLine.getChatComponent().getFormattedText();
                                    Fonts.fontSemibold40.drawStringWithShadow(text, x + 2, y - 8, 0xFFFFFF + (alpha << 24));
                                    glColor4f(1, 1, 1, 1);
                                    resetColor();
                                }
                            }
                        }
                    }

                    if (isChatOpen) {
                        int fontHeight = Fonts.fontSemibold40.getFontHeight();
                        translate(-3f, 0f, 0f);
                        int totalHeight = totalDrawn * fontHeight + totalDrawn;
                        int renderedHeight = renderedLines * fontHeight + renderedLines;
                        int scrollHeight = scrollPos * renderedHeight / totalDrawn;
                        int scrollbarHeight = renderedHeight * renderedHeight / totalHeight;
                        if (totalHeight != renderedHeight) {
                            int scrollbarAlpha = scrollHeight > 0 ? 170 : 96;
                            int scrollbarColor = isScrolled ? 13382451 : 3355562;
                            Gui.drawRect(0, -scrollHeight, 2, -scrollHeight - scrollbarHeight, scrollbarColor + (scrollbarAlpha << 24));
                            Gui.drawRect(2, -scrollHeight, 1, -scrollHeight - scrollbarHeight, 13421772 + (scrollbarAlpha << 24));
                        }
                    }

                    popMatrix();
                }
            }
        }
    }

    /**
     * Modifies the message limit constant in the setChatLine method based on ChatControl.
     */
    @ModifyConstant(method = "setChatLine", constant = @Constant(intValue = 100))
    private int fixMsgLimit(int constant) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if (chatControl.handleEvents() && chatControl.getChatClearValue()) {
            return 114514; // Adjust this value as needed
        } else {
            return 100;
        }
    }

    /**
     * Injection to handle chat component interactions, maintaining all functionalities from the old code.
     */
    @Inject(method = "getChatComponent", at = @At("HEAD"), cancellable = true)
    private void getChatComponent(int mouseX, int mouseY, final CallbackInfoReturnable<IChatComponent> callbackInfo) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if (chatControl.shouldModifyChatFont()) {
            if (getChatOpen()) {
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                int scaleFactor = scaledResolution.getScaleFactor();
                float chatScale = getChatScale();
                int adjustedMouseX = mouseX / scaleFactor - 3;
                int adjustedMouseY = mouseY / scaleFactor - 27;
                adjustedMouseX = MathHelper.floor_float((float) adjustedMouseX / chatScale);
                adjustedMouseY = MathHelper.floor_float((float) adjustedMouseY / chatScale);
                if (adjustedMouseX >= 0 && adjustedMouseY >= 0) {
                    int maxLines = Math.min(getLineCount(), drawnChatLines.size());
                    if (adjustedMouseX <= MathHelper.floor_float((float) getChatWidth() / chatScale) && adjustedMouseY < Fonts.fontSemibold40.getFontHeight() * maxLines + maxLines) {
                        int lineIndex = adjustedMouseY / Fonts.fontSemibold40.getFontHeight() + scrollPos;
                        if (lineIndex >= 0 && lineIndex < drawnChatLines.size()) {
                            ChatLine chatLine = drawnChatLines.get(lineIndex);
                            int currentWidth = 0;

                            for (IChatComponent chatComponent : chatLine.getChatComponent()) {
                                if (chatComponent instanceof ChatComponentText) {
                                    currentWidth += Fonts.fontSemibold40.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) chatComponent).getChatComponentText_TextValue(), false));
                                    if (currentWidth > adjustedMouseX) {
                                        callbackInfo.setReturnValue(chatComponent);
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
