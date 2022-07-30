package net.skiddermc.fdpclient.features.module.modules.misc;

import net.skiddermc.fdpclient.FDPClient;
import net.skiddermc.fdpclient.event.EventTarget;
import net.skiddermc.fdpclient.event.UpdateEvent;
import net.skiddermc.fdpclient.features.module.Module;
import net.skiddermc.fdpclient.features.module.ModuleCategory;
import net.skiddermc.fdpclient.features.module.ModuleInfo;
import net.skiddermc.fdpclient.ui.client.hud.element.elements.Notification;
import net.skiddermc.fdpclient.ui.client.hud.element.elements.NotifyType;
import net.skiddermc.fdpclient.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;

import java.util.HashMap;

@ModuleInfo(name = "MurderDetector", category = ModuleCategory.MISC)
public class MurderDetector extends Module {
    public static Minecraft mc=Minecraft.getMinecraft();
    public static int[] itemIds={288,396,412,398,75,50};
    public static Item[] itemTypes=new Item[] {
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
    public static HashMap<EntityPlayer, KillerData> killerData = new HashMap<EntityPlayer, KillerData>();
    @EventTarget
    public static void onUpdate(UpdateEvent event){
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                if (entityLivingBase instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entityLivingBase;
                    if(player.inventory.getCurrentItem()!=null) {
                        MurderDetector murderDetector=new MurderDetector();
                        if(killerData.get(player)==null){
                            if (murderDetector.isWeapon(player.inventory.getCurrentItem().getItem())) {
                                ClientUtils.INSTANCE.displayChatMessage("§a[%module.MurderDetector.name%]§c "+player.getName()+" is Killer!!!");
                                FDPClient.hud.addNotification(new Notification("§a[%module.MurderDetector.name%]§c",player.getName()+" is Killer!!!" , NotifyType.WARNING,4000,500));
                                if(killerData.get(player) == null) killerData.put(player, new KillerData(player));
                            }
                        }else{
                            if (!murderDetector.isWeapon(player.inventory.getCurrentItem().getItem())) {
                                killerData.remove(player);
                            }
                        }

                    }
                }
            }
        }
    }
    @Override
    public void onEnable(){
        killerData.clear();
    }
    public boolean isWeapon(Item item){
        for(int id:itemIds){
            Item itemId=Item.getItemById(id);
            //ClientUtils.INSTANCE.displayChatMessage(itemId+":"+item);
            if(item==itemId){
                return true;
            }
        }
        for(Item id:itemTypes){
            if(item==id){
                return true;
            }
        }
        return false;
    }
}
class KillerData {
    public static String playerName="";
    public KillerData(EntityPlayer player){

    }
}
