/*
 * This code is by: ZAVZ Hacked Client
 *  FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual;

import me.zywl.fdpclient.FDPClient;
import me.zywl.fdpclient.event.EventTarget;
import me.zywl.fdpclient.event.Render3DEvent;
import me.zywl.fdpclient.event.TickEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.handler.combat.CombatManager;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme;
import net.ccbluex.liquidbounce.utils.AnimationUtils;
import net.ccbluex.liquidbounce.utils.render.BlendUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import me.zywl.fdpclient.value.impl.BoolValue;
import me.zywl.fdpclient.value.impl.FloatValue;
import me.zywl.fdpclient.value.impl.IntegerValue;
import me.zywl.fdpclient.value.impl.ListValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "KillESP", category = ModuleCategory.VISUAL)
public class KillESP extends Module {

    private static final double DOUBLE_PI = Math.PI * 2;

    // Module options
    private final BoolValue DefaultMode = new BoolValue("Default", false);

    private final BoolValue fdpMode = new BoolValue("FDP", false);
    private final BoolValue JelloMode = new BoolValue("Jello", false);
    private final BoolValue SigmaMode = new BoolValue("Sigma", false);
    private final BoolValue TracersMode = new BoolValue("Tracers", false);
    private final BoolValue ZavzMode = new BoolValue("Zavz", true);
    private final BoolValue Zavz2Mode = new BoolValue("Zywl", false);
    private final BoolValue LiesMode = new BoolValue("Lies", false);
    private final BoolValue SimsMode = new BoolValue("Sims", false);
    private final BoolValue BlockMode = new BoolValue("Block", false);
    private final BoolValue OtherBlockMode = new BoolValue("OtherBlock", false);
    private final ListValue colorModeValue = new ListValue("Color", new String[]{"Custom", "Rainbow", "Sky", "Slowly", "Fade", "Mixer", "Health"}, "Custom");
    private final IntegerValue colorRedValue = new IntegerValue("Red", 255, 0, 255);
    private final IntegerValue colorGreenValue = new IntegerValue("Green", 255, 0, 255);
    private final IntegerValue colorBlueValue = new IntegerValue("Blue", 255, 0, 255);
    private final IntegerValue colorAlphaValue = new IntegerValue("Alpha", 255, 0, 255);
    private final BoolValue circleValue = new BoolValue("Circle", false);
    private final IntegerValue circleRedValue = new IntegerValue("CircleRed", 255, 0, 255);
    private final IntegerValue circleGreenValue = new IntegerValue("CircleGreen", 255, 0, 255);
    private final IntegerValue circleBlueValue = new IntegerValue("CircleBlue", 255, 0, 255);
    private final IntegerValue circleAlphaValue = new IntegerValue("CircleAlpha", 255, 0, 255);
    private final FloatValue circleThicknessValue = new FloatValue("CircleThickness", 2F, 1F, 5F);

    private final FloatValue speed = new FloatValue("Zavz-Speed", 0.1f, 0.0f, 10.0f);
    private final BoolValue dual = new BoolValue("Zavz-Dual", true);
    private final FloatValue jelloAlphaValue = new FloatValue("JelloEndAlphaPercent", 0.4F, 0F, 1F);
    private final FloatValue jelloWidthValue = new FloatValue("JelloCircleWidth", 3F, 0.01F, 5F);
    private final FloatValue jelloGradientHeightValue = new FloatValue("JelloGradientHeight", 3F, 1F, 8F);
    private final FloatValue jelloFadeSpeedValue = new FloatValue("JelloFadeSpeed", 0.1F, 0.01F, 0.5F);
    private final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F);
    private final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F);
    public final FloatValue moveMarkValue = new FloatValue("MoveMarkY", 0.6F, 0F, 2F);
    private final FloatValue thicknessValue = new FloatValue("Thickness", 1F, 0.1F, 5F);
    private final BoolValue colorTeam = new BoolValue("Team", false);
    private final FloatValue blockMarkExpandValue = new FloatValue("BlockExpandValue", 0.2f, -0.5f, 1f);

    // Other fields
    private final MSTimer markTimer = new MSTimer();
    private EntityLivingBase entity;
    private EntityLivingBase markEntity = null;
    private double direction = 1;
    private double yPos;
    private double progress = 0;
    private double start;
    private float al = 0;
    private AxisAlignedBB bb;
    private CombatManager combat;
    private KillAura aura;
    private long lastMS = System.currentTimeMillis();
    private long lastDeltaMS = 0L;

    @Override
    public void onEnable() {
        start = 0.0d;
    }

    @Override
    public void onInitialize() {
        combat = FDPClient.combatManager;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (JelloMode.get())
            al = AnimationUtils.changer(al, (combat.getTarget() != null ? jelloFadeSpeedValue.get() : -jelloFadeSpeedValue.get()), 0F, colorAlphaValue.get() / 255.0F);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (JelloMode.get()) {
            renderJelloMode(event);
        }

        if (DefaultMode.get()) {
            renderDefaultMode(event);
        }

        if (SimsMode.get()) {
            renderSimsMode(event);
        }

        if (LiesMode.get()) {
            renderLiesMode(event);
        }

        if (BlockMode.get()) {
            renderBlockMode(event);
        }

        if (OtherBlockMode.get()) {
            renderOtherBlockMode(event);
        }

        if (ZavzMode.get()) {
            renderZavzMode(event);
        }

        if (TracersMode.get()) {
            renderTracersMode(event);
        }

        if (Zavz2Mode.get()) {
            renderZavz2Mode(event);
        }

        if (SigmaMode.get()) {
            renderSigmaMode(event);
        }

        if (fdpMode.get()) {
            renderFDPMode(event);
        }

        if (circleValue.get()) {
            renderCircleMode(event);
        }

    }

    private void renderCircleMode(Render3DEvent event) {
        GL11.glPushMatrix();

        // Calculate interpolated player position for smooth rendering
        double interpolatedX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX;
        double interpolatedY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY;
        double interpolatedZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ;
        GL11.glTranslated(interpolatedX, interpolatedY, interpolatedZ);

        // Set up OpenGL rendering states
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glLineWidth(circleThicknessValue.get());
        GL11.glColor4f(
                circleRedValue.get() / 255.0F,
                circleGreenValue.get() / 255.0F,
                circleBlueValue.get() / 255.0F,
                circleAlphaValue.get() / 255.0F
        );

        GL11.glRotatef(90F, 1F, 0F, 0F);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 5) { // Adjust step for circle accuracy
            double angleRadians = Math.toRadians(i);
            GL11.glVertex2f(
                    (float) (Math.cos(angleRadians) * aura.getRangeValue().get()),
                    (float) (Math.sin(angleRadians) * aura.getRangeValue().get())
            );
        }

        GL11.glEnd();

        // Restore OpenGL states
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GL11.glPopMatrix();
    }

    private static final float RAINBOW_START_VALUE = 0.0f;
    private static final float RAINBOW_STOP_VALUE = 1.0f;
    private void renderFDPMode(Render3DEvent event) {

        int drawTime = (int) (System.currentTimeMillis() % 1500);
        boolean drawMode = drawTime > 750;
        double drawPercent = drawTime / 750.0;

        if (!drawMode) {
            drawPercent = 1 - drawPercent;
        } else {
            drawPercent -= 1;
        }

        mc.entityRenderer.disableLightmap();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        AxisAlignedBB bb = markEntity.getEntityBoundingBox();

        float radius = (float) ((bb.maxX - bb.minX + (bb.maxZ - bb.minZ)) * 0.5f);
        float height = (float) (bb.maxY - bb.minY);

        double posX = markEntity.lastTickPosX + (markEntity.posX - markEntity.lastTickPosX) * mc.timer.renderPartialTicks;
        double posY = markEntity.lastTickPosY + (markEntity.posY - markEntity.lastTickPosY) * mc.timer.renderPartialTicks;

        double x = markEntity.lastTickPosX + (markEntity.posX - markEntity.lastTickPosX) * event.getPartialTicks() - mc.getRenderManager().viewerPosX;
        double y = markEntity.lastTickPosY + (markEntity.posY - markEntity.lastTickPosY) * event.getPartialTicks() - mc.getRenderManager().viewerPosY;
        double z = markEntity.lastTickPosZ + (markEntity.posZ - markEntity.lastTickPosZ) * event.getPartialTicks() - mc.getRenderManager().viewerPosZ;

        mc.entityRenderer.disableLightmap();
        GL11.glLineWidth((radius * 8f));
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < 360; i += 10) {
            float hue = i < 180 ? RAINBOW_START_VALUE + (RAINBOW_STOP_VALUE - RAINBOW_START_VALUE) * i / 180f : RAINBOW_START_VALUE + (RAINBOW_STOP_VALUE - RAINBOW_START_VALUE) * (-(i - 360)) / 180f;
            Color color = Color.getHSBColor(hue, 0.7f, 1.0f);
            GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1.0f);
            GL11.glVertex3d(x - (float) Math.sin(i * Math.PI / 180F) * radius, y, z + (float) Math.cos(i * Math.PI / 180F) * radius);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderSigmaMode(Render3DEvent event) {
        long drawTime = System.currentTimeMillis() % 2000;
        boolean drawMode = drawTime > 1000;
        double drawPercent = drawTime / 1000.0;
        if (!drawMode) {
            drawPercent = 1 - drawPercent;
        } else {
            drawPercent -= 1;
        }
        drawPercent = easeInOutQuad(drawPercent);

        List<Vec3> points = new ArrayList<>();
        AxisAlignedBB bb = markEntity.getEntityBoundingBox();
        double radius = bb.maxX - bb.minX;
        double height = bb.maxY - bb.minY;
        double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks;
        double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks;
        if (drawMode) {
            posY -= 0.5;
        } else {
            posY += 0.5;
        }
        double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks;
        for (int i = 0; i <= 360; i += 7) {
            points.add(new Vec3(
                    posX - Math.sin(i * Math.PI / 180F) * radius,
                    posY + height * drawPercent,
                    posZ + Math.cos(i * Math.PI / 180F) * radius
            ));
        }
        points.add(points.get(0));

        // Draw
        mc.entityRenderer.disableLightmap();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        double baseMove = (drawPercent > 0.5) ? (1 - drawPercent) : drawPercent;
        double min = (height / 60) * 20 * (1 - baseMove) * (drawMode ? -1 : 1);
        for (int i = 0; i <= 20; i++) {
            double moveFace = (height / 60F) * i * baseMove;
            if (drawMode) {
                moveFace = -moveFace;
            }
            Vec3 firstPoint = points.get(0);
            GL11.glVertex3d(
                    firstPoint.xCoord - mc.getRenderManager().viewerPosX,
                    firstPoint.yCoord - moveFace - min - mc.getRenderManager().viewerPosY,
                    firstPoint.zCoord - mc.getRenderManager().viewerPosZ
            );
            GL11.glColor4f(1F, 1F, 1F, 0.7F * (i / 20F));
            for (Vec3 vec3 : points) {
                GL11.glVertex3d(
                        vec3.xCoord - mc.getRenderManager().viewerPosX,
                        vec3.yCoord - moveFace - min - mc.getRenderManager().viewerPosY,
                        vec3.zCoord - mc.getRenderManager().viewerPosZ
                );
            }
            GL11.glColor4f(0F, 0F, 0F, 0F);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }


    private void renderJelloMode(Render3DEvent event) {
        double lastY = yPos;

        if (al > 0F) {
            if (System.currentTimeMillis() - lastMS >= 1000L) {
                direction = -direction;
                lastMS = System.currentTimeMillis();
            }
            long weird = (direction > 0 ? System.currentTimeMillis() - lastMS : 1000L - (System.currentTimeMillis() - lastMS));
            progress = (double)weird / 1000D;
            lastDeltaMS = System.currentTimeMillis() - lastMS;
        } else { // keep the progress
            lastMS = System.currentTimeMillis() - lastDeltaMS;
        }

        if (combat.getTarget() != null) {
            entity = combat.getTarget();
            bb = entity.getEntityBoundingBox();
        }

        if (bb == null || entity == null) return;

        double radius = bb.maxX - bb.minX;
        double height = bb.maxY - bb.minY;
        double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks;
        double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks;
        double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks;

        yPos = easeInOutQuart(progress) * height;

        double deltaY = (direction > 0 ? yPos - lastY : lastY - yPos) * -direction * jelloGradientHeightValue.get();

        if (al <= 0 && entity != null) {
            entity = null;
            return;
        }

        Color colour = getColor(entity);
        float r = colour.getRed() / 255.0F;
        float g = colour.getGreen() / 255.0F;
        float b = colour.getBlue() / 255.0F;

        pre3D();
        //post circles
        GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        GL11.glBegin(GL11.GL_QUAD_STRIP);

        for (int i = 0; i <= 360; i++) {
            double calc = i * Math.PI / 180;
            double posX2 = posX - Math.sin(calc) * radius;
            double posZ2 = posZ + Math.cos(calc) * radius;

            GL11.glColor4f(r, g, b, 0F);
            GL11.glVertex3d(posX2, posY + yPos + deltaY, posZ2);

            GL11.glColor4f(r, g, b, al * jelloAlphaValue.get());
            GL11.glVertex3d(posX2, posY + yPos, posZ2);
        }

        GL11.glEnd();

        drawCircle(posX, posY + yPos, posZ, jelloWidthValue.get(), radius, r, g, b, al);

        post3D();
    }
    private void renderDefaultMode(Render3DEvent event) {
        EntityLivingBase target = combat.getTarget();
        if (target != null) {
            Color color = (target.hurtTime > 0) ? ColorUtils.reAlpha(getColor(target), colorAlphaValue.get()) : new Color(235, 40, 40, colorAlphaValue.get());
            RenderUtils.drawPlatforms(target, color);
        }
    }

    private void renderSimsMode(Render3DEvent event) {
        EntityLivingBase target = combat.getTarget();
        if (target != null) {
            renderESP();
            int colorRGB = (target.hurtTime <= 0) ? new Color(80, 255, 80, 200).getRGB() : new Color(255, 0, 0, 200).getRGB();
            drawESP(target, colorRGB, event);
        }
    }

    private void renderLiesMode(Render3DEvent event) {
        final EntityLivingBase target = combat.getTarget();

        if (target == null) {
            return;
        }

        final float ticks = event.getPartialTicks();

        int interval = 3000;
        long drawTime = System.currentTimeMillis() % interval;
        boolean drawMode = drawTime > (interval / 2);
        double drawPercent = drawTime / (interval / 2.0);

        // true when goes up
        if (!drawMode) {
            drawPercent = 1 - drawPercent;
        } else {
            drawPercent -= 1;
        }

        drawPercent = easeInOutQuad(drawPercent);

        mc.entityRenderer.disableLightmap();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_FLAT);
        mc.entityRenderer.disableLightmap();

        AxisAlignedBB bb = target.getEntityBoundingBox();
        float radius = ((float) (bb.maxX - bb.minX) + (float) (bb.maxZ - bb.minZ)) * 0.5f;
        float height = (float) (bb.maxY - bb.minY);
        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * event.getPartialTicks() - mc.getRenderManager().viewerPosX;
        double y = (target.lastTickPosY + (target.posY - target.lastTickPosY) * event.getPartialTicks() - mc.getRenderManager().viewerPosY) + height * drawPercent;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.getPartialTicks() - mc.getRenderManager().viewerPosZ;

        float eased = (float) ((height / 3) * ((drawPercent > 0.5) ? (1 - drawPercent) : drawPercent) * (drawMode ? -1 : 1));

        for (int i = 5; i <= 360; i += 5) {
            Color color = Color.getHSBColor(
                    (i < 180)
                            ? ClientTheme.INSTANCE.getRainbowStartValue().get() + (ClientTheme.INSTANCE.getRainbowStopValue().get() - ClientTheme.INSTANCE.getRainbowStartValue().get()) * (i / 180f)
                            : ClientTheme.INSTANCE.getRainbowStartValue().get() + (ClientTheme.INSTANCE.getRainbowStopValue().get() - ClientTheme.INSTANCE.getRainbowStartValue().get()) * (-(i - 360) / 180f),
                    0.7f,
                    1.0f
            );
            double x1 = x - Math.sin(i * Math.PI / 180F) * radius;
            double z1 = z + Math.cos(i * Math.PI / 180F) * radius;
            double x2 = x - Math.sin((i - 5) * Math.PI / 180F) * radius;
            double z2 = z + Math.cos((i - 5) * Math.PI / 180F) * radius;
            GL11.glBegin(GL11.GL_QUADS);
            RenderUtils.glColor(color, 0f);
            GL11.glVertex3d(x1, y + eased, z1);
            GL11.glVertex3d(x2, y + eased, z2);
            RenderUtils.glColor(color, 150f);
            GL11.glVertex3d(x2, y, z2);
            GL11.glVertex3d(x1, y, z1);
            GL11.glEnd();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderBlockMode(Render3DEvent event) {

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer ) {
                continue;
            }

            EntityLivingBase livingEntity = (EntityLivingBase) entity;

            if (BlockMode.get() || OtherBlockMode.get()) {
                AxisAlignedBB originalBB = livingEntity.getEntityBoundingBox();
                AxisAlignedBB expandedBB = originalBB.expand(blockMarkExpandValue.get(), blockMarkExpandValue.get(), blockMarkExpandValue.get());
                livingEntity.setEntityBoundingBox(expandedBB);

                Color boxColor;
                if (livingEntity.hurtTime <= 0) {
                    if (livingEntity == combat.getTarget()) {
                        boxColor = new Color(25, 230, 0, 170);
                    } else {
                        boxColor = new Color(10, 250, 10, 170);
                    }
                } else {
                    boxColor = new Color(255, 0, 0, 170);
                }

                RenderUtils.drawEntityBox(livingEntity, boxColor, BlockMode.get(), true, 4f);

                livingEntity.setEntityBoundingBox(originalBB);
            }
        }
    }

    private void renderOtherBlockMode(Render3DEvent event) {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer) {
                continue;
            }

            EntityLivingBase livingEntity = (EntityLivingBase) entity;

            if (OtherBlockMode.get()) {
                AxisAlignedBB originalBB = livingEntity.getEntityBoundingBox();
                AxisAlignedBB expandedBB = originalBB.expand(blockMarkExpandValue.get(), blockMarkExpandValue.get(), blockMarkExpandValue.get());
                livingEntity.setEntityBoundingBox(expandedBB);

                Color boxColor;
                if (livingEntity.hurtTime <= 0) {
                    if (livingEntity == combat.getTarget()) {
                        boxColor = new Color(25, 230, 0, 170);
                    } else {
                        boxColor = new Color(10, 250, 10, 170);
                    }
                } else {
                    boxColor = new Color(255, 0, 0, 170);
                }

                RenderUtils.drawEntityBox(livingEntity, boxColor, BlockMode.get(), true, 4f);

                livingEntity.setEntityBoundingBox(originalBB);
            }
        }
    }

    private void renderZavzMode(Render3DEvent event) {
        final EntityLivingBase target = combat.getTarget();

        if (target == null) {
            return;
        }

        final float ticks = event.getPartialTicks();

        GL11.glPushMatrix();
        GL11.glDisable(3553);

        RenderUtils.startSmooth();

        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(3);

        final double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * ticks - mc.getRenderManager().viewerPosX,
                z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * ticks - mc.getRenderManager().viewerPosZ;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * ticks - mc.getRenderManager().viewerPosY;

        final double radius = 0.65d;
        final int precision = 360;

        final double startPos = start % 360;

        start += speed.get();

        for (int i = 0; i <= precision; i++) {
            final double posX = x + radius * Math.cos(startPos + i * DOUBLE_PI / (precision / 2.0d)),
                    posZ = z + radius * Math.sin(startPos + i * DOUBLE_PI / (precision / 2.0d));

            RenderUtils.glColor(Objects.requireNonNull(ClientTheme.getColor(1)));

            GL11.glVertex3d(posX, y, posZ);

            y += target.height / precision;

            RenderUtils.glColor(0, 0, 0, 0);
        }

        GL11.glEnd();
        GL11.glDepthMask(true);
        GL11.glEnable(2929);

        RenderUtils.endSmooth();

        GL11.glEnable(3553);
        GL11.glPopMatrix();

        if (dual.get()) {
            GL11.glPushMatrix();
            GL11.glDisable(3553);

            RenderUtils.startSmooth();

            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);

            final double startPos2 = start % 360;

            start += speed.get();

            y = target.lastTickPosY + (target.posY - target.lastTickPosY) * ticks - mc.getRenderManager().viewerPosY + target.height;

            for (int i = 0; i <= precision; i++) {
                final double posX = x + radius * Math.cos(-(startPos2 + i * DOUBLE_PI / (precision / 2.0d))),
                        posZ = z + (radius * Math.sin(-(startPos2 + i * DOUBLE_PI / (precision / 2.0d))));

                RenderUtils.glColor(ClientTheme.getColor(1));

                GL11.glVertex3d(posX, y, posZ);

                y -= target.height / precision;

                RenderUtils.glColor(0, 0, 0, 0);
            }

            GL11.glEnd();
            GL11.glDepthMask(true);
            GL11.glEnable(2929);

            RenderUtils.endSmooth();

            GL11.glEnable(3553);
            GL11.glPopMatrix();
        }
    }

    private void renderZavz2Mode(Render3DEvent event) {
        final EntityLivingBase target = combat.getTarget();

        if (target == null) {
            return;
        }

        final float ticks = event.getPartialTicks();

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        renderRing(target, ticks, false);
        if (dual.get()) {
            renderRing(target, ticks, true);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderRing(EntityLivingBase target, float ticks, boolean dualRing) {
        final double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * ticks - mc.getRenderManager().viewerPosX;
        final double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * ticks - mc.getRenderManager().viewerPosZ;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * ticks - mc.getRenderManager().viewerPosY;

        final double radius = 0.65d;
        final int precision = 360;

        final double startPos = start % 360;

        start += speed.get();

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (int i = 0; i <= precision; i++) {
            final double angle = startPos + i * Math.PI * 2.0 / precision;
            final double posX = x + radius * Math.cos(angle);
            final double posZ = z + radius * Math.sin(angle);

            final double offset = Math.abs(System.currentTimeMillis() / 10L) / 100.0 + y;
            final int alpha = dualRing ? 0 : 170;
            final Color color = ColorUtils.getGradientOffset(
                    ClientTheme.getColor(1),
                    ClientTheme.getColor(2),
                    offset
            );

            RenderUtils.glColor(color, alpha);
            GL11.glVertex3d(posX, y, posZ);

            if (dualRing) {
                y -= target.height / precision;
            } else {
                y += target.height / precision;
            }
        }

        GL11.glEnd();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }

    private void renderTracersMode(Render3DEvent event) {
        final Tracers tracers = FDPClient.moduleManager.getModule(Tracers.class);
        if (tracers == null) return;

        final EntityLivingBase target = combat.getTarget();

        if (target == null) {
            return;
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(thicknessValue.get());
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glBegin(GL11.GL_LINES);

        int dist = (int)(Objects.requireNonNull(combat.getTarget()).getDistanceToEntity(mc.thePlayer) * 2);

        if (dist > 255) dist = 255;

        tracers.drawTraces(combat.getTarget(), getColor(combat.getTarget()), false);

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.resetColor();
    }
    private void renderESP() {
        if (markEntity != null) {
            if (markTimer.hasTimePassed(500) || markEntity.isDead) {
                markEntity = null;
                return;
            }

            // Can mark
            long drawTime = System.currentTimeMillis() % 2000;
            boolean drawMode = drawTime > 1000;
            float drawPercent = drawTime / 1000.0f;

            // True when goes up
            if (!drawMode) {
                drawPercent = 1 - drawPercent;
            } else {
                drawPercent -= 1;
            }

            List<Vec3> points = new ArrayList<>();
            AxisAlignedBB bb = markEntity.getEntityBoundingBox();
            double radius = bb.maxX - bb.minX;
            double height = bb.maxY - bb.minY;
            double posX = markEntity.lastTickPosX + (markEntity.posX - markEntity.lastTickPosX) * mc.timer.renderPartialTicks;
            double posY = markEntity.lastTickPosY + (markEntity.posY - markEntity.lastTickPosY) * mc.timer.renderPartialTicks;
            if (drawMode) {
                posY -= 0.5;
            } else {
                posY += 0.5;
            }
            double posZ = markEntity.lastTickPosZ + (markEntity.posZ - markEntity.lastTickPosZ) * mc.timer.renderPartialTicks;
            for (int i = 0; i <= 360; i += 7) {
                points.add(new Vec3(
                        posX - Math.sin(i * Math.PI / 180F) * radius,
                        posY + height * drawPercent,
                        posZ + Math.cos(i * Math.PI / 180F) * radius
                ));
            }
            points.add(points.get(0));

            // Draw
            mc.entityRenderer.disableLightmap();
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            for (int i = 0; i <= 20; i++) {
                double moveFace = (height / 60.0) * i;
                if (drawMode) {
                    moveFace = -moveFace;
                }
                Vec3 firstPoint = points.get(0);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(
                        firstPoint.xCoord - mc.getRenderManager().viewerPosX,
                        firstPoint.yCoord - moveFace - mc.getRenderManager().viewerPosY,
                        firstPoint.zCoord - mc.getRenderManager().viewerPosZ
                );
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f * (i / 20.0f));
                for (Vec3 vec3 : points) {
                    GL11.glVertex3d(
                            vec3.xCoord - mc.getRenderManager().viewerPosX,
                            vec3.yCoord - moveFace - mc.getRenderManager().viewerPosY,
                            vec3.zCoord - mc.getRenderManager().viewerPosZ
                    );
                }
                GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
                GL11.glEnd();
            }
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
        }
    }

    private void drawESP(EntityLivingBase entity, int color, Render3DEvent e) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * e.getPartialTicks() - mc.getRenderManager().renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * e.getPartialTicks() - mc.getRenderManager().renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * e.getPartialTicks() - mc.getRenderManager().renderPosZ;
        float radius = 0.15f;
        int side = 4;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y + 2, z);
        GL11.glRotatef(-entity.width, 0.0f, 1.0f, 0.0f);

        RenderUtils.glColor(color);
        RenderUtils.enableSmoothLine(1.5f);

        Cylinder c = new Cylinder();
        GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        c.setDrawStyle(100012);
        RenderUtils.glColor((entity.hurtTime <= 0) ? new Color(80, 255, 80, 200) : new Color(255, 0, 0, 200));
        c.draw(0.0f, radius, 0.3f, side, 1);
        c.setDrawStyle(100012);

        GL11.glTranslated(0.0, 0.0, 0.3);
        c.draw(radius, 0.0f, 0.3f, side, 1);

        GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
        c.setDrawStyle(100011);

        GL11.glTranslated(0.0, 0.0, -0.3);
        RenderUtils.glColor(color);
        c.draw(0.0f, radius, 0.3f, side, 1);
        c.setDrawStyle(100011);

        GL11.glTranslated(0.0, 0.0, 0.3);
        c.draw(radius, 0.0f, 0.3f, side, 1);

        RenderUtils.disableSmoothLine();
        GL11.glPopMatrix();
    }

    public final Color getColor(final Entity ent) {
        if (ent instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) ent;

            if (colorModeValue.get().equalsIgnoreCase("Health"))
                return BlendUtils.getHealthColor(entityLivingBase.getHealth(), entityLivingBase.getMaxHealth());

            if (colorTeam.get()) {
                final char[] chars = entityLivingBase.getDisplayName().getFormattedText().toCharArray();
                int color = Integer.MAX_VALUE;

                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] != '§' || i + 1 >= chars.length)
                        continue;

                    final int index = GameFontRenderer.getColorIndex(chars[i + 1]);

                    if (index < 0 || index > 15)
                        continue;

                    color = ColorUtils.hexColors[index];
                    break;
                }

                return new Color(color);
            }
        }

        switch (colorModeValue.get()) {
            case "Custom":
                return new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
            case "Slowly":
                return ColorUtils.slowlyRainbow(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get());
            default:
                return ColorUtils.fade(new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100);
        }
    }

    public static void pre3D() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glDisable(2884);
    }

    public static void post3D() {
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    private void drawCircle(double x, double y, double z, float width, double radius, float red, float green, float blue, float alp) {
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glColor4f(red, green, blue, alp);

        for (int i = 0; i <= 360; i += 1) {
            double posX = x - Math.sin(i * Math.PI / 180) * radius;
            double posZ = z + Math.cos(i * Math.PI / 180) * radius;
            GL11.glVertex3d(posX, y, posZ);
        }

        GL11.glEnd();
    }

    private double easeInOutQuart(double x) {
        return (x < 0.5) ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2;
    }

    private double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

}
