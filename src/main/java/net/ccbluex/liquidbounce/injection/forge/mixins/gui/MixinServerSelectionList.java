/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ServerListEntryLanDetected;
import net.minecraft.client.gui.ServerSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerSelectionList.class)
public abstract class MixinServerSelectionList extends GuiSlot {

    public MixinServerSelectionList(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
    }

    @Shadow @Final private List<ServerListEntryLanDetected> serverListLan;
    @Shadow @Final private GuiListExtended.IGuiListEntry lanScanEntry;

    /**
     * @author CCBlueX
     */
    @Overwrite
    protected int getScrollBarX() {
        return this.width - 5;
    }

    @Inject(
        method = "getListEntry",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/ServerSelectionList;serverListLan:Ljava/util/List;"),
        cancellable = true
    )
    private void resolveIndexError(int index, CallbackInfoReturnable<GuiListExtended.IGuiListEntry> cir) {
        if (index >= this.serverListLan.size())
            cir.setReturnValue(this.lanScanEntry);
    }
}
