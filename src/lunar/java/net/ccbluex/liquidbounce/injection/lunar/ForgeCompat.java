/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockVine;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.lang.reflect.*;

/** Optional Forge notifications live behind this boundary so Lunar never resolves Forge types. */
public final class ForgeCompat {
    private ForgeCompat() { }

    public static boolean isForgeSlider(Object gui) {
        for (Class<?> type = gui.getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().equals("net.minecraftforge.fml.client.config.GuiSlider")) return true;
        }
        return false;
    }

    public static Collection<?> brandings() {
        try {
            Class<?> handler = Class.forName("net.minecraftforge.fml.common.FMLCommonHandler", false, ForgeCompat.class.getClassLoader());
            return (Collection<?>) handler.getMethod("getBrandings", boolean.class)
                .invoke(handler.getMethod("instance").invoke(null), false);
        } catch (ClassNotFoundException lunar) { return Collections.emptyList(); }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException("Forge branding lookup failed", failure); }
    }

    public static void renderTileItem(Item item, int metadata) {
        try {
            Class.forName("net.minecraftforge.client.ForgeHooksClient", false, ForgeCompat.class.getClassLoader())
                .getMethod("renderTileItem", Item.class, int.class).invoke(null, item, metadata);
        } catch (ClassNotFoundException lunar) { }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException("Forge tile rendering failed", failure); }
    }

    // Forge's public field stores the standing height, before sneaking/sleeping adjustments.
    public static float playerEyeHeight(Entity player) { return 1.62F; }
    public static boolean canRiderInteract(Entity entity) { return false; }
    public static int blockLightOpacity(Block block, IBlockAccess world, BlockPos pos) { return block.getLightOpacity(); }
    public static boolean isLadder(Block block, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return block instanceof BlockLadder || block instanceof BlockVine;
    }
    public static boolean doesSneakBypassUse(Item item, World world, BlockPos pos, EntityPlayer player) { return false; }
    public static boolean isRepairable(Item item) { return item.isDamageable(); }
    public static boolean onEntitySwing(Item item, EntityLivingBase entity, ItemStack stack) { return false; }

    public static AxisAlignedBB tileBoundingBox(TileEntity tile) {
        BlockPos pos = tile.getPos();
        World world = tile.getWorld();
        if (world != null) {
            Block block = tile.getBlockType();
            if (block == Blocks.enchanting_table) return new AxisAlignedBB(pos, pos.add(1, 1, 1));
            if (block == Blocks.chest || block == Blocks.trapped_chest) return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
            if (block != null && block != Blocks.beacon) {
                try {
                    AxisAlignedBB collision = block.getCollisionBoundingBox(world, pos, world.getBlockState(pos));
                    if (collision != null) return collision;
                } catch (RuntimeException incompleteChunk) {
                    return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(1, 1, 1));
                }
            }
            return new AxisAlignedBB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        return new AxisAlignedBB(pos, pos.add(1, 1, 1));
    }

    public static synchronized int nextCreativeTabId() {
        try {
            Field tabs = CreativeTabs.class.getDeclaredField("field_78032_a");
            tabs.setAccessible(true);
            CreativeTabs[] current = (CreativeTabs[]) tabs.get(null);
            tabs.set(null, Arrays.copyOf(current, current.length + 1));
            return current.length;
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Cannot allocate a creative tab", failure); }
    }

    public static GuiScreen modList(GuiScreen parent) {
        try {
            return (GuiScreen) Class.forName("net.minecraftforge.fml.client.GuiModList", true, ForgeCompat.class.getClassLoader())
                .getConstructor(GuiScreen.class).newInstance(parent);
        } catch (ClassNotFoundException lunar) { return parent; }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
    }

    public static boolean hasTileEntity(Block block, IBlockState state) {
        try {
            return (Boolean) Block.class.getMethod("hasTileEntity", IBlockState.class).invoke(block, state);
        } catch (NoSuchMethodException lunar) { return block.hasTileEntity(); }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
    }

    public static boolean onItemUseFirst(Item item, ItemStack stack, EntityPlayer player, World world,
                                         BlockPos pos, EnumFacing side, float x, float y, float z) {
        if (item == null) return false;
        try {
            return (Boolean) Item.class.getMethod("onItemUseFirst", ItemStack.class, EntityPlayer.class,
                World.class, BlockPos.class, EnumFacing.class, float.class, float.class, float.class)
                .invoke(item, stack, player, world, pos, side, x, y, z);
        } catch (NoSuchMethodException lunar) { return false; }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
    }

    public static Method findMethod(Class<?> owner, String[] names, Class<?>... parameters) {
        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            for (String name : names) {
                try {
                    Method method = type.getDeclaredMethod(name, parameters);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException next) { }
            }
        }
        throw new IllegalStateException("Cannot find method in " + owner.getName());
    }

    public static void onPlayerDestroyItem(EntityPlayer player, ItemStack stack) {
        try {
            Class<?> factory = Class.forName("net.minecraftforge.event.ForgeEventFactory", false, ForgeCompat.class.getClassLoader());
            factory.getMethod("onPlayerDestroyItem", EntityPlayer.class, ItemStack.class).invoke(null, player, stack);
        } catch (ClassNotFoundException lunar) {
            // Vanilla/Lunar has no Forge event bus to notify.
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Forge item notification failed", failure); }
    }

    public static boolean fullBoundingBoxLadders() {
        try {
            return Class.forName("net.minecraftforge.common.ForgeModContainer", false, ForgeCompat.class.getClassLoader())
                .getField("fullBoundingBoxLadders").getBoolean(null);
        } catch (ClassNotFoundException lunar) { return false; }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
    }

    public static void postMouseEvent(int button, boolean down) {
        try {
            ClassLoader loader = ForgeCompat.class.getClassLoader();
            Class<?> forge = Class.forName("net.minecraftforge.common.MinecraftForge", false, loader);
            Class<?> mouse = Class.forName("net.minecraftforge.client.event.MouseEvent", true, loader);
            Object event = mouse.newInstance();
            Field key = mouse.getDeclaredField("button");
            Field state = mouse.getDeclaredField("buttonstate");
            key.setAccessible(true);
            state.setAccessible(true);
            key.setInt(event, button);
            state.setBoolean(event, down);
            Object bus = forge.getField("EVENT_BUS").get(null);
            Class<?> eventClass = Class.forName("net.minecraftforge.fml.common.eventhandler.Event", false, loader);
            bus.getClass().getMethod("post", eventClass).invoke(bus, event);
        } catch (ClassNotFoundException lunar) { }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException("Forge mouse notification failed", failure); }
    }
}
