/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.UpdateModelEvent;
import net.ccbluex.liquidbounce.features.module.modules.visual.CustomModel;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Objects;

/**
 * The type Mixin model player fix.
 */
@Mixin(ModelPlayer.class)
public class MixinModelPlayerFix extends ModelBiped {

    /**
     * The Left leg.
     */
    @Unique
    public ModelRenderer left_leg;
    /**
     * The Right leg.
     */
    @Unique
    public ModelRenderer right_leg;
    /**
     * The Body.
     */
    @Unique
    public ModelRenderer fDPClient$body;
    /**
     * The Eye.
     */
    @Unique
    public ModelRenderer fDPClient$eye;
    /**
     * The Rabbit bone.
     */
    @Unique
    public ModelRenderer fDPClient$rabbitBone;
    /**
     * The Rabbit rleg.
     */
    @Unique
    public ModelRenderer fDPClient$rabbitRleg;
    /**
     * The Rabbit larm.
     */
    @Unique
    public ModelRenderer fDPClient$rabbitLarm;
    /**
     * The Rabbit rarm.
     */
    @Unique
    public ModelRenderer fDPClient$rabbitRarm;
    /**
     * The Rabbit lleg.
     */
    @Unique
    public ModelRenderer fDPClient$rabbitLleg;
    /**
     * The Rabbit head.
     */
    @Unique
    public ModelRenderer fDPClient$rabbitHead;
    /**
     * The Fredhead.
     */
    @Unique
    public ModelRenderer fDPClient$fredhead;
    /**
     * The Arm left.
     */
    @Unique
    public ModelRenderer fDPClient$armLeft;
    /**
     * The Leg right.
     */
    @Unique
    public ModelRenderer fDPClient$legRight;
    /**
     * The Leg left.
     */
    @Unique
    public ModelRenderer fDPClient$legLeft;
    /**
     * The Arm right.
     */
    @Unique
    public ModelRenderer fDPClient$armRight;
    /**
     * The Fredbody.
     */
    @Unique
    public ModelRenderer fDPClient$fredbody;
    /**
     * The Arm leftpad 2.
     */
    @Unique
    public ModelRenderer fDPClient$armLeftpad2;
    /**
     * The Torso.
     */
    @Unique
    public ModelRenderer fDPClient$torso;
    /**
     * The Ear rightpad 1.
     */
    @Unique
    public ModelRenderer earRightpad_1;
    /**
     * The Arm rightpad 2.
     */
    @Unique
    public ModelRenderer fDPClient$armRightpad2;
    /**
     * The Leg leftpad.
     */
    @Unique
    public ModelRenderer fDPClient$legLeftpad;
    /**
     * The Hat.
     */
    public ModelRenderer fDPClient$hat;
    /**
     * The Leg leftpad 2.
     */
    public ModelRenderer fDPClient$legLeftpad2;
    /**
     * The Arm right 2.
     */
    public ModelRenderer fDPClient$armRight2;
    /**
     * The Leg right 2.
     */
    public ModelRenderer fDPClient$legRight2;
    /**
     * The Ear rightpad.
     */
    public ModelRenderer fDPClient$earRightpad;
    /**
     * The Arm left 2.
     */
    public ModelRenderer fDPClient$armLeft2;
    /**
     * The Frednose.
     */
    public ModelRenderer fDPClient$frednose;
    /**
     * The Ear left.
     */
    public ModelRenderer fDPClient$earLeft;
    /**
     * The Foot right.
     */
    public ModelRenderer fDPClient$footRight;
    /**
     * The Leg rightpad 2.
     */
    public ModelRenderer fDPClient$legRightpad2;
    /**
     * The Leg rightpad.
     */
    public ModelRenderer fDPClient$legRightpad;
    /**
     * The Arm leftpad.
     */
    public ModelRenderer fDPClient$armLeftpad;
    /**
     * The Leg left 2.
     */
    public ModelRenderer fDPClient$legLeft2;
    /**
     * The Foot left.
     */
    public ModelRenderer fDPClient$footLeft;
    /**
     * The Hat 2.
     */
    public ModelRenderer fDPClient$hat2;
    /**
     * The Arm rightpad.
     */
    public ModelRenderer fDPClient$armRightpad;
    /**
     * The Ear right.
     */
    public ModelRenderer fDPClient$earRight;
    /**
     * The Crotch.
     */
    public ModelRenderer fDPClient$crotch;
    /**
     * The Jaw.
     */
    public ModelRenderer fDPClient$jaw;
    /**
     * The Hand right.
     */
    public ModelRenderer fDPClient$handRight;
    /**
     * The Hand left.
     */
    public ModelRenderer fDPClient$handLeft;
    @Shadow
    private boolean smallArms;
    /**
     * The Biped left armwear.
     */
    @Shadow
    public ModelRenderer bipedLeftArmwear;

    /**
     * The Biped right armwear.
     */
    @Shadow
    public ModelRenderer bipedRightArmwear;

    /**
     * The Biped left legwear.
     */
    @Shadow
    public ModelRenderer bipedLeftLegwear;

    /**
     * The Biped right legwear.
     */
    @Shadow
    public ModelRenderer bipedRightLegwear;

    /**
     * The Biped body wear.
     */
    @Shadow
    public ModelRenderer bipedBodyWear;

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 2.5F))
    private float fixAlexArmHeight(float original) {
        return 2F;
    }

    /**
     * @author As_pw
     * @reason PostRender
     */
    @Overwrite
    public void postRenderArm(float scale) {
        if (this.smallArms) {
            this.bipedRightArm.rotationPointX += 0.5F;
            this.bipedRightArm.postRender(scale);
            this.bipedRightArm.rotationPointZ -= 0.5F;
        } else {
            this.bipedRightArm.postRender(scale);
        }
    }

    /**
     * Render hook.
     *
     * @param entityIn        the entity in
     * @param limbSwing       the limb swing
     * @param limbSwingAmount the limb swing amount
     * @param ageInTicks      the age in ticks
     * @param netHeadYaw      the net head yaw
     * @param headPitch       the head pitch
     * @param scale           the scale
     * @param ci              the ci
     */
    @Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
    public void renderHook(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        CustomModel customModel = Objects.requireNonNull(FDPClient.moduleManager.getModule(CustomModel.class));
        if (customModel.getState()) {
            ci.cancel();
            fDPClient$renderCustom(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    /**
     * Sets rotation angle.
     *
     * @param modelRenderer the model renderer
     * @param x             the x
     * @param y             the y
     * @param z             the z
     */
    @Unique
    public void fDPClient$setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    /**
     * Generatemodel.
     */
    @Unique
    public void fDPClient$generatemodel() {
        fDPClient$body = new ModelRenderer(this);
        fDPClient$body.setRotationPoint(0.0F, 0.0F, 0.0F);
        fDPClient$body.setTextureOffset(34, 8).addBox(-4.0F, 6.0F, -3.0F, 8, 12, 6);
        fDPClient$body.setTextureOffset(15, 10).addBox(-3.0F, 9.0F, 3.0F, 6, 8, 3);
        fDPClient$body.setTextureOffset(26, 0).addBox(-3.0F, 5.0F, -3.0F, 6, 1, 6);
        fDPClient$eye = new ModelRenderer(this);
        fDPClient$eye.setTextureOffset(0, 10).addBox(-3.0F, 7.0F, -4.0F, 6, 4, 1);
        left_leg = new ModelRenderer(this);
        left_leg.setRotationPoint(-2.0F, 18.0F, 0.0F);
        left_leg.setTextureOffset(0, 0).addBox(2.9F, 0.0F, -1.5F, 3, 6, 3, 0.0F);
        right_leg = new ModelRenderer(this);
        right_leg.setRotationPoint(2.0F, 18.0F, 0.0F);
        right_leg.setTextureOffset(13, 0).addBox(-5.9F, 0.0F, -1.5F, 3, 6, 3);
        (this.fDPClient$rabbitBone = new ModelRenderer(this)).setRotationPoint(0.0F, 24.0F, 0.0F);
        this.fDPClient$rabbitBone.cubeList.add(new ModelBox(this.fDPClient$rabbitBone, 28, 45, -5.0F, -13.0F, -5.0F, 10, 11, 8, 0.0F, false));
        (this.fDPClient$rabbitRleg = new ModelRenderer(this)).setRotationPoint(-3.0F, -2.0F, -1.0F);
        this.fDPClient$rabbitBone.addChild(this.fDPClient$rabbitRleg);
        this.fDPClient$rabbitRleg.cubeList.add(new ModelBox(this.fDPClient$rabbitRleg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F, false));
        (this.fDPClient$rabbitLarm = new ModelRenderer(this)).setRotationPoint(5.0F, -13.0F, -1.0F);
        fDPClient$setRotationAngle(this.fDPClient$rabbitLarm, 0.0F, 0.0F, -0.0873F);
        this.fDPClient$rabbitBone.addChild(this.fDPClient$rabbitLarm);
        this.fDPClient$rabbitLarm.cubeList.add(new ModelBox(this.fDPClient$rabbitLarm, 0, 0, 0.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F, false));
        (this.fDPClient$rabbitRarm = new ModelRenderer(this)).setRotationPoint(-5.0F, -13.0F, -1.0F);
        fDPClient$setRotationAngle(this.fDPClient$rabbitRarm, 0.0F, 0.0F, 0.0873F);
        this.fDPClient$rabbitBone.addChild(this.fDPClient$rabbitRarm);
        this.fDPClient$rabbitRarm.cubeList.add(new ModelBox(this.fDPClient$rabbitRarm, 0, 0, -2.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F, false));
        (this.fDPClient$rabbitLleg = new ModelRenderer(this)).setRotationPoint(3.0F, -2.0F, -1.0F);
        this.fDPClient$rabbitBone.addChild(this.fDPClient$rabbitLleg);
        this.fDPClient$rabbitLleg.cubeList.add(new ModelBox(this.fDPClient$rabbitLleg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F, false));
        (this.fDPClient$rabbitHead = new ModelRenderer(this)).setRotationPoint(0.0F, -14.0F, -1.0F);
        this.fDPClient$rabbitBone.addChild(this.fDPClient$rabbitHead);
        this.fDPClient$rabbitHead.cubeList.add(new ModelBox(this.fDPClient$rabbitHead, 0, 0, -3.0F, 0.0F, -4.0F, 6, 1, 6, 0.0F, false));
        this.fDPClient$rabbitHead.cubeList.add(new ModelBox(this.fDPClient$rabbitHead, 56, 0, -5.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, false));
        this.fDPClient$rabbitHead.cubeList.add(new ModelBox(this.fDPClient$rabbitHead, 56, 0, 3.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, true));
        this.fDPClient$rabbitHead.cubeList.add(new ModelBox(this.fDPClient$rabbitHead, 0, 45, -4.0F, -11.0F, -4.0F, 8, 11, 8, 0.0F, false));
        this.fDPClient$rabbitHead.cubeList.add(new ModelBox(this.fDPClient$rabbitHead, 46, 0, 1.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false));
        this.fDPClient$rabbitHead.cubeList.add(new ModelBox(this.fDPClient$rabbitHead, 46, 0, -4.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false));
        this.textureWidth = 100;
        this.textureHeight = 80;
        final ModelRenderer footRight = new ModelRenderer(this, 22, 39);
        this.fDPClient$footRight = footRight;
        footRight.setRotationPoint(0.0f, 8.0f, 0.0f);
        this.fDPClient$footRight.addBox(-2.5f, 0.0f, -6.0f, 5, 3, 8, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$footRight, -0.034906585f, 0.0f, 0.0f);
        final ModelRenderer earRight = new ModelRenderer(this, 8, 0);
        this.fDPClient$earRight = earRight;
        earRight.setRotationPoint(-4.5f, -5.5f, 0.0f);
        this.fDPClient$earRight.addBox(-1.0f, -3.0f, -0.5f, 2, 3, 1, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$earRight, 0.05235988f, 0.0f, -1.0471976f);
        final ModelRenderer legLeftpad = new ModelRenderer(this, 48, 39);
        this.fDPClient$legLeftpad = legLeftpad;
        legLeftpad.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$legLeftpad.addBox(-3.0f, 0.0f, -3.0f, 6, 9, 6, 0.0f);
        final ModelRenderer earRightpad_1 = new ModelRenderer(this, 40, 39);
        this.earRightpad_1 = earRightpad_1;
        earRightpad_1.setRotationPoint(0.0f, -1.0f, 0.0f);
        this.earRightpad_1.addBox(-2.0f, -5.0f, -1.0f, 4, 4, 2, 0.0f);
        final ModelRenderer legLeft = new ModelRenderer(this, 54, 10);
        this.fDPClient$legLeft = legLeft;
        legLeft.setRotationPoint(3.3f, 12.5f, 0.0f);
        this.fDPClient$legLeft.addBox(-1.0f, 0.0f, -1.0f, 2, 10, 2, 0.0f);
        final ModelRenderer armRightpad2 = new ModelRenderer(this, 0, 26);
        this.fDPClient$armRightpad2 = armRightpad2;
        armRightpad2.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$armRightpad2.addBox(-2.5f, 0.0f, -2.5f, 5, 7, 5, 0.0f);
        final ModelRenderer handLeft = new ModelRenderer(this, 58, 56);
        this.fDPClient$handLeft = handLeft;
        handLeft.setRotationPoint(0.0f, 8.0f, 0.0f);
        this.fDPClient$handLeft.addBox(-2.0f, 0.0f, -2.5f, 4, 4, 5, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$handLeft, 0.0f, 0.0f, 0.05235988f);
        final ModelRenderer armLeft = new ModelRenderer(this, 62, 10);
        this.fDPClient$armLeft = armLeft;
        armLeft.setRotationPoint(6.5f, -8.0f, 0.0f);
        this.fDPClient$armLeft.addBox(-1.0f, 0.0f, -1.0f, 2, 10, 2, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$armLeft, 0.0f, 0.0f, -0.2617994f);
        final ModelRenderer legRight = new ModelRenderer(this, 90, 8);
        this.fDPClient$legRight = legRight;
        legRight.setRotationPoint(-3.3f, 12.5f, 0.0f);
        this.fDPClient$legRight.addBox(-1.0f, 0.0f, -1.0f, 2, 10, 2, 0.0f);
        final ModelRenderer armLeft2 = new ModelRenderer(this, 90, 48);
        this.fDPClient$armLeft2 = armLeft2;
        armLeft2.setRotationPoint(0.0f, 9.6f, 0.0f);
        this.fDPClient$armLeft2.addBox(-1.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$armLeft2, -0.17453292f, 0.0f, 0.0f);
        final ModelRenderer legRight2 = new ModelRenderer(this, 20, 35);
        this.fDPClient$legRight2 = legRight2;
        legRight2.setRotationPoint(0.0f, 9.6f, 0.0f);
        this.fDPClient$legRight2.addBox(-1.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$legRight2, 0.034906585f, 0.0f, 0.0f);
        final ModelRenderer armLeftpad2 = new ModelRenderer(this, 0, 58);
        this.fDPClient$armLeftpad2 = armLeftpad2;
        armLeftpad2.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$armLeftpad2.addBox(-2.5f, 0.0f, -2.5f, 5, 7, 5, 0.0f);
        final ModelRenderer legLeft2 = new ModelRenderer(this, 72, 48);
        this.fDPClient$legLeft2 = legLeft2;
        legLeft2.setRotationPoint(0.0f, 9.6f, 0.0f);
        this.fDPClient$legLeft2.addBox(-1.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$legLeft2, 0.034906585f, 0.0f, 0.0f);
        final ModelRenderer hat = new ModelRenderer(this, 70, 24);
        this.fDPClient$hat = hat;
        hat.setRotationPoint(0.0f, -8.4f, 0.0f);
        this.fDPClient$hat.addBox(-3.0f, -0.5f, -3.0f, 6, 1, 6, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$hat, -0.017453292f, 0.0f, 0.0f);
        final ModelRenderer earRightpad = new ModelRenderer(this, 85, 0);
        this.fDPClient$earRightpad = earRightpad;
        earRightpad.setRotationPoint(0.0f, -1.0f, 0.0f);
        this.fDPClient$earRightpad.addBox(-2.0f, -5.0f, -1.0f, 4, 4, 2, 0.0f);
        final ModelRenderer crotch = new ModelRenderer(this, 56, 0);
        this.fDPClient$crotch = crotch;
        crotch.setRotationPoint(0.0f, 9.5f, 0.0f);
        this.fDPClient$crotch.addBox(-5.5f, 0.0f, -3.5f, 11, 3, 7, 0.0f);
        final ModelRenderer torso = new ModelRenderer(this, 8, 0);
        this.fDPClient$torso = torso;
        torso.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.fDPClient$torso.addBox(-6.0f, -9.0f, -4.0f, 12, 18, 8, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$torso, 0.017453292f, 0.0f, 0.0f);
        final ModelRenderer armRight2 = new ModelRenderer(this, 90, 20);
        this.fDPClient$armRight2 = armRight2;
        armRight2.setRotationPoint(0.0f, 9.6f, 0.0f);
        this.fDPClient$armRight2.addBox(-1.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$armRight2, -0.17453292f, 0.0f, 0.0f);
        final ModelRenderer handRight = new ModelRenderer(this, 20, 26);
        this.fDPClient$handRight = handRight;
        handRight.setRotationPoint(0.0f, 8.0f, 0.0f);
        this.fDPClient$handRight.addBox(-2.0f, 0.0f, -2.5f, 4, 4, 5, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$handRight, 0.0f, 0.0f, -0.05235988f);
        final ModelRenderer fredbody = new ModelRenderer(this, 0, 0);
        this.fDPClient$fredbody = fredbody;
        fredbody.setRotationPoint(0.0f, -9.0f, 0.0f);
        this.fDPClient$fredbody.addBox(-1.0f, -14.0f, -1.0f, 2, 24, 2, 0.0f);
        final ModelRenderer fredhead = new ModelRenderer(this, 39, 22);
        this.fDPClient$fredhead = fredhead;
        fredhead.setRotationPoint(0.0f, -13.0f, -0.5f);
        this.fDPClient$fredhead.addBox(-5.5f, -8.0f, -4.5f, 11, 8, 9, 0.0f);
        final ModelRenderer legRightpad = new ModelRenderer(this, 73, 33);
        this.fDPClient$legRightpad = legRightpad;
        legRightpad.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$legRightpad.addBox(-3.0f, 0.0f, -3.0f, 6, 9, 6, 0.0f);
        final ModelRenderer frednose = new ModelRenderer(this, 17, 67);
        this.fDPClient$frednose = frednose;
        frednose.setRotationPoint(0.0f, -2.0f, -4.5f);
        this.fDPClient$frednose.addBox(-4.0f, -2.0f, -3.0f, 8, 4, 3, 0.0f);
        final ModelRenderer legLeftpad2 = new ModelRenderer(this, 16, 50);
        this.fDPClient$legLeftpad2 = legLeftpad2;
        legLeftpad2.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$legLeftpad2.addBox(-2.5f, 0.0f, -3.0f, 5, 7, 6, 0.0f);
        final ModelRenderer armRightpad3 = new ModelRenderer(this, 70, 10);
        this.fDPClient$armRightpad = armRightpad3;
        armRightpad3.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$armRightpad.addBox(-2.5f, 0.0f, -2.5f, 5, 9, 5, 0.0f);
        final ModelRenderer armLeftpad3 = new ModelRenderer(this, 38, 54);
        this.fDPClient$armLeftpad = armLeftpad3;
        armLeftpad3.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$armLeftpad.addBox(-2.5f, 0.0f, -2.5f, 5, 9, 5, 0.0f);
        final ModelRenderer hat2 = new ModelRenderer(this, 78, 61);
        this.fDPClient$hat2 = hat2;
        hat2.setRotationPoint(0.0f, 0.1f, 0.0f);
        this.fDPClient$hat2.addBox(-2.0f, -4.0f, -2.0f, 4, 4, 4, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$hat2, -0.017453292f, 0.0f, 0.0f);
        final ModelRenderer legRightpad2 = new ModelRenderer(this, 0, 39);
        this.fDPClient$legRightpad2 = legRightpad2;
        legRightpad2.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$legRightpad2.addBox(-2.5f, 0.0f, -3.0f, 5, 7, 6, 0.0f);
        final ModelRenderer jaw = new ModelRenderer(this, 49, 65);
        this.fDPClient$jaw = jaw;
        jaw.setRotationPoint(0.0f, 0.5f, 0.0f);
        this.fDPClient$jaw.addBox(-5.0f, 0.0f, -4.5f, 10, 3, 9, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$jaw, 0.08726646f, 0.0f, 0.0f);
        final ModelRenderer armRight3 = new ModelRenderer(this, 48, 0);
        this.fDPClient$armRight = armRight3;
        armRight3.setRotationPoint(-6.5f, -8.0f, 0.0f);
        this.fDPClient$armRight.addBox(-1.0f, 0.0f, -1.0f, 2, 10, 2, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$armRight, 0.0f, 0.0f, 0.2617994f);
        final ModelRenderer footLeft = new ModelRenderer(this, 72, 50);
        this.fDPClient$footLeft = footLeft;
        footLeft.setRotationPoint(0.0f, 8.0f, 0.0f);
        this.fDPClient$footLeft.addBox(-2.5f, 0.0f, -6.0f, 5, 3, 8, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$footLeft, -0.034906585f, 0.0f, 0.0f);
        final ModelRenderer earLeft = new ModelRenderer(this, 40, 0);
        this.fDPClient$earLeft = earLeft;
        earLeft.setRotationPoint(4.5f, -5.5f, 0.0f);
        this.fDPClient$earLeft.addBox(-1.0f, -3.0f, -0.5f, 2, 3, 1, 0.0f);
        this.fDPClient$setRotationAngle(this.fDPClient$earLeft, 0.05235988f, 0.0f, 1.0471976f);
        this.fDPClient$legRight2.addChild(this.fDPClient$footRight);
        this.fDPClient$fredhead.addChild(this.fDPClient$earRight);
        this.fDPClient$legLeft.addChild(this.fDPClient$legLeftpad);
        this.fDPClient$earLeft.addChild(this.earRightpad_1);
        this.fDPClient$fredbody.addChild(this.fDPClient$legLeft);
        this.fDPClient$armRight2.addChild(this.fDPClient$armRightpad2);
        this.fDPClient$armLeft2.addChild(this.fDPClient$handLeft);
        this.fDPClient$fredbody.addChild(this.fDPClient$armLeft);
        this.fDPClient$fredbody.addChild(this.fDPClient$legRight);
        this.fDPClient$armLeft.addChild(this.fDPClient$armLeft2);
        this.fDPClient$legRight.addChild(this.fDPClient$legRight2);
        this.fDPClient$armLeft2.addChild(this.fDPClient$armLeftpad2);
        this.fDPClient$legLeft.addChild(this.fDPClient$legLeft2);
        this.fDPClient$fredhead.addChild(this.fDPClient$hat);
        this.fDPClient$earRight.addChild(this.fDPClient$earRightpad);
        this.fDPClient$fredbody.addChild(this.fDPClient$crotch);
        this.fDPClient$fredbody.addChild(this.fDPClient$torso);
        this.fDPClient$armRight.addChild(this.fDPClient$armRight2);
        this.fDPClient$armRight2.addChild(this.fDPClient$handRight);
        this.fDPClient$fredbody.addChild(this.fDPClient$fredhead);
        this.fDPClient$legRight.addChild(this.fDPClient$legRightpad);
        this.fDPClient$fredhead.addChild(this.fDPClient$frednose);
        this.fDPClient$legLeft2.addChild(this.fDPClient$legLeftpad2);
        this.fDPClient$armRight.addChild(this.fDPClient$armRightpad);
        this.fDPClient$armLeft.addChild(this.fDPClient$armLeftpad);
        this.fDPClient$hat.addChild(this.fDPClient$hat2);
        this.fDPClient$legRight2.addChild(this.fDPClient$legRightpad2);
        this.fDPClient$fredhead.addChild(this.fDPClient$jaw);
        this.fDPClient$fredbody.addChild(this.fDPClient$armRight);
        this.fDPClient$legLeft2.addChild(this.fDPClient$footLeft);
        this.fDPClient$fredhead.addChild(this.fDPClient$earLeft);
    }

    /**
     * Render custom.
     *
     * @param entityIn        the entity in
     * @param limbSwing       the limb swing
     * @param limbSwingAmount the limb swing amount
     * @param ageInTicks      the age in ticks
     * @param netHeadYaw      the net head yaw
     * @param headPitch       the head pitch
     * @param scale           the scale
     */
    @Unique
    public void fDPClient$renderCustom(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (left_leg == null) {
            fDPClient$generatemodel();
        }


        CustomModel customModel = FDPClient.moduleManager.getModule(CustomModel.class);
        GlStateManager.pushMatrix();
        if (CustomModel.customModel.get() && (CustomModel.onlyMe.get() && entityIn == Minecraft.getMinecraft().thePlayer || CustomModel.onlyOther.get() && entityIn != Minecraft.getMinecraft().thePlayer) && Objects.requireNonNull(FDPClient.moduleManager.getModule(CustomModel.class)).getState()) {
            if (CustomModel.mode.get().contains("Rabbit")) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(1.25D, 1.25D, 1.25D);
                GlStateManager.translate(0.0D, -0.3D, 0.0D);
                this.fDPClient$rabbitHead.rotateAngleX = this.bipedHead.rotateAngleX;
                this.fDPClient$rabbitHead.rotateAngleY = this.bipedHead.rotateAngleY;
                this.fDPClient$rabbitHead.rotateAngleZ = this.bipedHead.rotateAngleZ;
                this.fDPClient$rabbitLarm.rotateAngleX = this.bipedLeftArm.rotateAngleX;
                this.fDPClient$rabbitLarm.rotateAngleY = this.bipedLeftArm.rotateAngleY;
                this.fDPClient$rabbitLarm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
                this.fDPClient$rabbitRarm.rotateAngleX = this.bipedRightArm.rotateAngleX;
                this.fDPClient$rabbitRarm.rotateAngleY = this.bipedRightArm.rotateAngleY;
                this.fDPClient$rabbitRarm.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
                this.fDPClient$rabbitRleg.rotateAngleX = this.bipedRightLeg.rotateAngleX;
                this.fDPClient$rabbitRleg.rotateAngleY = this.bipedRightLeg.rotateAngleY;
                this.fDPClient$rabbitRleg.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
                this.fDPClient$rabbitLleg.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
                this.fDPClient$rabbitLleg.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
                this.fDPClient$rabbitLleg.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
                this.fDPClient$rabbitBone.render(scale);
                GlStateManager.popMatrix();
            } else if (Objects.requireNonNull(customModel).getState() && CustomModel.mode.get().contains("Freddy")) {
                this.fDPClient$fredhead.rotateAngleX = this.bipedHead.rotateAngleX;
                this.fDPClient$fredhead.rotateAngleY = this.bipedHead.rotateAngleY;
                this.fDPClient$fredhead.rotateAngleZ = this.bipedHead.rotateAngleZ;
                this.fDPClient$armLeft.rotateAngleX = this.bipedLeftArm.rotateAngleX;
                this.fDPClient$armLeft.rotateAngleY = this.bipedLeftArm.rotateAngleY;
                this.fDPClient$armLeft.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
                this.fDPClient$legRight.rotateAngleX = this.bipedRightLeg.rotateAngleX;
                this.fDPClient$legRight.rotateAngleY = this.bipedRightLeg.rotateAngleY;
                this.fDPClient$legRight.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
                this.fDPClient$legLeft.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
                this.fDPClient$legLeft.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
                this.fDPClient$legLeft.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
                this.fDPClient$armRight.rotateAngleX = this.bipedRightArm.rotateAngleX;
                this.fDPClient$armRight.rotateAngleY = this.bipedRightArm.rotateAngleY;
                this.fDPClient$armRight.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.75, 0.65, 0.75);
                GlStateManager.translate(0.0, 0.85, 0.0);
                this.fDPClient$fredbody.render(scale);
                GlStateManager.popMatrix();
            } else if (CustomModel.mode.get().contains("Amogus")){
                this.bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
                this.bipedHead.rotateAngleX = headPitch * 0.017453292F;
                this.bipedBody.rotateAngleY = 0.0F;
                this.bipedRightArm.rotationPointZ = 0.0F;
                this.bipedRightArm.rotationPointX = -5.0F;
                this.bipedLeftArm.rotationPointZ = 0.0F;
                this.bipedLeftArm.rotationPointX = 5.0F;
                float f = 1.0F;
                this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F / f;
                this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
                this.bipedRightArm.rotateAngleZ = 0.0F;
                this.bipedLeftArm.rotateAngleZ = 0.0F;
                this.right_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
                this.left_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount / f;
                this.right_leg.rotateAngleY = 0.0F;
                this.left_leg.rotateAngleY = 0.0F;
                this.right_leg.rotateAngleZ = 0.0F;
                this.left_leg.rotateAngleZ = 0.0F;
                int bodyCustomColor = new Color(CustomModel.bodyColorR.get(), CustomModel.bodyColorG.get(), CustomModel.bodyColorB.get()).getRGB();
                int eyeCustomColor = new Color(CustomModel.eyeColorR.get(), CustomModel.eyeColorG.get(), CustomModel.eyeColorB.get()).getRGB();
                int legsCustomColor = new Color(CustomModel.legsColorR.get(), CustomModel.legsColorG.get(), CustomModel.legsColorB.get()).getRGB();
                if (this.isChild) {
                    GlStateManager.scale(0.5F, 0.5F, 0.5F);
                    GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
                    this.fDPClient$body.render(scale);
                    this.left_leg.render(scale);
                    this.right_leg.render(scale);
                } else {
                    GlStateManager.translate(0.0D, -0.8D, 0.0D);
                    GlStateManager.scale(1.8D, 1.6D, 1.6D);
                    RenderUtils.color(bodyCustomColor);
                    GlStateManager.translate(0.0D, 0.15D, 0.0D);
                    this.fDPClient$body.render(scale);
                    RenderUtils.color(eyeCustomColor);
                    this.fDPClient$eye.render(scale);
                    RenderUtils.color(legsCustomColor);
                    GlStateManager.translate(0.0D, -0.15D, 0.0D);
                    this.left_leg.render(scale);
                    this.right_leg.render(scale);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        } else {
            super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (isChild) {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            } else {
                if (entityIn.isSneaking())
                    GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            bipedLeftLegwear.render(scale);
            bipedRightLegwear.render(scale);
            bipedLeftArmwear.render(scale);
            bipedRightArmwear.render(scale);
            bipedBodyWear.render(scale);
        }
        GlStateManager.popMatrix();
    }

    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    private void revertSwordAnimation(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_, CallbackInfo callbackInfo) {
        FDPClient.eventManager.callEvent(new UpdateModelEvent((EntityPlayer) p_setRotationAngles_7_,(ModelPlayer)(Object)this));
    }
}