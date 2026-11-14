/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.verification;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/** Compiled against Forge; executed on the real Lunar class loader after agent adaptation. */
public final class ForgeApiProbe {
    public static void run() {
        Minecraft mc = Minecraft.getMinecraft();
        C00Handshake handshake = new C00Handshake(47, "localhost", 25565, EnumConnectionState.LOGIN, true);
        check(handshake.getProtocolVersion() == 47, "handshake constructor");
        check(Math.abs(mc.thePlayer.eyeHeight - 1.62F) < 0.001F, "player eye-height field");
        check(World.MAX_ENTITY_RADIUS == 2.0D, "entity scan radius");
        BlockPos pos = mc.thePlayer.getPosition();
        check(Blocks.ladder.isLadder(mc.theWorld, pos, mc.thePlayer), "ladder hook");
        check(Blocks.vine.isLadder(mc.theWorld, pos, mc.thePlayer), "vine hook");
        Block stone = Blocks.stone;
        check(stone.getLightOpacity(mc.theWorld, pos) == stone.getLightOpacity(), "block opacity hook");
        check(!mc.thePlayer.canRiderInteract(), "rider interaction hook");
        Item sword = Items.diamond_sword;
        check(sword.isRepairable(), "repairable item hook");
        check(!sword.doesSneakBypassUse(mc.theWorld, pos, mc.thePlayer), "sneak hook");
        check(!sword.onEntitySwing(mc.thePlayer, new ItemStack(sword)), "swing hook");
        TileEntityChest chest = new TileEntityChest();
        chest.setWorldObj(mc.theWorld);
        chest.setPos(pos);
        check(chest.getRenderBoundingBox() != null, "tile-entity bounds hook");
        CreativeTabs tab = new CreativeTabs("FdpSmoke") {
            public Item getTabIconItem() { return Items.apple; }
        };
        check(tab.getTabLabel().equals("FdpSmoke"), "creative-tab constructor");
    }

    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError("Forge compatibility API: " + message);
    }
}
