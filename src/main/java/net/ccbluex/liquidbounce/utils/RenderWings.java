/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.Wings;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderWings extends ModelBase {
    Minecraft mc = Minecraft.getMinecraft();
    private ResourceLocation location;
    private final ModelRenderer wing;
    private final ModelRenderer wingTip;
    private final boolean playerUsesFullHeight;
    final Wings Wings = FDPClient.moduleManager.getModule(Wings.class);
    String WingMode = Wings.getWingStyle().get();

    public RenderWings() {
        if(WingMode.equals("Dragon")) { this.location = new ResourceLocation("fdpclient/wings/DragonWings.png"); } else if(WingMode.equals("Simple")) { this.location = new ResourceLocation("fdpclient/wings/NeonWings.png"); }
        this.playerUsesFullHeight = true;
        this.setTextureOffset("wing.bone", 0, 0);
        this.setTextureOffset("wing.skin", -10, 8);
        this.setTextureOffset("wingtip.bone", 0, 5);
        this.setTextureOffset("wingtip.skin", -10, 18);
        this.wing = new ModelRenderer(this, "wing");
        this.wing.setTextureSize(30, 30);
        this.wing.setRotationPoint(-2.0F, 0.0F, 0.0F);
        this.wing.addBox("bone", -10.0F, -1.0F, -1.0F, 10, 2, 2);
        this.wing.addBox("skin", -10.0F, 0.0F, 0.5F, 10, 0, 10);
        this.wingTip = new ModelRenderer(this, "wingtip");
        this.wingTip.setTextureSize(30, 30);
        this.wingTip.setRotationPoint(-10.0F, 0.0F, 0.0F);
        this.wingTip.addBox("bone", -10.0F, -0.5F, -0.5F, 10, 1, 1);
        this.wingTip.addBox("skin", -10.0F, 0.0F, 0.5F, 10, 0, 10);
        this.wing.addChild(this.wingTip);
    }
    public void renderWings(float partialTicks) {
        double scale = 100 / 100.0D;
        double rotate = this.interpolate( mc.thePlayer.prevRenderYawOffset, mc.thePlayer.renderYawOffset, partialTicks);
        GL11.glPushMatrix();
        GL11.glScaled(-scale, -scale, scale);
        GL11.glRotated(180.0D + rotate, 0.0D, 1.0D, 0.0D);
        GL11.glTranslated(0.0, (-(this.playerUsesFullHeight ? 1.45 : 1.25)) / scale, 0.0);
        GL11.glTranslated(0.0D, 0.0D, 0.2D / scale);
        if (mc.thePlayer.isSneaking()) {
            GL11.glTranslated( 0.0, 0.125 / scale, 0.0);
        }
        if(Wings.getColourType().get().equals("Chroma")){ RenderUtils.glColor(ColorUtils.rainbow(), 255F);} else if(Wings.getColourType().get().equals("Custom")){RenderUtils.glColor(new Color(Wings.getCR().get(), Wings.getCG().get(), Wings.getCB().get()), 255F);} else { GL11.glColor3f(1,1,1);}
        this.mc.getTextureManager().bindTexture(this.location);

        for (int j = 0; j < 2; ++j) {
            GL11.glEnable(2884);
            float f11 = (float) (System.currentTimeMillis() % 1000L) / 1000.0F * 3.1415927F * 2.0F;
            this.wing.rotateAngleX = (float) Math.toRadians(-80.0D) - (float) Math.cos(f11) * 0.2F;
            this.wing.rotateAngleY = (float) Math.toRadians(20.0D) + (float) Math.sin(f11) * 0.4F;
            this.wing.rotateAngleZ = (float) Math.toRadians(20.0D);
            this.wingTip.rotateAngleZ = -((float) (Math.sin(f11 + 2.0F) + 0.5D)) * 0.75F;
            this.wing.render(0.0625F);
            GL11.glScalef(-1.0F, 1.0F, 1.0F);
            if (j == 0) {
                GL11.glCullFace(1028);
            }
        }

        GL11.glCullFace(1029);
        GL11.glDisable(2884);
        GL11.glColor3f(255.0F, 255.0F, 255.0F);
        GL11.glPopMatrix();
    }

    private double interpolate(float yaw1, float yaw2, float percent) {
        double f = (yaw1 + (yaw2 - yaw1) * percent) % 360.0D;
        if (f < 0.0F) {
            f += 360.0F;
        }

        return f;
    }
}
