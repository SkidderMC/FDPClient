/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.UpdateModelEvent;
import net.ccbluex.liquidbounce.features.module.modules.visual.CustomModel;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(ModelPlayer.class)
public class MixinModelPlayerFix extends ModelBiped {

    public ModelRenderer left_leg;
    public ModelRenderer right_leg;
    public ModelRenderer body;
    public ModelRenderer eye;
    public ModelRenderer rabbitBone;
    public ModelRenderer rabbitRleg;
    public ModelRenderer rabbitLarm;
    public ModelRenderer rabbitRarm;
    public ModelRenderer rabbitLleg;
    public ModelRenderer rabbitHead;
    public ModelRenderer fredhead;
    public ModelRenderer armLeft;
    public ModelRenderer legRight;
    public ModelRenderer legLeft;
    public ModelRenderer armRight;
    public ModelRenderer fredbody;
    public ModelRenderer armLeftpad2;
    public ModelRenderer torso;
    public ModelRenderer earRightpad_1;
    public ModelRenderer armRightpad2;
    public ModelRenderer legLeftpad;
    public ModelRenderer hat;
    public ModelRenderer legLeftpad2;
    public ModelRenderer armRight2;
    public ModelRenderer legRight2;
    public ModelRenderer earRightpad;
    public ModelRenderer armLeft2;
    public ModelRenderer frednose;
    public ModelRenderer earLeft;
    public ModelRenderer footRight;
    public ModelRenderer legRightpad2;
    public ModelRenderer legRightpad;
    public ModelRenderer armLeftpad;
    public ModelRenderer legLeft2;
    public ModelRenderer footLeft;
    public ModelRenderer hat2;
    public ModelRenderer armRightpad;
    public ModelRenderer earRight;
    public ModelRenderer crotch;
    public ModelRenderer jaw;
    public ModelRenderer handRight;
    public ModelRenderer handLeft;
    public ModelRenderer bT1;
    public ModelRenderer bT2;
    public ModelRenderer bF1;
    public ModelRenderer bF2;
    public ModelRenderer bF3;
    public ModelRenderer bF4;

    private float breastOffsetT1 = 0.0F;
    private float breastOffsetT2 = 0.0F;
    private float breastOffsetF1 = 0.0F;
    private float breastOffsetF2 = 0.0F;
    private float breastOffsetF3 = 0.0F;
    private float breastOffsetF4 = 0.0F;

    @Shadow
    private boolean smallArms;

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 2.5F))
    private float fixAlexArmHeight(final float original) {
        return 2F;
    }

    /**
     * @author As_pw
     * @reason Post Arm Renderer
     */
    @Override
    @Overwrite
    public void postRenderArm(final float scale) {
        if (this.smallArms) {
            this.bipedRightArm.rotationPointX += 0.5F;
            this.bipedRightArm.postRender(scale);
            this.bipedRightArm.rotationPointZ -= 0.5F;
        } else {
            this.bipedRightArm.postRender(scale);
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderHook(final Entity entityIn, final float limbSwing, final float limbSwingAmount,
                           final float ageInTicks, final float netHeadYaw, final float headPitch,
                           final float scale, final CallbackInfo ci) {
        final CustomModel customModel = CustomModel.INSTANCE;
        if (customModel.getState()) {
            if (!customModel.getMode().equals("Female")) {
                ci.cancel();
                renderCustom(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderFemaleDetails(final Entity entityIn, final float limbSwing, final float limbSwingAmount,
                                    final float ageInTicks, final float netHeadYaw, final float headPitch,
                                    final float scale, final CallbackInfo ci) {
        final CustomModel customModel = CustomModel.INSTANCE;
        if (customModel.getState() && customModel.getMode().equals("Female") && entityIn instanceof EntityPlayer) {
            renderFemale(entityIn, scale);
        }
    }

    public void setRotationAngle(final ModelRenderer modelRenderer, final float x, final float y, final float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    public void generateModel() {
        body = new ModelRenderer(this);
        body.setRotationPoint(0.0F, 0.0F, 0.0F);
        body.setTextureOffset(34, 8).addBox(-4.0F, 6.0F, -3.0F, 8, 12, 6);
        body.setTextureOffset(15, 10).addBox(-3.0F, 9.0F, 3.0F, 6, 8, 3);
        body.setTextureOffset(26, 0).addBox(-3.0F, 5.0F, -3.0F, 6, 1, 6);

        eye = new ModelRenderer(this);
        eye.setTextureOffset(0, 10).addBox(-3.0F, 7.0F, -4.0F, 6, 4, 1);

        left_leg = new ModelRenderer(this);
        left_leg.setRotationPoint(-2.0F, 18.0F, 0.0F);
        left_leg.setTextureOffset(0, 0).addBox(2.9F, 0.0F, -1.5F, 3, 6, 3, 0.0F);

        right_leg = new ModelRenderer(this);
        right_leg.setRotationPoint(2.0F, 18.0F, 0.0F);
        right_leg.setTextureOffset(13, 0).addBox(-5.9F, 0.0F, -1.5F, 3, 6, 3);

        rabbitBone = new ModelRenderer(this);
        rabbitBone.setRotationPoint(0.0F, 24.0F, 0.0F);
        rabbitBone.cubeList.add(new ModelBox(rabbitBone, 28, 45, -5.0F, -13.0F, -5.0F, 10, 11, 8, 0.0F, false));

        rabbitRleg = new ModelRenderer(this);
        rabbitRleg.setRotationPoint(-3.0F, -2.0F, -1.0F);
        rabbitBone.addChild(rabbitRleg);
        rabbitRleg.cubeList.add(new ModelBox(rabbitRleg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F, false));

        rabbitLarm = new ModelRenderer(this);
        rabbitLarm.setRotationPoint(5.0F, -13.0F, -1.0F);
        setRotationAngle(rabbitLarm, 0.0F, 0.0F, -0.0873F);
        rabbitBone.addChild(rabbitLarm);
        rabbitLarm.cubeList.add(new ModelBox(rabbitLarm, 0, 0, 0.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F, false));

        rabbitRarm = new ModelRenderer(this);
        rabbitRarm.setRotationPoint(-5.0F, -13.0F, -1.0F);
        setRotationAngle(rabbitRarm, 0.0F, 0.0F, 0.0873F);
        rabbitBone.addChild(rabbitRarm);
        rabbitRarm.cubeList.add(new ModelBox(rabbitRarm, 0, 0, -2.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F, false));

        rabbitLleg = new ModelRenderer(this);
        rabbitLleg.setRotationPoint(3.0F, -2.0F, -1.0F);
        rabbitBone.addChild(rabbitLleg);
        rabbitLleg.cubeList.add(new ModelBox(rabbitLleg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F, false));

        rabbitHead = new ModelRenderer(this);
        rabbitHead.setRotationPoint(0.0F, -14.0F, -1.0F);
        rabbitBone.addChild(rabbitHead);
        rabbitHead.cubeList.add(new ModelBox(rabbitHead, 0, 0, -3.0F, 0.0F, -4.0F, 6, 1, 6, 0.0F, false));
        rabbitHead.cubeList.add(new ModelBox(rabbitHead, 56, 0, -5.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, false));
        rabbitHead.cubeList.add(new ModelBox(rabbitHead, 56, 0, 3.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, true));
        rabbitHead.cubeList.add(new ModelBox(rabbitHead, 0, 45, -4.0F, -11.0F, -4.0F, 8, 11, 8, 0.0F, false));
        rabbitHead.cubeList.add(new ModelBox(rabbitHead, 46, 0, 1.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false));
        rabbitHead.cubeList.add(new ModelBox(rabbitHead, 46, 0, -4.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false));

        this.textureWidth = 100;
        this.textureHeight = 80;

        footRight = new ModelRenderer(this, 22, 39);
        footRight.setRotationPoint(0.0F, 8.0F, 0.0F);
        footRight.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
        setRotationAngle(footRight, -0.034906585F, 0.0F, 0.0F);

        earRight = new ModelRenderer(this, 8, 0);
        earRight.setRotationPoint(-4.5F, -5.5F, 0.0F);
        earRight.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
        setRotationAngle(earRight, 0.05235988F, 0.0F, -1.0471976F);

        legLeftpad = new ModelRenderer(this, 48, 39);
        legLeftpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        legLeftpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);

        earRightpad_1 = new ModelRenderer(this, 40, 39);
        earRightpad_1.setRotationPoint(0.0F, -1.0F, 0.0F);
        earRightpad_1.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);

        legLeft = new ModelRenderer(this, 54, 10);
        legLeft.setRotationPoint(3.3F, 12.5F, 0.0F);
        legLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);

        armRightpad2 = new ModelRenderer(this, 0, 26);
        armRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        armRightpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);

        handLeft = new ModelRenderer(this, 58, 56);
        handLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
        handLeft.addBox(-2.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
        setRotationAngle(handLeft, 0.0F, 0.0F, 0.05235988F);

        armLeft = new ModelRenderer(this, 62, 10);
        armLeft.setRotationPoint(6.5F, -8.0F, 0.0F);
        armLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
        setRotationAngle(armLeft, 0.0F, 0.0F, -0.2617994F);

        legRight = new ModelRenderer(this, 90, 8);
        legRight.setRotationPoint(-3.3F, 12.5F, 0.0F);
        legRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);

        armLeft2 = new ModelRenderer(this, 90, 48);
        armLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
        armLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        setRotationAngle(armLeft2, -0.17453292F, 0.0F, 0.0F);

        legRight2 = new ModelRenderer(this, 20, 35);
        legRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
        legRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        setRotationAngle(legRight2, 0.034906585F, 0.0F, 0.0F);

        armLeftpad2 = new ModelRenderer(this, 0, 58);
        armLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        armLeftpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);

        legLeft2 = new ModelRenderer(this, 72, 48);
        legLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
        legLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        setRotationAngle(legLeft2, 0.034906585F, 0.0F, 0.0F);

        hat = new ModelRenderer(this, 70, 24);
        hat.setRotationPoint(0.0F, -8.4F, 0.0F);
        hat.addBox(-3.0F, -0.5F, -3.0F, 6, 1, 6, 0.0F);
        setRotationAngle(hat, -0.017453292F, 0.0F, 0.0F);

        earRightpad = new ModelRenderer(this, 85, 0);
        earRightpad.setRotationPoint(0.0F, -1.0F, 0.0F);
        earRightpad.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);

        crotch = new ModelRenderer(this, 56, 0);
        crotch.setRotationPoint(0.0F, 9.5F, 0.0F);
        crotch.addBox(-5.5F, 0.0F, -3.5F, 11, 3, 7, 0.0F);

        torso = new ModelRenderer(this, 8, 0);
        torso.setRotationPoint(0.0F, 0.0F, 0.0F);
        torso.addBox(-6.0F, -9.0F, -4.0F, 12, 18, 8, 0.0F);
        setRotationAngle(torso, 0.017453292F, 0.0F, 0.0F);

        armRight2 = new ModelRenderer(this, 90, 20);
        armRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
        armRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        setRotationAngle(armRight2, -0.17453292F, 0.0F, 0.0F);

        handRight = new ModelRenderer(this, 20, 26);
        handRight.setRotationPoint(0.0F, 8.0F, 0.0F);
        handRight.addBox(-2.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
        setRotationAngle(handRight, 0.0F, 0.0F, -0.05235988F);

        fredbody = new ModelRenderer(this, 0, 0);
        fredbody.setRotationPoint(0.0F, -9.0F, 0.0F);
        fredbody.addBox(-1.0F, -14.0F, -1.0F, 2, 24, 2, 0.0F);

        fredhead = new ModelRenderer(this, 39, 22);
        fredhead.setRotationPoint(0.0F, -13.0F, -0.5F);
        fredhead.addBox(-5.5F, -8.0F, -4.5F, 11, 8, 9, 0.0F);

        legRightpad = new ModelRenderer(this, 73, 33);
        legRightpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        legRightpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);

        frednose = new ModelRenderer(this, 17, 67);
        frednose.setRotationPoint(0.0F, -2.0F, -4.5F);
        frednose.addBox(-4.0F, -2.0F, -3.0F, 8, 4, 3, 0.0F);

        legLeftpad2 = new ModelRenderer(this, 16, 50);
        legLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        legLeftpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);

        armRightpad = new ModelRenderer(this, 70, 10);
        armRightpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        armRightpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);

        armLeftpad = new ModelRenderer(this, 38, 54);
        armLeftpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        armLeftpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);

        hat2 = new ModelRenderer(this, 78, 61);
        hat2.setRotationPoint(0.0F, 0.1F, 0.0F);
        hat2.addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4, 0.0F);
        setRotationAngle(hat2, -0.017453292F, 0.0F, 0.0F);

        legRightpad2 = new ModelRenderer(this, 0, 39);
        legRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        legRightpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);

        jaw = new ModelRenderer(this, 49, 65);
        jaw.setRotationPoint(0.0F, 0.5F, 0.0F);
        jaw.addBox(-5.0F, 0.0F, -4.5F, 10, 3, 9, 0.0F);
        setRotationAngle(jaw, 0.08726646F, 0.0F, 0.0F);

        armRight = new ModelRenderer(this, 48, 0);
        armRight.setRotationPoint(-6.5F, -8.0F, 0.0F);
        armRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
        setRotationAngle(armRight, 0.0F, 0.0F, 0.2617994F);

        footLeft = new ModelRenderer(this, 72, 50);
        footLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
        footLeft.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
        setRotationAngle(footLeft, -0.034906585F, 0.0F, 0.0F);

        earLeft = new ModelRenderer(this, 40, 0);
        earLeft.setRotationPoint(4.5F, -5.5F, 0.0F);
        earLeft.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
        setRotationAngle(earLeft, 0.05235988F, 0.0F, 1.0471976F);

        legRight2.addChild(footRight);
        fredhead.addChild(earRight);
        legLeft.addChild(legLeftpad);
        earLeft.addChild(earRightpad_1);
        fredbody.addChild(legLeft);
        armRight2.addChild(armRightpad2);
        armLeft2.addChild(handLeft);
        fredbody.addChild(armLeft);
        fredbody.addChild(legRight);
        armLeft.addChild(armLeft2);
        legRight.addChild(legRight2);
        armLeft2.addChild(armLeftpad2);
        legLeft.addChild(legLeft2);
        fredhead.addChild(hat);
        earRight.addChild(earRightpad);
        fredbody.addChild(crotch);
        fredbody.addChild(torso);
        armRight.addChild(armRight2);
        armRight2.addChild(handRight);
        fredbody.addChild(fredhead);
        legRight.addChild(legRightpad);
        fredhead.addChild(frednose);
        legLeft2.addChild(legLeftpad2);
        armRight.addChild(armRightpad);
        armLeft.addChild(armLeftpad);
        hat.addChild(hat2);
        legRight2.addChild(legRightpad2);
        fredhead.addChild(jaw);
        fredbody.addChild(armRight);
        legLeft2.addChild(footLeft);
        fredhead.addChild(earLeft);
    }

    public void renderCustom(final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
                             final float netHeadYaw, final float headPitch, final float scale) {
        if (left_leg == null) {
            generateModel();
        }

        final CustomModel customModel = CustomModel.INSTANCE;
        GlStateManager.pushMatrix();

        if (customModel.getState() && customModel.getMode().contains("Rabbit")) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.25D, 1.25D, 1.25D);
            GlStateManager.translate(0.0D, -0.3D, 0.0D);
            rabbitHead.rotateAngleX = bipedHead.rotateAngleX;
            rabbitHead.rotateAngleY = bipedHead.rotateAngleY;
            rabbitHead.rotateAngleZ = bipedHead.rotateAngleZ;
            rabbitLarm.rotateAngleX = bipedLeftArm.rotateAngleX;
            rabbitLarm.rotateAngleY = bipedLeftArm.rotateAngleY;
            rabbitLarm.rotateAngleZ = bipedLeftArm.rotateAngleZ;
            rabbitRarm.rotateAngleX = bipedRightArm.rotateAngleX;
            rabbitRarm.rotateAngleY = bipedRightArm.rotateAngleY;
            rabbitRarm.rotateAngleZ = bipedRightArm.rotateAngleZ;
            rabbitRleg.rotateAngleX = bipedRightLeg.rotateAngleX;
            rabbitRleg.rotateAngleY = bipedRightLeg.rotateAngleY;
            rabbitRleg.rotateAngleZ = bipedRightLeg.rotateAngleZ;
            rabbitLleg.rotateAngleX = bipedLeftLeg.rotateAngleX;
            rabbitLleg.rotateAngleY = bipedLeftLeg.rotateAngleY;
            rabbitLleg.rotateAngleZ = bipedLeftLeg.rotateAngleZ;
            rabbitBone.render(scale);
            GlStateManager.popMatrix();
        } else if (customModel.getState() && customModel.getMode().contains("Freddy")) {
            fredhead.rotateAngleX = bipedHead.rotateAngleX;
            fredhead.rotateAngleY = bipedHead.rotateAngleY;
            fredhead.rotateAngleZ = bipedHead.rotateAngleZ;
            armLeft.rotateAngleX = bipedLeftArm.rotateAngleX;
            armLeft.rotateAngleY = bipedLeftArm.rotateAngleY;
            armLeft.rotateAngleZ = bipedLeftArm.rotateAngleZ;
            legRight.rotateAngleX = bipedRightLeg.rotateAngleX;
            legRight.rotateAngleY = bipedRightLeg.rotateAngleY;
            legRight.rotateAngleZ = bipedRightLeg.rotateAngleZ;
            legLeft.rotateAngleX = bipedLeftLeg.rotateAngleX;
            legLeft.rotateAngleY = bipedLeftLeg.rotateAngleY;
            legLeft.rotateAngleZ = bipedLeftLeg.rotateAngleZ;
            armRight.rotateAngleX = bipedRightArm.rotateAngleX;
            armRight.rotateAngleY = bipedRightArm.rotateAngleY;
            armRight.rotateAngleZ = bipedRightArm.rotateAngleZ;

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75, 0.65, 0.75);
            GlStateManager.translate(0.0, 0.85, 0.0);
            fredbody.render(scale);
            GlStateManager.popMatrix();
        } else if (customModel.getState() && customModel.getMode().contains("Imposter")) {
            bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
            bipedHead.rotateAngleX = headPitch * 0.017453292F;
            bipedBody.rotateAngleY = 0.0F;
            final float f = 1.0F;
            right_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
            left_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount / f;
            right_leg.rotateAngleY = 0.0F;
            left_leg.rotateAngleY = 0.0F;
            right_leg.rotateAngleZ = 0.0F;
            left_leg.rotateAngleZ = 0.0F;

            int bodyCustomColor = new Color(197, 16, 17).getRGB();
            int eyeCustomColor = new Color(254, 254, 254).getRGB();
            int legsCustomColor = new Color(122, 7, 56).getRGB();

            if (this.isChild) {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
                body.render(scale);
                left_leg.render(scale);
                right_leg.render(scale);
            } else {
                GlStateManager.translate(0.0D, -0.8D, 0.0D);
                GlStateManager.scale(1.8D, 1.6D, 1.6D);
                RenderUtils.INSTANCE.color(bodyCustomColor);
                GlStateManager.translate(0.0D, 0.15D, 0.0D);
                body.render(scale);
                RenderUtils.INSTANCE.color(eyeCustomColor);
                eye.render(scale);
                RenderUtils.INSTANCE.color(legsCustomColor);
                GlStateManager.translate(0.0D, -0.15D, 0.0D);
                left_leg.render(scale);
                right_leg.render(scale);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        if (customModel.getState() && customModel.getMode().contains("Female")) {
            renderFemale(entityIn, scale);
        }

        GlStateManager.popMatrix();
    }

    private boolean bindChestArmorTexture(EntityPlayer player) {
        ItemStack chest = player.inventory.armorInventory[2];
        if (chest != null && chest.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) chest.getItem();
            String mat = armor.getArmorMaterial().getName().toLowerCase();
            ResourceLocation tex = new ResourceLocation("textures/models/armor/" + mat + "_layer_1.png");
            Minecraft.getMinecraft().getTextureManager().bindTexture(tex);

            if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
                int c = armor.getColor(chest);
                float r = (float)(c >> 16 & 255) / 255.0F;
                float g = (float)(c >>  8 & 255) / 255.0F;
                float b = (float)(c       & 255) / 255.0F;
                GlStateManager.color(r, g, b, 1.0F);
            } else {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

    private void rebindPlayerSkin(AbstractClientPlayer player) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(player.getLocationSkin());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderFemale(final Entity entityIn, final float scale) {
        if (!(entityIn instanceof EntityPlayer)) return;

        final EntityPlayer player = (EntityPlayer) entityIn;

        if (CustomModel.INSTANCE.getBreastNoArmor()) {
            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack != null) return;
            }
        }

        if (bT1 == null) {
            bT1 = new ModelRenderer(this, 20, 20); bT1.addBox(-3.0F, 1.0F, -3.0F, 6, 1, 1, 0.0F);
            bT2 = new ModelRenderer(this, 19, 21); bT2.addBox(-4.0F, 2.0F, -3.0F, 8, 3, 1, 0.0F);
            bF1 = new ModelRenderer(this, 20, 21); bF1.addBox(-3.0F, 2.0F, -4.0F, 6, 1, 1, 0.0F);
            bF2 = new ModelRenderer(this, 19, 22); bF2.addBox(-4.0F, 3.0F, -4.0F, 8, 1, 1, 0.0F);
            bF3 = new ModelRenderer(this, 20, 23); bF3.addBox(-3.0F, 4.0F, -4.0F, 2, 1, 1, 0.0F);
            bF4 = new ModelRenderer(this, 23, 23); bF4.addBox( 1.0F, 4.0F, -4.0F, 2, 1, 1, 0.0F);
        }

        final float rotationDegrees = CustomModel.INSTANCE.getBreastRotation();

        boolean usingArmorTex = bindChestArmorTexture(player);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(rotationDegrees, 1.0F, 0.0F, 0.0F);

        if (!CustomModel.INSTANCE.getBreastPhysics()) {

            bT1.rotationPointY = 0.0F;
            bT2.rotationPointY = 0.0F;
            bF1.rotationPointY = 0.0F;
            bF2.rotationPointY = 0.0F;
            bF3.rotationPointY = 0.0F;
            bF4.rotationPointY = 0.0F;

            bT1.render(scale);
            bT2.render(scale);
            bF1.render(scale);
            bF2.render(scale);
            bF3.render(scale);
            bF4.render(scale);
        } else {
            final float gravity = CustomModel.INSTANCE.getBreastGravity();
            final float bounce  = CustomModel.INSTANCE.getBreastBounce();

            breastOffsetT1 += (0 - breastOffsetT1) * gravity;
            breastOffsetT2 += (0 - breastOffsetT2) * gravity;
            breastOffsetF1 += (0 - breastOffsetF1) * gravity;
            breastOffsetF2 += (0 - breastOffsetF2) * gravity;
            breastOffsetF3 += (0 - breastOffsetF3) * gravity;
            breastOffsetF4 += (0 - breastOffsetF4) * gravity;

            if (entityIn.motionY > 0.0D) {
                float jumpForce = (float) entityIn.motionY * 0.5F;
                breastOffsetT1 -= jumpForce * bounce;
                breastOffsetT2 -= jumpForce * bounce;
                breastOffsetF1 -= jumpForce * bounce;
                breastOffsetF2 -= jumpForce * bounce;
                breastOffsetF3 -= jumpForce * bounce;
                breastOffsetF4 -= jumpForce * bounce;
            }

            bT1.rotationPointY = breastOffsetT1; bT1.render(scale);
            bT2.rotationPointY = breastOffsetT2; bT2.render(scale);
            bF1.rotationPointY = breastOffsetF1; bF1.render(scale);
            bF2.rotationPointY = breastOffsetF2; bF2.render(scale);
            bF3.rotationPointY = breastOffsetF3; bF3.render(scale);
            bF4.rotationPointY = breastOffsetF4; bF4.render(scale);
        }

        GlStateManager.popMatrix();

        if (usingArmorTex && player instanceof AbstractClientPlayer) {
            rebindPlayerSkin((AbstractClientPlayer) player);
        }
    }

    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    private void revertSwordAnimation(final float p1, final float p2, final float p3, final float p4,
                                      final float p5, final float p6, final Entity entity, final CallbackInfo callbackInfo) {
        EventManager.INSTANCE.call(new UpdateModelEvent((EntityPlayer) entity, (ModelPlayer) (Object) this));
    }
}