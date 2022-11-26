package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.exploit.PacketFix;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Objects;

@Mixin(C08PacketPlayerBlockPlacement.class)
public class MixinC08PacketPlayerBlockPlacement {
    @Shadow
    private BlockPos position;
    @Shadow
    private int placedBlockDirection;
    @Shadow
    public ItemStack stack;
    @Shadow
    public float facingX;
    @Shadow
    public float facingY;
    @Shadow
    public float facingZ;

    /**
     * @author MatrixAura
     * @reason Fix right click
     */
    @Overwrite
    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(this.position);
        buf.writeByte(this.placedBlockDirection);
        buf.writeItemStackToBuffer(this.stack);
        if (Objects.requireNonNull(LiquidBounce.moduleManager.getModule(PacketFix.class)).getState()) {
            buf.writeFloat(this.facingX);
            buf.writeFloat(this.facingY);
            buf.writeFloat(this.facingZ);
        } else {
            buf.writeByte((int) (this.facingX * 16.0F));
            buf.writeByte((int) (this.facingY * 16.0F));
            buf.writeByte((int) (this.facingZ * 16.0F));
        }
    }


    /**
     * @author MatrixAura
     * @reason Fix right click
     */
    @Overwrite
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.position = buf.readBlockPos();
        this.placedBlockDirection = buf.readUnsignedByte();
        this.stack = buf.readItemStackFromBuffer();
        if (Objects.requireNonNull(LiquidBounce.moduleManager.getModule(PacketFix.class)).getState()) {
            this.facingX = buf.readFloat();
            this.facingY = buf.readFloat();
            this.facingZ = buf.readFloat();
        } else {
            this.facingX = (float) buf.readUnsignedByte() / 16.0F;
            this.facingY = (float) buf.readUnsignedByte() / 16.0F;
            this.facingZ = (float) buf.readUnsignedByte() / 16.0F;
        }
    }
}