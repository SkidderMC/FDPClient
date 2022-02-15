package net.ccbluex.liquidbounce.injection.forge.mixins.splash;

import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.render.AnimatedValue;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.ProgressManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

@Mixin(targets="net.minecraftforge.fml.client.SplashProgress$3", remap=false)
public abstract class MixinSplashProgressRunnable {

    @Shadow(remap = false)
    protected abstract void setGL();

    @Shadow(remap = false)
    protected abstract void clearGL();

    @Inject(method="run()V", at=@At(value="HEAD"), remap=false, cancellable=true)
    private void run(CallbackInfo callbackInfo) {
        callbackInfo.cancel();

        this.setGL();
        GL11.glClearColor(1f, 1f, 1f, 1f);

        ClientUtils.INSTANCE.logInfo("[Splash] Loading Texture...");
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        int tex;
        try {
            tex = RenderUtils.loadGlTexture(ImageIO.read(this.getClass().getResourceAsStream("/assets/minecraft/fdpclient/misc/splash.png")));
        } catch (IOException e) {
            tex = 0;
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        AnimatedValue animatedValue = new AnimatedValue();
        animatedValue.setType(EaseUtils.EnumEasingType.CIRC);
        animatedValue.setDuration(600L);

        ClientUtils.INSTANCE.logInfo("[Splash] Starting Render Thread...");
        while (!SplashProgress.done) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            int width = Display.getWidth();
            int height = Display.getHeight();
            GL11.glViewport(0, 0, width, height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0, width, height, 0.0, -1.0, 1.0);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glColor4f(1f, 1f, 1f, 1f);

            // draw splash background
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0.0f, 0.0f);
            GL11.glVertex2f(0.0f, 0.0f);
            GL11.glTexCoord2f(1.0f, 0.0f);
            GL11.glVertex2f(width, 0.0f);
            GL11.glTexCoord2f(1.0f, 1.0f);
            GL11.glVertex2f(width, height);
            GL11.glTexCoord2f(0.0f, 1.0f);
            GL11.glVertex2f(0.0f, height);
            GL11.glEnd();
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            // draw progress bar
            float rectX = width * 0.2f;
            float rectX2 = width * 0.8f;
            float rectY = height * 0.75f;
            float rectY2 = height * 0.8f;
            float rectRadius = height * 0.025f;
            float progress = (float) animatedValue.sync(getProgress());

            if(progress != 1f) {
                GL11.glColor4f(0f, 0f, 0f, 0.3f);
                RenderUtils.drawRoundedCornerRect(rectX, rectY, rectX2, rectY2, rectRadius);
            }

            if(progress != 0f) {
                GL11.glColor4f(1f, 1f, 1f, 1f);
                RenderUtils.drawRoundedCornerRect(rectX, rectY, rectX + (width * 0.6f * progress), rectY2, rectRadius);
            }

            SplashProgress.mutex.acquireUninterruptibly();
            Display.update();
            SplashProgress.mutex.release();
            if (SplashProgress.pause) {
                this.clearGL();
                this.setGL();
            }
            Display.sync(60);
        }

        GL11.glDeleteTextures(tex);
        this.clearGL();
    }

    private static float getProgress() {
        float progress = 0;
        Iterator<ProgressManager.ProgressBar> it = ProgressManager.barIterator();
        if (it.hasNext()) {
            ProgressManager.ProgressBar bar = it.next();
            progress = bar.getStep() / (float) bar.getSteps();
        }

        return progress;
    }
}