/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.config.ColorValue;
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoArmor;
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner;
import net.ccbluex.liquidbounce.features.module.modules.other.ChestStealer;
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timing.TickTimer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@Mixin(GuiContainer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiContainer extends MixinGuiScreen {

    // Separate TickTimer instances to avoid timing conflicts
    @Unique
    final TickTimer tick0 = new TickTimer();
    @Unique
    final TickTimer tick1 = new TickTimer();
    @Unique
    final TickTimer tick2 = new TickTimer();

    @Inject(method = "initGui", at = @At("RETURN"), cancellable = true)
    private void init(CallbackInfo ci) {
        if (ChestStealer.INSTANCE.handleEvents() && ChestStealer.INSTANCE.getSilentGUI()) {
            if (mc.currentScreen instanceof GuiChest) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (ChestStealer.INSTANCE.handleEvents() && ChestStealer.INSTANCE.getSilentGUI()) {
            if (mc.currentScreen instanceof GuiChest) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void drawSlot(Slot slot, CallbackInfo ci) {
        // Instances
        final InventoryManager inventoryManager = InventoryManager.INSTANCE;
        final ChestStealer chestStealer = ChestStealer.INSTANCE;
        final InventoryCleaner inventoryCleaner = InventoryCleaner.INSTANCE;
        final AutoArmor autoArmor = AutoArmor.INSTANCE;
        final RenderUtils renderUtils = RenderUtils.INSTANCE;

        // Slot X/Y
        int x = slot.xDisplayPosition;
        int y = slot.yDisplayPosition;

        // ChestStealer Highlight Values
        int chestStealerBackgroundColor = ((ColorValue) chestStealer.getBackgroundColor()).selectedColor().getRGB();
        int chestStealerBorderColor = ((ColorValue) chestStealer.getBorderColor()).selectedColor().getRGB();

        // InvCleaner & AutoArmor Highlight Values
        int invManagerBackgroundColor = ((ColorValue) inventoryManager.getBackgroundColor()).selectedColor().getRGB();
        int invManagerBorderColor = ((ColorValue) inventoryManager.getBorderColor()).selectedColor().getRGB();

        // Get the current slot being stolen
        int currentSlotChestStealer = inventoryManager.getChestStealerCurrentSlot();
        int currentSlotInvCleaner = inventoryManager.getInvCleanerCurrentSlot();
        int currentSlotAutoArmor = inventoryManager.getAutoArmorCurrentSlot();

        glPushMatrix();
        glPushAttrib(GL_ENABLE_BIT);
        glDisable(GL_LIGHTING);

        if (mc.currentScreen instanceof GuiChest) {
            if (chestStealer.handleEvents() && !chestStealer.getSilentGUI() && chestStealer.getHighlightSlot()) {
                if (slot.slotNumber == currentSlotChestStealer && currentSlotChestStealer != -1 && currentSlotChestStealer != inventoryManager.getChestStealerLastSlot()) {
                    renderUtils.drawBorderedRect(x, y, x + 16, y + 16, chestStealer.getBorderStrength(), chestStealerBorderColor, chestStealerBackgroundColor);

                    // Prevent rendering the highlighted rectangle twice
                    if (!slot.getHasStack() && tick0.hasTimePassed(100)) {
                        inventoryManager.setChestStealerLastSlot(currentSlotChestStealer);
                        tick0.reset();
                    } else {
                        tick0.update();
                    }
                }
            }
        }

        if (mc.currentScreen instanceof GuiInventory) {
            if (inventoryManager.getHighlightSlotValue().get()) {
                if (inventoryCleaner.handleEvents()) {
                    if (slot.slotNumber == currentSlotInvCleaner && currentSlotInvCleaner != -1 && currentSlotInvCleaner != inventoryManager.getInvCleanerLastSlot()) {
                        renderUtils.drawBorderedRect(x, y, x + 16, y + 16, inventoryManager.getBorderStrength().get(), invManagerBorderColor, invManagerBackgroundColor);

                        // Prevent rendering the highlighted rectangle twice
                        if (!slot.getHasStack() && tick1.hasTimePassed(100)) {
                            inventoryManager.setInvCleanerLastSlot(currentSlotInvCleaner);
                            tick1.reset();
                        } else {
                            tick1.update();
                        }
                    }
                }

                if (autoArmor.handleEvents()) {
                    if (slot.slotNumber == currentSlotAutoArmor && currentSlotAutoArmor != -1 && currentSlotAutoArmor != inventoryManager.getAutoArmorLastSlot()) {
                        renderUtils.drawBorderedRect(x, y, x + 16, y + 16, inventoryManager.getBorderStrength().get(), invManagerBorderColor, invManagerBackgroundColor);

                        // Prevent rendering the highlighted rectangle twice
                        if (!slot.getHasStack() && tick2.hasTimePassed(100)) {
                            inventoryManager.setAutoArmorLastSlot(currentSlotAutoArmor);
                            tick2.reset();
                        } else {
                            tick2.update();
                        }
                    }
                }
            }
        }
        glPopAttrib();
        glPopMatrix();
    }
}
