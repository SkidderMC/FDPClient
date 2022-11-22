package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
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

import java.util.HashMap;

// I didn't test you, so good luck, MurderDetector!

@ModuleInfo(name = "MurderDetector", category = ModuleCategory.MISC)
public class MurderDetector extends Module {
    public static Minecraft mc = Minecraft.getMinecraft();
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
    private static boolean mode; // Are you Killer?
    public static HashMap<EntityPlayer, KillerData> killerData = new HashMap<>();

    @EventTarget
    public static void onUpdate(UpdateEvent ignored) {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (player.inventory.getCurrentItem() != null && entity != mc.thePlayer) {
                    if (killerData.get(player) == null) {
                        if (isWeapon(player.inventory.getCurrentItem().getItem())) {
                            ClientUtils.INSTANCE.displayChatMessage("§a[%module.MurderDetector.name%]§c " + player.getName() + " is Killer!!!");
                            LiquidBounce.hud.addNotification(new Notification("§a[%module.MurderDetector.name%]§c", player.getName() + " is Killer!!!", NotifyType.WARNING, 4000, 500));
                            if (killerData.get(player) == null) killerData.put(player, new KillerData(player));
                        }
                    } else {
                        if (!isWeapon(player.inventory.getCurrentItem().getItem())) {
                            killerData.remove(player);
                        }
                    }

                }
            }
        }
    }

    @Override
    public void onEnable() {
        killerData.clear();
        for (ItemStack itemStack : mc.thePlayer.inventory.mainInventory) {
            if (itemStack.getItem() != null && isWeapon(itemStack.getItem())) {
                mode = true;
                return;
            }
        }
        mode = false;
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
}

class KillerData {
    public static String playerName = "";

    public KillerData(EntityPlayer player) {

    }
}
