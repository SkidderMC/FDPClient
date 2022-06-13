package net.ccbluex.liquidbounce.ui.client.particle;

import java.util.Random;

import net.ccbluex.liquidbounce.ui.client.particle.Particle;
import net.ccbluex.liquidbounce.utils.render.COL0rs;
import net.ccbluex.liquidbounce.utils.render.RUtils;
import net.ccbluex.liquidbounce.utils.render.SuperLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class ParticleSnow extends Particle {
    private Random random = new Random();
    private ScaledResolution res;

    public void draw(int xAdd) {
        this.prepare();
        this.move();
        this.drawPixel(xAdd);
        this.resetPos();
    }

    private void prepare() {
        this.res = new ScaledResolution(Minecraft.getMinecraft());
    }

    private void drawPixel(int xAdd) {
        float size = 10.0F;

        for (int i = 0; i < 10; ++i) {
            int alpha = Math.min(0, 1 - i / 10);
            RUtils.drawFilledCircle(this.vector.x, this.vector.y, size + 1.0F + (float) i * 0.2F, SuperLib.reAlpha(COL0rs.WHITE.c, (float) alpha), 5);
        }

        RUtils.drawFilledCircle(this.vector.x + (float) xAdd, this.vector.y, 1.1F, SuperLib.reAlpha(-1, 0.2F), 5);
        RUtils.drawFilledCircle(this.vector.x + (float) xAdd, this.vector.y, 0.8F, SuperLib.reAlpha(-1, 0.4F), 5);
        RUtils.drawFilledCircle(this.vector.x + (float) xAdd, this.vector.y, 0.5F, SuperLib.reAlpha(-1, 0.6F), 5);
        RUtils.drawFilledCircle(this.vector.x + (float) xAdd, this.vector.y, 0.3F, SuperLib.reAlpha(COL0rs.WHITE.c, 1.0F), 5);
    }

    private void move() {
        float speed = 100.0F;
        this.vector.y += this.random.nextFloat() * 0.25F;
        this.vector.x -= this.random.nextFloat();
    }

    private void resetPos() {
        if (this.vector.x < 0.0F) {
            this.vector.x = (float) this.res.getScaledWidth();
        }

        if (this.vector.y > (float) this.res.getScaledHeight()) {
            this.vector.y = 0.0F;
        }

    }
}
