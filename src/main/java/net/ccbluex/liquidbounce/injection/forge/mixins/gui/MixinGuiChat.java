/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.ChatControl;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat extends MixinGuiScreen {

    @Shadow
    protected GuiTextField inputField;

    @Shadow
    private List<String> foundPlayerNames;

    @Shadow
    private boolean waitingOnAutocomplete;

    @Shadow
    public abstract void onAutocompleteResponse(String[] p_onAutocompleteResponse_1_);

    @Shadow
    private int sentHistoryCursor;

    @Shadow private String historyBuffer;

    private float yPosOfInputField;
    private float fade = 0;

    private final ChatControl chatEnhance = FDPClient.moduleManager.getModule(ChatControl.class);

    /**
     * @author Liuli
     * @reason 这种客户端验证需要玩家点击一段'.'开头的100长度字符串，而客户端会自动填充.say来尝试绕过，但是自动填充的.say在需要按上箭头重新发送上一条消息的时候就会因为长度不够导致展示不全
     */
    @Overwrite
    public void getSentHistory(int p_getSentHistory_1_) {
        int i = this.sentHistoryCursor + p_getSentHistory_1_;
        int j = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);
        if (i != this.sentHistoryCursor) {
            if (i == j) {
                this.sentHistoryCursor = j;
                setText(this.historyBuffer);
            } else {
                if (this.sentHistoryCursor == j) {
                    this.historyBuffer = this.inputField.getText();
                }

                setText(this.mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.sentHistoryCursor = i;
            }
        }
    }

    private void setText(String text){
        if(text.startsWith(String.valueOf(FDPClient.commandManager.getPrefix()))) {
            this.inputField.setMaxStringLength(114514);
        } else {
            if(chatEnhance.getState() && chatEnhance.getChatLimitValue().get()) {
                this.inputField.setMaxStringLength(114514);
            } else {
                this.inputField.setMaxStringLength(100);
            }
        }
        this.inputField.setText(text);
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void init(CallbackInfo callbackInfo) {
        inputField.yPosition = height - 5;
        yPosOfInputField = inputField.yPosition;
    }

    /**
     * only trust message in KeyTyped to anti some client click check (like old zqat.top)
     */
    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void keyTyped(char typedChar, int keyCode, CallbackInfo callbackInfo) {
        String text = inputField.getText();
        if(text.startsWith(String.valueOf(FDPClient.commandManager.getPrefix()))) {
            this.inputField.setMaxStringLength(114514);
            if (keyCode == 28 || keyCode == 156) {
                FDPClient.commandManager.executeCommands(text);
                callbackInfo.cancel();
                mc.ingameGUI.getChatGUI().addToSentMessages(text);
                if(mc.currentScreen instanceof GuiChat)
                    Minecraft.getMinecraft().displayGuiScreen(null);
            }else{
                FDPClient.commandManager.autoComplete(text);
            }
        } else {
            if(chatEnhance.getState() && chatEnhance.getChatLimitValue().get()) {
                this.inputField.setMaxStringLength(114514);
            } else {
                this.inputField.setMaxStringLength(100);
            }
        }
    }

    /**
     * bypass click command auth like kjy.pub
     */
    @Inject(method = "setText", at = @At("HEAD"), cancellable = true)
    private void setText(String newChatText, boolean shouldOverwrite, CallbackInfo callbackInfo) {
        if(shouldOverwrite&&newChatText.startsWith(String.valueOf(FDPClient.commandManager.getPrefix()))){
            setText(FDPClient.commandManager.getPrefix()+"say "+newChatText);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "updateScreen", at = @At("HEAD"))
    private void updateScreen(CallbackInfo callbackInfo) {
        final int delta = RenderUtils.deltaTime;

        if (fade < 14) fade += 0.4F * delta;
        if (fade > 14) fade = 14;

        if (yPosOfInputField > height - 12) yPosOfInputField -= 0.4F * delta;
        if (yPosOfInputField < height - 12) yPosOfInputField = height - 12;

        inputField.yPosition = (int) yPosOfInputField - 1;
    }

    @Inject(method = "autocompletePlayerNames", at = @At("HEAD"))
    private void prioritizeClientFriends(final CallbackInfo callbackInfo) {
        foundPlayerNames.sort(
                Comparator.comparing(s -> !FDPClient.fileManager.getFriendsConfig().isFriend(s)));
    }

    /**
     * Adds client command auto completion and cancels sending an auto completion request packet
     * to the server if the message contains a client command.
     *
     * @author NurMarvin
     */
    @Inject(method = "sendAutocompleteRequest", at = @At("HEAD"), cancellable = true)
    private void handleClientCommandCompletion(String full, final String ignored, CallbackInfo callbackInfo) {
        if (FDPClient.commandManager.autoComplete(full)) {
            waitingOnAutocomplete = true;

            String[] latestAutoComplete = FDPClient.commandManager.getLatestAutoComplete();

            if (full.toLowerCase().endsWith(latestAutoComplete[latestAutoComplete.length - 1].toLowerCase()))
                return;

            this.onAutocompleteResponse(latestAutoComplete);

            callbackInfo.cancel();
        }
    }

    private void onAutocompleteResponse(String[] autoCompleteResponse, CallbackInfo callbackInfo) {
        if (FDPClient.commandManager.getLatestAutoComplete().length != 0) callbackInfo.cancel();
    }
    public void draw(){
    }
    /**
     * @author CCBlueX
     */
    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    public void drawScreen(int mouseX, int mouseY, float partialTicks,CallbackInfo ci) {
        //RenderUtils.drawRect(10,10,20,20,new Color(255,255,255,255).getRGB());
        RenderUtils.drawRoundedCornerRect(1, this.height - (int) fade - 2, this.width - 4, this.height - 1 , 2f, new Color(255,255,255,50).getRGB());
        RenderUtils.drawRoundedCornerRect(2, this.height - (int) fade - 1, this.width - 3, this.height - 2 ,3f, new Color(0,0,0,200).getRGB());

        this.inputField.drawTextBox();

        if (FDPClient.commandManager.getLatestAutoComplete().length > 0 && !inputField.getText().isEmpty() && inputField.getText().startsWith(String.valueOf(FDPClient.commandManager.getPrefix()))) {
            String[] latestAutoComplete = FDPClient.commandManager.getLatestAutoComplete();
            String[] textArray = inputField.getText().split(" ");
            String text = textArray[textArray.length - 1];
            Object[] result = Arrays.stream(latestAutoComplete).filter((str) -> str.toLowerCase().startsWith(text.toLowerCase())).toArray();
            String resultText = "";
            if(result.length>0)
                resultText = ((String)result[0]).substring(Math.min(((String)result[0]).length(),text.length()));

            mc.fontRendererObj.drawStringWithShadow(resultText, 5.5F + inputField.xPosition + mc.fontRendererObj.getStringWidth(inputField.getText()), inputField.yPosition+2f, new Color(165, 165, 165).getRGB());
        }

        IChatComponent ichatcomponent =
                this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (ichatcomponent != null)
            this.handleComponentHover(ichatcomponent, mouseX, mouseY);
        ci.cancel();
    }
}