package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.event.WorldEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.value.BoolValue;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

// Tested on blocksmc.com (1.8.9) - works fine

@ModuleInfo(name = "MurderDetector", category = ModuleCategory.MISC)
public class MurderDetector extends Module {

    public static Minecraft mc = Minecraft.getMinecraft();
    private static boolean mode; // Are you Killer?
    public static BoolValue sendMessages = new BoolValue("SendMessages", false);
    public static ArrayList<EntityPlayer> detectedPlayers = new ArrayList<>();
    public static int[] itemIds = {288, 396, 412, 398, 75, 50};
    public static Item[] itemTypes = new Item[]{
            Items.fishing_rod,
            Items.diamond_hoe,
            Items.golden_hoe,
            Items.iron_hoe,
            Items.stone_hoe,
            Items.wooden_hoe,
            Items.stone_sword,
            Items.diamond_sword,
            Items.golden_sword,
            ItemBlock.getItemFromBlock(Blocks.sponge),
            Items.iron_sword,
            Items.wooden_sword,
            Items.diamond_axe,
            Items.golden_axe,
            Items.iron_axe,
            Items.stone_axe,
            Items.diamond_pickaxe,
            Items.wooden_axe,
            Items.golden_pickaxe,
            Items.iron_pickaxe,
            Items.stone_pickaxe,
            Items.wooden_pickaxe,
            Items.stone_shovel,
            Items.diamond_shovel,
            Items.golden_shovel,
            Items.iron_shovel,
            Items.wooden_shovel
    };

    @Override
    public void onEnable() {
        detectedPlayers.clear();
        for (ItemStack itemStack : mc.thePlayer.inventory.mainInventory) {
            if (itemStack.getItem() != null && isWeapon(itemStack.getItem())) {
                mode = true;
                return;
            }
        }
        mode = false;
    }

    @EventTarget
    public static void onUpdate(UpdateEvent ignored) {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (entity == mc.thePlayer) continue;
                if (detectedPlayers.contains(player)) continue;
                if (player.inventory.getCurrentItem() != null) {
                    if (isWeapon(player.inventory.getCurrentItem().getItem())) {
                        ClientUtils.INSTANCE.displayChatMessage("§8[§dMurderDetector§8]§c " + player.getName() + " is the murderer!");
                        LiquidBounce.hud.addNotification(new Notification("§dMurderDetector", player.getName() + " is the murderer!", NotifyType.WARNING, 4000, 500));
                        if (sendMessages.get()) sendChatMessage(player.getName());
                        detectedPlayers.add(player);
                    }
                }
            }
        }
    }

    @EventTarget
    public static void onWorldChange(WorldEvent ignored) {
        detectedPlayers.clear();
    }

    public static boolean isWeapon(Item item) {
        if (mode) {
            return item == Items.bow; // Bow: HYP
        }
        for (int id : itemIds) {
            Item itemId = Item.getItemById(id);
            if (item == itemId) {
                return true;
            }
        }
        for (Item id : itemTypes) {
            if (item == id) {
                return true;
            }
        }
        return false;
    }

    private static void sendChatMessage(String target) {
        int i = (int) (Math.random() * 3);
        switch (i) {
            case 0:
                mc.thePlayer.sendChatMessage(target + " is the killer!");
                break;
            case 1:
                mc.thePlayer.sendChatMessage(target + " is the murderer!");
                break;
            case 2:
                mc.thePlayer.sendChatMessage(target + " has tried to kill me");
                break;
        }
    }
}
