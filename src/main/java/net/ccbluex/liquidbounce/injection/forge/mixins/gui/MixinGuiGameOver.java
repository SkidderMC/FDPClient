package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.network.play.server.S07PacketRespawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGameOver.class)
public abstract class MixinGuiGameOver extends MixinGuiScreen {
    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        buttonList.add(new GuiButton(114514, width-100, height-20, 100, 20, "%ui.gameover.close%"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        switch (button.id) {
            case 114514:
                PacketUtils.INSTANCE.handlePacket(new S07PacketRespawn(mc.thePlayer.dimension,mc.theWorld.getDifficulty(),mc.theWorld.getWorldType(),mc.theWorld.getWorldInfo().getGameType()));
                break;
        }
    }
}
