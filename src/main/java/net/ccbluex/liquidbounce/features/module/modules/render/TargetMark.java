/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.color.ColorMixer;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.AnimationUtils;
import net.ccbluex.liquidbounce.utils.render.BlendUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;
import java.awt.Color;

@ModuleInfo(name = "TargetMark", category = ModuleCategory.RENDER)
public class TargetMark extends Module {

    public final ListValue modeValue = new ListValue("Mode", new String[]{"Default", "Box", "Jello", "Tracers"}, "Default");
    private final ListValue colorModeValue = new ListValue("Color", new String[] {"Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer", "Health"}, "Custom");
	private final IntegerValue colorRedValue = new IntegerValue("Red", 255, 0, 255);
	private final IntegerValue colorGreenValue = new IntegerValue("Green", 255, 0, 255);
	private final IntegerValue colorBlueValue = new IntegerValue("Blue", 255, 0, 255);
	private final IntegerValue colorAlphaValue = new IntegerValue("Alpha", 255, 0, 255);
	private final FloatValue jelloAlphaValue = new FloatValue("JelloEndAlphaPercent", 0.4F, 0F, 1F, "x", () -> modeValue.get().equalsIgnoreCase("jello"));
	private final FloatValue jelloWidthValue = new FloatValue("JelloCircleWidth", 3F, 0.01F, 5F, () -> modeValue.get().equalsIgnoreCase("jello"));
	private final FloatValue jelloGradientHeightValue = new FloatValue("JelloGradientHeight", 3F, 1F, 8F, "m", () -> modeValue.get().equalsIgnoreCase("jello"));
	private final FloatValue jelloFadeSpeedValue = new FloatValue("JelloFadeSpeed", 0.1F, 0.01F, 0.5F, "x", () -> modeValue.get().equalsIgnoreCase("jello"));
	private final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F);
	private final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F);
	private final IntegerValue mixerSecondsValue = new IntegerValue("Seconds", 2, 1, 10);
	public final FloatValue moveMarkValue = new FloatValue("MoveMarkY", 0.6F, 0F, 2F, () -> modeValue.get().equalsIgnoreCase("default"));
	private final FloatValue thicknessValue = new FloatValue("Thickness", 1F, 0.1F, 5F, () -> modeValue.get().equalsIgnoreCase("tracers"));
   	private final BoolValue colorTeam = new BoolValue("Team", false);

	private EntityLivingBase entity;
	
	private double direction = 1,
				   yPos, progress = 0;
	
	private float al = 0;
	
	private AxisAlignedBB bb;

	private KillAura aura;

	private long lastMS = System.currentTimeMillis();
	private long lastDeltaMS = 0L;

    @Override
    public void onInitialize() {
        aura = LiquidBounce.moduleManager.getModule(KillAura.class);
    }

	@EventTarget
	public void onTick(TickEvent event) {
		if (modeValue.get().equalsIgnoreCase("jello") && !aura.getTargetModeValue().get().equalsIgnoreCase("multi"))
            al = AnimationUtils.changer(al, (aura.getTarget() != null ? jelloFadeSpeedValue.get() : -jelloFadeSpeedValue.get()), 0F, colorAlphaValue.get() / 255.0F);
	}
	
	@EventTarget
	public void onRender3D(Render3DEvent event) {
        if (modeValue.get().equalsIgnoreCase("jello") && !aura.getTargetModeValue().get().equalsIgnoreCase("multi")) {
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

		    if (aura.getTarget() != null) {
			    entity = aura.getTarget();
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
        } else if (modeValue.get().equalsIgnoreCase("default")) {
            if (!aura.getTargetModeValue().get().equalsIgnoreCase("multi") && aura.getTarget() != null) RenderUtils.drawPlatform(aura.getTarget(), (aura.getHitable()) ? ColorUtils.reAlpha(getColor(aura.getTarget()), colorAlphaValue.get()) : new Color(255, 0, 0, colorAlphaValue.get()));
        } else if (modeValue.get().equalsIgnoreCase("tracers")) {
			if (!aura.getTargetModeValue().get().equalsIgnoreCase("multi") && aura.getTarget() != null) {
				final Tracers tracers = LiquidBounce.moduleManager.getModule(Tracers.class);
				if (tracers == null) return;
			
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		        GL11.glEnable(GL11.GL_BLEND);
		        GL11.glEnable(GL11.GL_LINE_SMOOTH);
		        GL11.glLineWidth(thicknessValue.get());
		        GL11.glDisable(GL11.GL_TEXTURE_2D);
		        GL11.glDisable(GL11.GL_DEPTH_TEST);
		        GL11.glDepthMask(false);

        		GL11.glBegin(GL11.GL_LINES);

                int dist = (int)(mc.thePlayer.getDistanceToEntity(aura.getTarget()) * 2);

                if (dist > 255) dist = 255;

                tracers.drawTraces(aura.getTarget(), getColor(aura.getTarget()), false);
            	
        		GL11.glEnd();

        		GL11.glEnable(GL11.GL_TEXTURE_2D);
        		GL11.glDisable(GL11.GL_LINE_SMOOTH);
        		GL11.glEnable(GL11.GL_DEPTH_TEST);
        		GL11.glDepthMask(true);
        		GL11.glDisable(GL11.GL_BLEND);
        		GlStateManager.resetColor();
			}
		} else {
            if (!aura.getTargetModeValue().get().equalsIgnoreCase("multi") && aura.getTarget() != null) RenderUtils.drawEntityBox(aura.getTarget(), (aura.getHitable()) ? ColorUtils.reAlpha(getColor(aura.getTarget()), colorAlphaValue.get()) : new Color(255, 0, 0, colorAlphaValue.get()), false);
        }
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
			case "Rainbow":
			 	return new Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0));
			case "Sky":
				return RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get());
			case "LiquidSlowly":
				return ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get());
			case "Mixer":
				return ColorMixer.getMixedColor(0, mixerSecondsValue.get());
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

	@Override
	public String getTag() {
		return modeValue.get();
	}

}
