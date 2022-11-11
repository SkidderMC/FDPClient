/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.light.LightSettings;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class InputBox extends Gui {
    private final int id;
    public int xPosition;
    public int yPosition;
    private final int width;
    private final int height;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private boolean visible = true;
    private GuiResponder field_175210_x;
    private Predicate validator = Predicates.alwaysTrue();

    public InputBox(int componentId, int x, int y, int par5Width, int par6Height) {
        this.id = componentId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = par5Width;
        this.height = par6Height;
    }


    public void func_175207_a(GuiResponder p_175207_1_) {
        this.field_175210_x = p_175207_1_;
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    public void setText(String p_146180_1_) {
        if(this.validator.apply(p_146180_1_)) {
            if(p_146180_1_.length() > this.maxStringLength) {
                this.text = p_146180_1_.substring(0, this.maxStringLength);
            } else {
                this.text = p_146180_1_;
            }

            this.setCursorPositionEnd();
        }

    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = this.cursorPosition < this.selectionEnd?this.cursorPosition:this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd?this.selectionEnd:this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void writeText(String p_146191_1_) {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(p_146191_1_);
        int i = this.cursorPosition < this.selectionEnd?this.cursorPosition:this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd?this.selectionEnd:this.cursorPosition;
        int k = this.maxStringLength - this.text.length() - (i - j);
        boolean l = false;
        if(this.text.length() > 0) {
            s = s + this.text.substring(0, i);
        }

        int l1;
        if(k < s1.length()) {
            s = s + s1.substring(0, k);
            l1 = k;
        } else {
            s = s + s1;
            l1 = s1.length();
        }

        if(this.text.length() > 0 && j < this.text.length()) {
            s = s + this.text.substring(j);
        }

        if(this.validator.apply(s)) {
            this.text = s;
            this.moveCursorBy(i - this.selectionEnd + l1);
            if(this.field_175210_x != null) {
                this.field_175210_x.func_175319_a(this.id, this.text);
            }
        }

    }

    public void deleteWords(int p_146177_1_) {
        if(this.text.length() != 0) {
            if(this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
            }
        }

    }

    public void deleteFromCursor(int p_146175_1_) {
        if(this.text.length() != 0) {
            if(this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = p_146175_1_ < 0;
                int i = flag?this.cursorPosition + p_146175_1_:this.cursorPosition;
                int j = flag?this.cursorPosition:this.cursorPosition + p_146175_1_;
                String s = "";
                if(i >= 0) {
                    s = this.text.substring(0, i);
                }

                if(j < this.text.length()) {
                    s = s + this.text.substring(j);
                }

                if(this.validator.apply(s)) {
                    this.text = s;
                    if(flag) {
                        this.moveCursorBy(p_146175_1_);
                    }

                    if(this.field_175210_x != null) {
                        this.field_175210_x.func_175319_a(this.id, this.text);
                    }
                }
            }
        }

    }

    public int getId() {
        return this.id;
    }

    public int getNthWordFromCursor(int p_146187_1_) {
        return this.getNthWordFromPos(p_146187_1_, this.getCursorPosition());
    }

    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
        return this.func_146197_a(p_146183_1_, p_146183_2_, true);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
        int i = p_146197_2_;
        boolean flag = p_146197_1_ < 0;
        int j = Math.abs(p_146197_1_);

        for(int k = 0; k < j; ++k) {
            if(!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if(i == -1) {
                    i = l;
                } else {
                    while(p_146197_3_ && i < l && this.text.charAt(i) == 32) {
                        ++i;
                    }
                }
            } else {
                while(p_146197_3_ && i > 0 && this.text.charAt(i - 1) == 32) {
                    --i;
                }

                while(i > 0 && this.text.charAt(i - 1) != 32) {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursorBy(int p_146182_1_) {
        this.setCursorPosition(this.selectionEnd + p_146182_1_);
    }

    public void setCursorPosition(int p_146190_1_) {
        this.cursorPosition = p_146190_1_;
        int i = this.text.length();
        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
        if(!this.isFocused) {
            return false;
        } else if(GuiScreen.isKeyComboCtrlA(p_146201_2_)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if(GuiScreen.isKeyComboCtrlC(p_146201_2_)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if(GuiScreen.isKeyComboCtrlV(p_146201_2_)) {
            if(this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if(GuiScreen.isKeyComboCtrlX(p_146201_2_)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            if(this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch(p_146201_2_) {
                case 14:
                    if(GuiScreen.isCtrlKeyDown()) {
                        if(this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if(this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;
                case 199:
                    if(GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }

                    return true;
                case 203:
                    if(GuiScreen.isShiftKeyDown()) {
                        if(GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if(GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;
                case 205:
                    if(GuiScreen.isShiftKeyDown()) {
                        if(GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if(GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;
                case 207:
                    if(GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }

                    return true;
                case 211:
                    if(GuiScreen.isCtrlKeyDown()) {
                        if(this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if(this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;
                default:
                    if(ChatAllowedCharacters.isAllowedCharacter(p_146201_1_)) {
                        if(this.isEnabled) {
                            this.writeText(Character.toString(p_146201_1_));
                        }

                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    public void mouseClicked(int p_146192_1_, int p_146192_2_, int p_146192_3_) {
        boolean flag = p_146192_1_ >= this.xPosition && p_146192_1_ < this.xPosition + this.width && p_146192_2_ >= this.yPosition && p_146192_2_ < this.yPosition + this.height;
        if(this.canLoseFocus) {
            this.setFocused(flag);
        }

        if(this.isFocused && flag && p_146192_3_ == 0) {
            int i = p_146192_1_ - this.xPosition;
            if(this.enableBackgroundDrawing) {
                i -= 4;
            }

            String s = Fonts.font35.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(Fonts.font35.trimStringToWidth(s, i).length() + this.lineScrollOffset);
        }

    }

    public void drawTextBox() {
        if(this.getVisible()) {
            if(this.getEnableBackgroundDrawing()) {
                RenderUtils.drawBorderedRect((float)this.xPosition, (float)this.yPosition - 2, (float)(this.xPosition + this.width), (float)(this.yPosition + this.height + 2), 1, new Color(0, 0, 0, 0).getRGB(),new Color(0, 0, 0, 140).getRGB());
            }

            int i = this.isEnabled?(new Color(100, 100, 100)).getRGB():this.disabledColor;
            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            String s = Fonts.font35.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
            int l = this.enableBackgroundDrawing?this.xPosition + 4:this.xPosition;
            int i1 = this.enableBackgroundDrawing?this.yPosition + (this.height - 8) / 2:this.yPosition;
            int j1 = l;
            if(k > s.length()) {
                k = s.length();
            }

            if(s.length() > 0) {
                String flag2 = flag?s.substring(0, j):s;
                //j1 = Fonts.font35.drawStringWithShadow(flag2, (float)l, (float)i1 + 5, -1);
            }

            boolean var13 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int k1 = j1;
            if(!flag) {
                k1 = j > 0?l + this.width:l;
            } else if(var13) {
                k1 = j1 - 1;
                --j1;
            }

            if(flag1) {
                if(var13) {
                    Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + Fonts.font35.FONT_HEIGHT, -3092272);
                } else {
                    //Fonts.font35.drawString("_", (float)k1, (float)i1 + 5, -1);
                }
            }

            if(k != j) {
                int l1 = l + Fonts.font35.getStringWidth(s.substring(0, k));
                this.drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + Fonts.font35.FONT_HEIGHT);
            }
        }

    }

    private void drawCursorVertical(int p_146188_1_, int p_146188_2_, int p_146188_3_, int p_146188_4_) {
        int tessellator;
        if(p_146188_1_ < p_146188_3_) {
            tessellator = p_146188_1_;
            p_146188_1_ = p_146188_3_;
            p_146188_3_ = tessellator;
        }

        if(p_146188_2_ < p_146188_4_) {
            tessellator = p_146188_2_;
            p_146188_2_ = p_146188_4_;
            p_146188_4_ = tessellator;
        }

        if(p_146188_3_ > this.xPosition + this.width) {
            p_146188_3_ = this.xPosition + this.width;
        }

        if(p_146188_1_ > this.xPosition + this.width) {
            p_146188_1_ = this.xPosition + this.width;
        }

        Tessellator tessellatorInstance = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellatorInstance.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_146188_1_, p_146188_4_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_3_, p_146188_4_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_3_, p_146188_2_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_1_, p_146188_2_, 0.0D).endVertex();
        tessellatorInstance.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void setMaxStringLength(int p_146203_1_) {
        this.maxStringLength = p_146203_1_;
        if(this.text.length() > p_146203_1_) {
            this.text = this.text.substring(0, p_146203_1_);
        }

    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean p_146185_1_) {
        this.enableBackgroundDrawing = p_146185_1_;
    }

    public void setTextColor(int p_146193_1_) {
        this.enabledColor = p_146193_1_;
    }

    public void setDisabledTextColour(int p_146204_1_) {
        this.disabledColor = p_146204_1_;
    }

    public void setFocused(boolean p_146195_1_) {
        if(p_146195_1_ && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = p_146195_1_;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void setEnabled(boolean p_146184_1_) {
        this.isEnabled = p_146184_1_;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public int getWidth() {
        return this.getEnableBackgroundDrawing()?this.width - 8:this.width;
    }

    public void setSelectionPos(int p_146199_1_) {
        int i = this.text.length();
        if(p_146199_1_ > i) {
            p_146199_1_ = i;
        }

        if(p_146199_1_ < 0) {
            p_146199_1_ = 0;
        }

        this.selectionEnd = p_146199_1_;
        if(Fonts.font35 != null) {
            if(this.lineScrollOffset > i) {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = Fonts.font35.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;
            if(p_146199_1_ == this.lineScrollOffset) {
                this.lineScrollOffset -= Fonts.font35.trimStringToWidth(this.text, j, true).length();
            }

            if(p_146199_1_ > k) {
                this.lineScrollOffset += p_146199_1_ - k;
            } else if(p_146199_1_ <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - p_146199_1_;
            }

            this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, i);
        }

    }

    public void setCanLoseFocus(boolean p_146205_1_) {
        this.canLoseFocus = p_146205_1_;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean p_146189_1_) {
        this.visible = p_146189_1_;
    }
}
