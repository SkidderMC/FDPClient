package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "JumpCircle", category = ModuleCategory.RENDER)
public class JumpCircle extends Module {
    private final IntegerValue redValue = new IntegerValue("Red", 255, 0, 255);
    private final IntegerValue greenValue = new IntegerValue("Green", 255, 0, 255);
    private final IntegerValue blueValue = new IntegerValue("Blue", 255, 0, 255);
    private final IntegerValue radiusValue = new IntegerValue("Radius", 3, 1, 5);
    private final FloatValue widthValue = new FloatValue("Width", 0.5F, 0.1F, 50F);
    private final FloatValue strengthValue = new FloatValue("Strength", 0.02F, 0.01F, 0.2F);
    private final CopyOnWriteArrayList<Circle> circles = new CopyOnWriteArrayList<>();
    private boolean lastOnGround;

    @Override
    public void onEnable(){
        lastOnGround = true;
    }

    @EventTarget
    public void onUpdate(UpdateEvent ignored) {
        if (mc.thePlayer.onGround && !lastOnGround) {
            circles.add(new Circle(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY, mc.thePlayer.lastTickPosZ, widthValue.get()));
        }
        lastOnGround = mc.thePlayer.onGround;
    }

    @EventTarget
    public void onRender3D(Render3DEvent ignored) {
        if (!circles.isEmpty()) {
            for (Circle circle : circles) {
                if (circle.add(strengthValue.get()) > radiusValue.get()) {
                    circles.remove(circle);
                    continue;
                }
                GL11.glPushMatrix();
                GL11.glTranslated(
                        circle.posX - mc.getRenderManager().renderPosX,
                        circle.posY - mc.getRenderManager().renderPosY,
                        circle.posZ - mc.getRenderManager().renderPosZ
                );
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                GL11.glLineWidth(circle.width);
                GL11.glColor4f(redValue.get() / 255.0F, greenValue.get() / 255.0F, blueValue.get() / 255.0F, (radiusValue.get() - circle.radius) / radiusValue.get());
                GL11.glRotatef(90F, 1F, 0F, 0F);

                GL11.glBegin(GL11.GL_LINE_STRIP);

                for (int i = 0; i <= 360; i += 5) { // You can change circle accuracy  (60 - accuracy)
                    GL11.glVertex2f(
                            (float) (Math.cos(i * Math.PI / 180.0) * circle.radius),
                            (float) (Math.sin(i * Math.PI / 180.0) * circle.radius)
                    );
                }

                GL11.glEnd();

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glPopMatrix();
            }
        }
    }

    static class Circle {
        public double posX, posY, posZ, lastTickPosX, lastTickPosY, lastTickPosZ;
        public float radius, width;
        Circle(double posX, double posY, double posZ, double lastTickPosX, double lastTickPosY, double lastTickPosZ, float width) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.lastTickPosX = lastTickPosX;
            this.lastTickPosY = lastTickPosY;
            this.lastTickPosZ = lastTickPosZ;
            this.width = width;
        }
        public double add(double radius) {
            this.radius += radius;
            return this.radius;
        }
    }
}