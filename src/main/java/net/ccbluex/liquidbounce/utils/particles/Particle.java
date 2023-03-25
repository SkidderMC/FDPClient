/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.particles;

import net.ccbluex.liquidbounce.utils.timer.ParticleTimer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLiquid;

public class Particle {

    private final ParticleTimer removeTimer = new ParticleTimer();

    public final Vec3 position;
    private final Vec3 delta;

    public Particle(final Vec3 position) {
        this.position = position;
        this.delta = new Vec3((Math.random() * 2.5 - 1.25) * 0.04, (Math.random() * 0.5 - 0.2) * 0.04, (Math.random() * 2.5 - 1.25) * 0.04);
        this.removeTimer.reset();
    }

    public void update() {
        final Block block1 = PlayerParticles.getBlock(this.position.xCoord, this.position.yCoord, this.position.zCoord + this.delta.zCoord);
        if (!(block1 instanceof BlockAir || block1 instanceof BlockBush || block1 instanceof BlockLiquid))
            this.delta.zCoord *= -0.8;

        final Block block2 = PlayerParticles.getBlock(this.position.xCoord, this.position.yCoord + this.delta.yCoord, this.position.zCoord);
        if (!(block2 instanceof BlockAir || block2 instanceof BlockBush || block2 instanceof BlockLiquid)) {
            this.delta.xCoord *= 0.99F;
            this.delta.zCoord *= 0.99F;

            this.delta.yCoord *= -0.5;
        }

        final Block block3 = PlayerParticles.getBlock(this.position.xCoord + this.delta.xCoord, this.position.yCoord, this.position.zCoord);
        if (!(block3 instanceof BlockAir || block3 instanceof BlockBush || block3 instanceof BlockLiquid))
            this.delta.xCoord *= -0.8;

        this.updateWithoutPhysics();
    }

    public void updateWithoutPhysics() {
        this.position.xCoord += this.delta.xCoord;
        this.position.yCoord += this.delta.yCoord;
        this.position.zCoord += this.delta.zCoord;
        this.delta.xCoord *= 0.999F;
        this.delta.yCoord -= 0.000026;
        this.delta.zCoord *= 0.999F;
    }
}
