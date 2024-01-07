/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.injection.access.IMixinGuiSlot;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GuiSlot.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiSlot implements IMixinGuiSlot {

    @Shadow
    protected boolean showSelectionBox;

    @Shadow
    protected abstract int getSize();

    @Final
    @Shadow
    public int slotHeight;

    @Shadow
    public int headerPadding;

    /**
     * The Left.
     */
    @Shadow
    public int left;
    /**
     * The Top.
     */
    @Shadow
    public int top;
    /**
     * The Width.
     */
    @Shadow
    public int width;
    /**
     * The Right.
     */
    @Shadow
    public int right;
    /**
     * The Bottom.
     */
    @Shadow
    public int bottom;
    /**
     * The Height.
     */
    @Shadow
    public int height;
    /**
     * The Field 178041 q.
     */
    @Shadow
    protected boolean field_178041_q;
    /**
     * The Mouse x.
     */
    @Shadow
    protected int mouseX;
    /**
     * The Mouse y.
     */
    @Shadow
    protected int mouseY;
    /**
     * The Amount scrolled.
     */
    @Shadow
    protected float amountScrolled;
    /**
     * The Has list header.
     */
    @Shadow
    protected boolean hasListHeader;
    /**
     * The Mc.
     */
    @Shadow
    @Final
    protected Minecraft mc;
    @Unique
    private int listWidth = 220;

    /**
     * Draw background.
     */
    @Shadow
    protected abstract void drawBackground();

    /**
     * Bind amount scrolled.
     */
    @Shadow
    protected abstract void bindAmountScrolled();

    /**
     * Draw list header.
     *
     * @param p_148129_1_ the p 148129 1
     * @param p_148129_2_ the p 148129 2
     * @param p_148129_3_ the p 148129 3
     */
    @Shadow
    protected abstract void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_);

    /**
     * Draw selection box.
     *
     * @param p_148120_1_ the p 148120 1
     * @param p_148120_2_ the p 148120 2
     * @param mouseXIn    the mouse x in
     * @param mouseYIn    the mouse y in
     */
    @Shadow
    protected abstract void drawSelectionBox(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn);

    /**
     * Gets content height.
     *
     * @return the content height
     */
    @Shadow
    protected abstract int getContentHeight();

    /**
     * Func 148135 f int.
     *
     * @return the int
     */
    @Shadow
    public abstract int func_148135_f();

    /**
     * Func 148142 b.
     *
     * @param p_148142_1_ the p 148142 1
     * @param p_148142_2_ the p 148142 2
     */
    @Shadow
    protected abstract void func_148142_b(int p_148142_1_, int p_148142_2_);

    @Shadow
    protected abstract void func_178040_a(int p_178040_1_, int p_178040_2_, int p_178040_3_);

    @Shadow
    protected abstract void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn);


    @Shadow
    protected abstract boolean isSelected(int slotIndex);


    @Inject(method = "drawSelectionBox", at = @At("HEAD"), cancellable = true)
    public void drawSelectionBoxs(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn, CallbackInfo ci) {
        int i = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        for (int j = 0; j < i; ++j) {
            int k = p_148120_2_ + j * this.slotHeight + this.headerPadding;
            int l = this.slotHeight - 4;

            if (k > this.bottom || k + l < this.top) {
                this.func_178040_a(j, p_148120_1_, k);
            }

            if (this.showSelectionBox && this.isSelected(j)) {
                int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
                int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
                int color = new Color(0, 0, 0, 90).getRGB();
                int radius = 4;
                float xPosition = p_148120_1_ - 3;
                float yPosition = k - 3;
                float width = j1 - i1 + 3;
                float height = l + 6;

                RenderUtils.drawRoundedCornerRect(xPosition, yPosition, xPosition + width ,
                        yPosition + height,radius, color);
                RenderUtils.drawRoundedCornerRect(xPosition-0.3f, yPosition-0.3f, xPosition + width + 0.3f ,
                        yPosition + height + 0.3f,radius-1, color);
                //GlStateManager.enableTexture2D();
            }

            this.drawSlot(j, p_148120_1_, k, l, mouseXIn, mouseYIn);
        }
        ci.cancel();
    }
    /**
     * Draw screen.
     *
     * @param mouseXIn    the mouse x in
     * @param mouseYIn    the mouse y in
     * @param p_148128_3_ the p 148128 3
     * @author opZywl
     * @reason Draw
     */
    @Overwrite
    public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_) {
        if (this.field_178041_q) {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            this.drawBackground();
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int) this.amountScrolled;
            if (this.hasListHeader) {
                this.drawListHeader(k, l, tessellator);
            }

            RenderUtils.makeScissorBox(left, top, right, bottom);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            this.drawSelectionBox(k, l + 2, mouseXIn, mouseYIn + 2);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            GlStateManager.disableDepth();
            int i1 = 4;

            // ClientCode
            Gui.drawRect(0, 0, this.width, this.top, Integer.MIN_VALUE);
            Gui.drawRect(0, this.bottom, this.width, this.height, Integer.MIN_VALUE);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(this.left, this.top + i1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos(this.right, this.top + i1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos(this.right, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(this.left, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(this.left, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(this.right, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(this.right, this.bottom - i1, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos(this.left, this.bottom - i1, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            int j1 = this.func_148135_f();
            if (j1 > 0) {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp_int(k1, 32, this.bottom - this.top - 8);
                int l1 = (int) this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;
                if (l1 < this.top) {
                    l1 = this.top;
                }

                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos(i, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(j, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(j, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(i, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos(i, l1 + k1, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos(j, l1 + k1, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos(j, l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos(i, l1 + k1 - 1, 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos(j - 1, l1 + k1 - 1, 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos(j - 1, l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.func_148142_b(mouseXIn, mouseYIn);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    /**
     * Gets scroll bar x.
     *
     * @return the scroll bar x
     * @author opZywl
     * @reason ScrollBar
     */
    @Overwrite
    protected int getScrollBarX() {
        return this.width - 5;
    }

    @Override
    public void setEnableScissor(boolean enableScissor) {
    }

    /**
     * Gets list width.
     *
     * @return the list width
     * @author As_pw
     * @reason Width
     */
    @Overwrite
    public int getListWidth() {
        return this.listWidth;
    }

    @Override
    public void setListWidth(int listWidth) {
        this.listWidth = listWidth;
    }

}