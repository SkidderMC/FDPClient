package net.ccbluex.liquidbounce.utils.render;

import net.minecraft.potion.Potion;

public class PotionData {
    public final potion;
    public int maxTimer = 0;
    public float animationX = 0;
    public final translate;
    public final int level;
    
    public PotionData(potion, translate, int level) {
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
