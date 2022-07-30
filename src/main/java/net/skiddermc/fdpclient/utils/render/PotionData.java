package net.skiddermc.fdpclient.utils.render;

import net.minecraft.potion.Potion;

public class PotionData {
    public final Potion potion;
    public int maxTimer = 0;
    public float animationX = 0;
    public final Translate translate;
    public final int level;
    public PotionData(Potion potion, Translate translate, int level) {
        this.potion = potion;
        this.translate = translate;
        this.level = level;
    }

    public float getAnimationX() {
        return animationX;
    }

    public Potion getPotion() {
        return potion;
    }

    public int getMaxTimer() {
        return maxTimer;
    }
}