package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.raphimc.vialoader.util.VersionEnum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemBlock.class)
public class MixinItemBlock extends Item {

    @Shadow
    @Final
    public Block block;


    @Overwrite
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }

        if (stack.stackSize == 0) {
            return false;
        } else if (!playerIn.canPlayerEdit(pos, side, stack)) {
            return false;
        } else if (worldIn.canBlockBePlaced(this.block, pos, false, side, null, stack)) {
            int i = this.getMetadata(stack.getMetadata());
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, pos, side, hitX, hitY, hitZ, i, playerIn);

            if (worldIn.setBlockState(pos, iblockstate1, 3)) {
                iblockstate1 = worldIn.getBlockState(pos);

                if (iblockstate1.getBlock() == this.block) {
                    ItemBlock.setTileEntityNBT(worldIn, playerIn, pos, stack);
                    this.block.onBlockPlacedBy(worldIn, pos, iblockstate1, playerIn, stack);
                }

                if (ProtocolBase.getManager().getTargetVersion().getProtocol() != VersionEnum.r1_8.getProtocol() && !MinecraftInstance.mc.isIntegratedServerRunning()) {
                    MinecraftInstance.mc.theWorld.playSoundAtPos(pos.add(0.5, 0.5, 0.5), this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F, false);
                } else {
                    worldIn.playSoundEffect((float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F, (float) pos.getZ() + 0.5F, this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
                }

                --stack.stackSize;
            }

            return true;
        } else {
            return false;
        }
    }
}