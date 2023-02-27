package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Mixin(ServerListEntryNormal.class)
public abstract class MixinServerListEntryNormal {
    @Shadow
    private static final ThreadPoolExecutor field_148302_b = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    @Shadow
    private static ResourceLocation UNKNOWN_SERVER;
    @Shadow
    private static ResourceLocation SERVER_SELECTION_BUTTONS;
    @Shadow
    private GuiMultiplayer owner;
    @Shadow
    private Minecraft mc;
    @Shadow
    private ServerData server;
    @Shadow
    private ResourceLocation serverIcon;
    @Shadow
    private String field_148299_g;
    @Shadow
    private DynamicTexture field_148305_h;
    @Shadow
    private long field_148298_f;

    @Shadow
    public abstract void prepareServerIcon();
    @Shadow
    public abstract boolean func_178013_b();

    @Shadow
    protected abstract void drawTextureAt(int p_drawTextureAt_1_, int p_drawTextureAt_2_, ResourceLocation p_drawTextureAt_3_);

    @Inject(method = "drawEntry", at = @At("HEAD"), cancellable = true)
    public void drawEntry(int p_drawEntry_1_, int p_drawEntry_2_, int p_drawEntry_3_, int p_drawEntry_4_, int p_drawEntry_5_, int p_drawEntry_6_, int p_drawEntry_7_, boolean p_drawEntry_8_, CallbackInfo ci) {
        if (!this.server.field_78841_f) {
            this.server.field_78841_f = true;
            this.server.pingToServer = -2L;
            this.server.serverMOTD = "";
            this.server.populationInfo = "";
            field_148302_b.submit(() -> {
                try {
                    owner.getOldServerPinger().ping(server);
                } catch (UnknownHostException var2) {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't resolve hostname";
                } catch (Exception var3) {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
                }

            });
        }

        boolean flag = this.server.version > 47;
        boolean flag1 = this.server.version < 47;
        boolean flag2 = flag || flag1;
        this.mc.fontRendererObj.drawStringWithShadow(this.server.serverName, p_drawEntry_2_ + 32 + 3, p_drawEntry_3_ + 1, 16777215);
        List<String> list = this.mc.fontRendererObj.listFormattedStringToWidth(FMLClientHandler.instance().fixDescription(this.server.serverMOTD), p_drawEntry_4_ - 48 - 2);

        for (int i = 0; i < Math.min(list.size(), 2); ++i) {
            this.mc.fontRendererObj.drawStringWithShadow(list.get(i), p_drawEntry_2_ + 32 + 3, p_drawEntry_3_ + 12 + this.mc.fontRendererObj.FONT_HEIGHT * i, 8421504);
        }

        String s2 = flag2 ? EnumChatFormatting.DARK_RED + this.server.gameVersion : this.server.populationInfo;
        int j = this.mc.fontRendererObj.getStringWidth(s2);
        this.mc.fontRendererObj.drawStringWithShadow(s2, p_drawEntry_2_ + p_drawEntry_4_ - j - 15 - 2, p_drawEntry_3_ + 1, 8421504);
        int k = 0;
        String s = null;
        int l;
        String s1;
        if (flag2) {
            l = 5;
            s1 = flag ? "Client out of date!" : "Server out of date!";
            s = this.server.playerList;
        } else if (this.server.field_78841_f && this.server.pingToServer != -2L) {
            if (this.server.pingToServer < 0L) {
                l = 5;
            } else if (this.server.pingToServer < 150L) {
                l = 0;
            } else if (this.server.pingToServer < 300L) {
                l = 1;
            } else if (this.server.pingToServer < 600L) {
                l = 2;
            } else if (this.server.pingToServer < 1000L) {
                l = 3;
            } else {
                l = 4;
            }

            if (this.server.pingToServer < 0L) {
                s1 = "(no connection)";
            } else {
                s1 = this.server.pingToServer + "ms";
                s = this.server.playerList;
            }
        } else {
            k = 1;
            l = (int) (Minecraft.getSystemTime() / 100L + (long) (p_drawEntry_1_ * 2L) & 7L);
            if (l > 4) {
                l = 8 - l;
            }

            s1 = "Pinging...";
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Gui.icons);
        Gui.drawModalRectWithCustomSizedTexture(p_drawEntry_2_ + p_drawEntry_4_ - 15, p_drawEntry_3_, (float) (k * 10), (float) (176 + l * 8), 10, 8, 256.0F, 256.0F);
        if (this.server.getBase64EncodedIconData() != null && !this.server.getBase64EncodedIconData().equals(this.field_148299_g)) {
            this.field_148299_g = this.server.getBase64EncodedIconData();
            this.prepareServerIcon();
            this.owner.getServerList().saveServerList();
        }

        if (this.field_148305_h != null) {
            this.drawTextureAt(p_drawEntry_2_, p_drawEntry_3_, this.serverIcon);
        } else {
            this.drawTextureAt(p_drawEntry_2_, p_drawEntry_3_, UNKNOWN_SERVER);
        }

        int i1 = p_drawEntry_6_ - p_drawEntry_2_;
        int j1 = p_drawEntry_7_ - p_drawEntry_3_;
        String tooltip = FMLClientHandler.instance().enhanceServerListEntry(null, this.server, p_drawEntry_2_, p_drawEntry_4_, p_drawEntry_3_, i1, j1);
        if (tooltip != null) {
            this.owner.setHoveringText(tooltip);
        } else if (i1 >= p_drawEntry_4_ - 15 && i1 <= p_drawEntry_4_ - 5 && j1 >= 0 && j1 <= 8) {
            this.owner.setHoveringText(s1);
        } else if (i1 >= p_drawEntry_4_ - j - 15 - 2 && i1 <= p_drawEntry_4_ - 15 - 2 && j1 >= 0 && j1 <= 8) {
            this.owner.setHoveringText(s);
        }

        if (this.mc.gameSettings.touchscreen || p_drawEntry_8_) {
            this.mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            Gui.drawRect(p_drawEntry_2_, p_drawEntry_3_, p_drawEntry_2_ + 32, p_drawEntry_3_ + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int k1 = p_drawEntry_6_ - p_drawEntry_2_;
            int l1 = p_drawEntry_7_ - p_drawEntry_3_;
            if (this.func_178013_b()) {
                if (k1 < 32 && k1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(p_drawEntry_2_, p_drawEntry_3_, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(p_drawEntry_2_, p_drawEntry_3_, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (this.owner.func_175392_a(null, p_drawEntry_1_)) {
                if (k1 < 16 && l1 < 16) {
                    Gui.drawModalRectWithCustomSizedTexture(p_drawEntry_2_, p_drawEntry_3_, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(p_drawEntry_2_, p_drawEntry_3_, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (this.owner.func_175394_b(null, p_drawEntry_1_)) {
                if (k1 < 16 && l1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(p_drawEntry_2_, p_drawEntry_3_, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(p_drawEntry_2_, p_drawEntry_3_, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }
        }
        ci.cancel();
    }
}
