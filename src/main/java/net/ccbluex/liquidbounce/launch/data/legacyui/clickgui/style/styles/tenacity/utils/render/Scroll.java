package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.render;

import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.animations.Animation;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.animations.Direction;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.animations.impl.SmoothStepAnimation;
import org.lwjgl.input.Mouse;

public class Scroll {

    //Contionability import librarie lombok please and fix

    //@Getter
    //@Setter

    public  float maxScroll = Float.MAX_VALUE, minScroll = 0, rawScroll;
    private float scroll;
    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);

    public void onScroll(int ms) {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        rawScroll += Mouse.getDWheel() / 4f;
        rawScroll = Math.max(Math.min(minScroll, rawScroll), -maxScroll);
        scrollAnimation = new SmoothStepAnimation(ms, rawScroll - scroll, Direction.BACKWARDS);
    }

    public boolean isScrollAnimationDone() {
        return scrollAnimation.isDone();
    }

    public float getScroll() {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        return scroll;
    }

    public void setMaxScroll(float maxScroll) {
        this.maxScroll = maxScroll;
    }

    public float getMaxScroll() {
        return maxScroll;
    }
}
