package net.ccbluex.liquidbounce.ui.client.gui.modernui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.font.CFontRenderer;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.ui.client.gui.scriptOnline.ScriptSubscribe;
import net.ccbluex.liquidbounce.ui.client.gui.scriptOnline.Subscriptions;
import net.ccbluex.liquidbounce.script.Script;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType;
import net.ccbluex.liquidbounce.utils.render.BlurUtils;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.SmoothRenderUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class GuiScriptLoadMenu extends GuiScreen {
    public final MSTimer timer1 = new MSTimer();
    public final MSTimer timer2 = new MSTimer();
    public int x = 20;
    public int y = 20;
    public int dragX;
    public ScriptMenuType menuType = ScriptMenuType.Main;
    public boolean drag;
    public int dragY;

    public int scroll;
    public int scrollTo;
    public int scrollVelocity;

    @Override
    public void initGui() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        x = scaledResolution.getScaledWidth() / 2 - 250;
        y = scaledResolution.getScaledHeight() / 2 - 150;
        guiOpenTime = System.currentTimeMillis();

    }

    public static boolean isClickSub = false;

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20) {
            if (mouseButton == 0) {
                drag = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            }
        }
        if (mouseX > x + 8 && mouseX < x + 112 && mouseY > y + 108 && mouseY < y + 128) {
            // Online
            menuType = ScriptMenuType.Online;
        }
        if (mouseX > x + 8 && mouseX < x + 112 && mouseY > y + 137 && mouseY < y + 157) {
            // Subscribes
            menuType = ScriptMenuType.Subscribes;
        }
        if (mouseX > x + 8 && mouseX < x + 108 && mouseY > y + 79 && mouseY < y + 99) {
            // Local
            menuType = ScriptMenuType.Local;
        }
        if (mouseX > x + 8 && mouseX < x + 108 && mouseY > y + 50 && mouseY < y + 70) {
            // Main
            menuType = ScriptMenuType.Main;
        }
        if (menuType == ScriptMenuType.Local) {
            if (mouseX > x + 450 && mouseX < x + 490 && mouseY > y + 280 && mouseY < y + 295) {
                long startTime = System.currentTimeMillis();
                FDPClient.hud.addNotification(new Notification("Script Manager", "Reloading Scripts..", NotifyType.INFO, 1500, 500));

                FDPClient.scriptManager.disableScripts();
                FDPClient.scriptManager.unloadScripts();
                for (ScriptSubscribe scriptSubscribe : Subscriptions.subscribes) {
                    scriptSubscribe.load();
                }
                FDPClient.scriptManager.loadScripts();
                FDPClient.scriptManager.enableScripts();
                FDPClient.hud.addNotification(new Notification("Script Manager", "Reload Successful (" + (System.currentTimeMillis() - startTime) + "ms)", NotifyType.SUCCESS, 1500, 500));
            }
            if (mouseX > x + 405 && mouseX < x + 445 && mouseY > y + 280 && mouseY < y + 295) {
                FDPClient.scriptManager.disableScripts();
                FDPClient.scriptManager.unloadScripts();
            }
            int i = 0;
            try {
                List<Script> scriptList = new ArrayList<>();
                FDPClient.scriptManager.getScripts().stream().filter(script -> !script.isOnline()).forEach(scriptList::add);
                for (Script script : scriptList) {
                    if (x + 450 < mouseX && y + 41 - scroll + (i * 30) < mouseY && mouseX < x + 490 && mouseY < y - scroll + 59 + (i * 30)) {
                        if (script.getState()) {
                            FDPClient.hud.addNotification(new Notification("Script Manager", "Unload " + script.scriptName, NotifyType.INFO, 1500, 500));
                            for (Module registeredModule : script.getRegisteredModules()) {
                                registeredModule.setState(false);
                            }
                            script.onDisable();
                        } else {
                            FDPClient.hud.addNotification(new Notification("Script Manager", "Load " + script.scriptName, NotifyType.INFO, 1500, 500));
                            script.onEnable();
                            script.regAnyThing();
                        }
                    }
                    if (x + 425 < mouseX && y + 41 - scroll + (i * 30) < mouseY && mouseX < x + 445 && mouseY < y - scroll + 59 + (i * 30) && script.getState()) {
                        for (Module registeredModule : script.getRegisteredModules()) {
                            registeredModule.setState(!registeredModule.getState());
                        }

                    }
                    i++;
                }
            } catch (Exception E) {
                E.printStackTrace();
            }
        } else if (menuType == ScriptMenuType.Online) {
            if (mouseX > x + 450 && mouseX < x + 490 && mouseY > y + 280 && mouseY < y + 295) {
                long startTime = System.currentTimeMillis();
                FDPClient.hud.addNotification(new Notification("Script Manager", "Reloading Scripts..", NotifyType.INFO, 1500, 500));

                FDPClient.scriptManager.disableScripts();
                FDPClient.scriptManager.unloadScripts();
                for (ScriptSubscribe scriptSubscribe : Subscriptions.subscribes) {
                    scriptSubscribe.load();
                }
                FDPClient.scriptManager.loadScripts();
                FDPClient.scriptManager.enableScripts();
                FDPClient.hud.addNotification(new Notification("Script Manager", "Reload Successful (" + (System.currentTimeMillis() - startTime) + "ms)", NotifyType.SUCCESS, 1500, 500));
            }
            if (mouseX > x + 405 && mouseX < x + 445 && mouseY > y + 280 && mouseY < y + 295) {
                FDPClient.hud.addNotification(new Notification("Script Manager", "Unload Scripts", NotifyType.INFO, 1500, 500));
                FDPClient.scriptManager.disableScripts();
                FDPClient.scriptManager.unloadScripts();
            }
            int i = 0;
            try {
                List<Script> scriptList = new ArrayList<>();
                FDPClient.scriptManager.getScripts().stream().filter(Script::isOnline).forEach(scriptList::add);
                for (Script script : scriptList) {
                    if (x + 450 < mouseX && y + 41 - scroll + (i * 30) < mouseY && mouseX < x + 490 && mouseY < y - scroll + 59 + (i * 30)) {
                        if (script.getState()) {
                            FDPClient.hud.addNotification(new Notification("Script Manager", "Unload " + script.scriptName, NotifyType.INFO, 1500, 500));

                            for (Module registeredModule : script.getRegisteredModules()) {
                                registeredModule.setState(false);
                            }
                            script.onDisable();
                        } else {
                            FDPClient.hud.addNotification(new Notification("Script Manager", "Load " + script.scriptName, NotifyType.INFO, 1500, 500));
                            script.onEnable();
                            script.regAnyThing();
                        }
                    }
                    if (x + 425 < mouseX && y + 41 - scroll + (i * 30) < mouseY && mouseX < x + 445 && mouseY < y - scroll + 59 + (i * 30) && script.getState()) {
                        for (Module registeredModule : script.getRegisteredModules()) {
                            registeredModule.setState(!registeredModule.getState());
                        }

                    }
                    i++;
                }
            } catch (Exception E) {
                E.printStackTrace();
            }
        } else if (menuType == ScriptMenuType.Subscribes) {
            if (mouseX > x + 450 && mouseX < x + 490 && mouseY > y + 280 && mouseY < y + 295) {
                isClickSub = true;
            }
            int i = 0;
            try {
                List<ScriptSubscribe> scriptList = Subscriptions.subscribes;
                for (ScriptSubscribe script : scriptList) {
                    if (x + 450 < mouseX && y + 41 - scroll + (i * 30) < mouseY && mouseX < x + 490 && mouseY < y - scroll + 59 + (i * 30)) {
                        script.state = !script.state;
                        scriptList.remove(script);
                        FDPClient.scriptManager.disableScripts();
                        FDPClient.scriptManager.unloadScripts();
                        for (ScriptSubscribe scriptSubscribe : Subscriptions.subscribes) {
                            scriptSubscribe.load();
                        }
                        FDPClient.scriptManager.loadScripts();
                        FDPClient.scriptManager.enableScripts();
                    }
                    i++;
                }
            } catch (Exception E) {
                E.printStackTrace();
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    private long guiOpenTime = -1;

    private boolean translated = false;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (isClickSub) {
            try {
                Thread.sleep(500);
                String url = JOptionPane.showInputDialog(null, "Please input your subscribe", "FDP Script Cloud", JOptionPane.WARNING_MESSAGE);
                Thread.sleep(200);
                String name = JOptionPane.showInputDialog(null, "What name do you want to give this Subscribe (can be left blank)", "FDP Script Cloud", JOptionPane.WARNING_MESSAGE);
                Subscriptions.addSubscribes(new ScriptSubscribe(url, name));
                long startTime = System.currentTimeMillis();
                FDPClient.hud.addNotification(new Notification("Script Manager", "Reloading Scripts...", NotifyType.INFO, 1500, 500));
                FDPClient.scriptManager.disableScripts();
                FDPClient.scriptManager.unloadScripts();
                for (ScriptSubscribe scriptSubscribe : Subscriptions.subscribes) {
                    scriptSubscribe.load();
                }
                FDPClient.scriptManager.loadScripts();
                FDPClient.scriptManager.enableScripts();
                FDPClient.hud.addNotification(new Notification("Script Manager", "Added Subscribe: " + name + " | " + url + " (" + (System.currentTimeMillis() - startTime) + "ms)", NotifyType.SUCCESS, 1500, 500));
                isClickSub = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double pct = Math.max(500d - (System.currentTimeMillis() - guiOpenTime), 0) / (500d);
        if (pct != 0) {
            pct = EaseUtils.INSTANCE.apply(EaseUtils.EnumEasingType.EXPO,
                    EaseUtils.EnumEasingOrder.FAST_AT_START_AND_END, pct);
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            double scale = 1 - pct;
            GL11.glScaled(scale, scale, scale);
            GL11.glTranslated(((0 + (scaledResolution.getScaledWidth() * 0.5 * pct)) / scale) - 0,
                    ((0 + (scaledResolution.getScaledHeight() * 0.5d * pct)) / scale) - 0,
                    0);
            translated = true;
        }
        if (pct != 0) {
            timer2.reset();
        }
        if (drag) {
            if (!Mouse.isButtonDown(0)) {
                drag = false;
            }
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
        if (Mouse.hasWheel()) {
            scrollVelocity = Mouse.getDWheel();
            if (scrollVelocity != 0) timer1.reset();
        }
        mouseScroll(mouseX, mouseY, scrollVelocity);
        if (scroll <= -5 && !(mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20)) {
            scroll = -5;
            if (timer1.hasTimePassed(100) && scroll <= -5) {
                scroll = 0;
            }
        }
        if (menuType == ScriptMenuType.Local) {
            List<Script> scriptList = new ArrayList<>();
            FDPClient.scriptManager.getScripts().stream().filter(script -> !script.isOnline()).forEach(scriptList::add);
            if (scroll >= (scriptList.size() * 30) - 230 && !(mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20)) {
                scroll = (scriptList.size() * 30) - 230;
                if (timer1.hasTimePassed(100) && scroll >= (scriptList.size() * 30) - 230) {
                    scroll = (scriptList.size() * 30) - 235;
                }
            }
        } else if (menuType == ScriptMenuType.Online) {
            List<Script> scriptList = new ArrayList<>();
            FDPClient.scriptManager.getScripts().stream().filter(Script::isOnline).forEach(scriptList::add);
            if (scroll >= (scriptList.size() * 30) - 230 && !(mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20)) {
                scroll = (scriptList.size() * 30) - 230;
                if (timer1.hasTimePassed(100) && scroll >= (scriptList.size() * 30) - 230) {
                    scroll = (scriptList.size() * 30) - 235;
                }
            }
        } else if (menuType == ScriptMenuType.Subscribes) {
            List<ScriptSubscribe> scriptList = Subscriptions.subscribes;
            if (scroll >= (scriptList.size() * 30) - 230 && !(mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20)) {
                scroll = (scriptList.size() * 30) - 230;
                if (timer1.hasTimePassed(100) && scroll >= (scriptList.size() * 30) - 230) {
                    scroll = (scriptList.size() * 30) - 235;
                }
            }
        }

        if (pct == 0 && timer2.timePassed() != 0)
            BlurUtils.INSTANCE.draw(x, y, 120, 300, (float) timer2.timePassed() <= 200f ? 30f * (timer2.timePassed() / 200f) : 30f);
        SmoothRenderUtils.drawRect(x, y, x + 120, y + 300, new Color(31, 31, 31, (int) (180 * (1 - pct))).getRGB());
        SmoothRenderUtils.drawRoundRect(x + 120, y, x + 500, y + 300, 7f, new Color(24, 24, 24, 255).getRGB());
        SmoothRenderUtils.drawRect(x + 120, y, x + 140, y + 300, new Color(24, 24, 24, 255).getRGB());
        SmoothRenderUtils.drawRoundRect(x + 120, y, x + 500, y + 20, 5f, new Color(29, 155, 240, 255).getRGB());
        SmoothRenderUtils.drawRect(x + 120, y + 15, x + 500, y + 20f, new Color(29, 155, 240, 255).getRGB());
        SmoothRenderUtils.drawRect(x + 120, y, x + 140, y + 20f, new Color(29, 155, 240, 255).getRGB());

        SmoothRenderUtils.drawRoundRect(x - 10, y, x, y + 300, 4.9f, new Color(29, 155, 240, 255).getRGB());
        SmoothRenderUtils.drawRect(x - 4, y, x, y + 300, new Color(29, 155, 240, 255).getRGB());
        SmoothRenderUtils.drawRect(x - 4, y, x, y + 300, new Color(29, 155, 240, 255).getRGB());
        if (mouseX > x + 8 && mouseX < x + 112 && mouseY > y + 79 && mouseY < y + 99)
            RenderUtils.drawRoundedCornerRect(x + 8, y + 79, x + 112, y + 99, 3f, new Color(0, 0, 0, 26).getRGB());
        if (mouseX > x + 8 && mouseX < x + 112 && mouseY > y + 50 && mouseY < y + 70)
            RenderUtils.drawRoundedCornerRect(x + 8, y + 50, x + 112, y + 70, 3f, new Color(0, 0, 0, 26).getRGB());
        if (mouseX > x + 8 && mouseX < x + 112 && mouseY > y + 108 && mouseY < y + 128)
            RenderUtils.drawRoundedCornerRect(x + 8, y + 108, x + 112, y + 128, 3f, new Color(0, 0, 0, 26).getRGB());
        if (mouseX > x + 8 && mouseX < x + 112 && mouseY > y + 137 && mouseY < y + 157)
            RenderUtils.drawRoundedCornerRect(x + 8, y + 137, x + 112, y + 157, 3f, new Color(0, 0, 0, 26).getRGB());

        SmoothRenderUtils.drawRect(x, y + 41f, x + 120f, y + 41.4f, new Color(255, 255, 255, 60).getRGB());
        CFontRenderer.DisplayFonts(FontLoaders.F40, "FDPClient", x + 10, y + 10, new Color(255, 255, 255, 255).getRGB());
        if (menuType == ScriptMenuType.Local) {
            SmoothRenderUtils.drawRoundRect(x + 8, y + 79, x + 112, y + 99, 3f, new Color(29, 155, 240, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Main Page", x + 12, y + 56, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Local Scripts", x + 12, y + 85, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Subscribes", x + 12, y + 143, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Online Scripts", x + 12, y + 114, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Local Scripts", x + 126, y + 6, new Color(255, 255, 255).getRGB());
            int i = 0;
            GL11.glPushMatrix();
            GL11.glEnable(3089);
            RenderUtils.makeScissorBox(x + 120, y + 20, x + 500, y + 275);
            List<Script> scriptList = new ArrayList<>();
            FDPClient.scriptManager.getScripts().stream().filter(script -> !script.isOnline()).forEach(scriptList::add);
            for (Script script : scriptList) {
                CFontRenderer.DisplayFonts(FontLoaders.C18, script.scriptName, x + 134, y - scroll + 40 + (i * 30), script.getState() ? new Color(255, 255, 255).getRGB() : new Color(180, 180, 180).getRGB());
                StringBuilder authors = new StringBuilder();
                int length = 0;
                for (String author : script.scriptAuthors) {
                    length++;
                    if (length != script.scriptAuthors.length) authors.append(author).append("/");
                    else {
                        authors.append(author);
                    }
                }
                CFontRenderer.DisplayFonts(FontLoaders.C16, "§3Authors: §f" + authors + " §3Version: §f" + script.scriptVersion, x + 134, y - scroll + 52 + (i * 30), new Color(255, 255, 255).getRGB());
                if (i + 1 != scriptList.size())
                    SmoothRenderUtils.drawRect(x + 130, y - scroll + 64.8 + (i * 30), x + 490, y - scroll + 65 + (i * 30), new Color(211, 211, 211, 95).getRGB());
                if (i != scriptList.size())
                    SmoothRenderUtils.drawRoundRect(x + 450, y - scroll + 41 + (i * 30), x + 490, y - scroll + 59 + (i * 30), 3f, new Color(29, 155, 240, 255).getRGB());
                if (i != scriptList.size())
                    CFontRenderer.DisplayFonts(FontLoaders.C16, script.getState() ? "Loaded" : "Unload", x + 455 + (script.getState() ? -1 : 0), y - scroll + 46 + (i * 30), !script.getState() ? new Color(220, 220, 220, 200).getRGB() : new Color(255, 255, 255, 255).getRGB());
                if (i != scriptList.size())
                    for (Module registeredModule : script.getRegisteredModules()) {
                        SmoothRenderUtils.drawRoundRect(x + 425, y - scroll + 41 + (i * 30), x + 445, y - scroll + 59 + (i * 30), 3f, new Color(29, 155, 240, 255).getRGB());
                        CFontRenderer.DisplayFonts(FontLoaders.C16, registeredModule.getState() ? "On" : "Off", x + 426 + (registeredModule.getState() ? 1 : 0), y - scroll + 46 + (i * 30), !registeredModule.getState() ? new Color(220, 220, 220, 200).getRGB() : new Color(255, 255, 255, 255).getRGB());
                    }
                i++;
            }
            GL11.glDisable(3089);
            GL11.glPopMatrix();
            RenderUtils.drawGradientSidewaysV(x + 120, y + 20, x + 500, y + 30, new Color(24, 24, 24).getRGB(), new Color(24, 24, 24, 0).getRGB());
            RenderUtils.drawGradientSidewaysV(x + 120, y + 265, x + 500, y + 275, new Color(24, 24, 24, 0).getRGB(), new Color(24, 24, 24).getRGB());
            RenderUtils.drawRect(x + 120, y + 274f, x + 500f, y + 274.4f, new Color(176, 176, 176, 89).getRGB());
            SmoothRenderUtils.drawRoundRect(x + 450, y + 280, x + 490, y + 295, 3f, new Color(29, 155, 240, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Reload", x + 455, y + 284, new Color(255, 255, 255, 255).getRGB());
            SmoothRenderUtils.drawRoundRect(x + 405, y + 280, x + 445, y + 295, 3f, new Color(29, 155, 240, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Unload", x + 410, y + 284, new Color(255, 255, 255, 255).getRGB());
        } else if (menuType == ScriptMenuType.Main) {
            SmoothRenderUtils.drawRoundRect(x + 8, y + 50, x + 112, y + 70, 3f, new Color(29, 155, 240, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Main Page", x + 12, y + 56, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Local Scripts", x + 12, y + 85, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Online Scripts", x + 12, y + 114, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Subscribes", x + 12, y + 143, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Main Page", x + 126, y + 6, new Color(255, 255, 255).getRGB());
            // 热交换舒服，所以就不创建在RendererUtils创建方法了
            CFontRenderer.DisplayFonts(FontLoaders.F30, "Script Manager", x + 135, y + 35, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "A Support Online Scripts Client!", x + 135, y + 55, new Color(255, 255, 255).getRGB());
            SmoothRenderUtils.drawRoundRect(x + 135, y + 78, x + 485, y + 130, 3f, new Color(127, 127, 127, 255).getRGB());
            SmoothRenderUtils.drawRoundRect(x + 136, y + 79, x + 484, y + 129, 2f, new Color(24, 24, 24, 255).getRGB());
            RenderUtils.drawRect(x + 145, y + 75, x + 170, y + 85, new Color(24, 24, 24, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Info", x + 148, y + 75, new Color(255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Loaded " + FDPClient.scriptManager.getScripts().size() + " Local scripts", x + 145, y + 85, new Color(217, 217, 217).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Import " + Subscriptions.subscribes.size() + " subscribe URLS", x + 145, y + 95, new Color(217, 217, 217).getRGB());
            AtomicInteger is = new AtomicInteger();
            FDPClient.scriptManager.getScripts().forEach(script -> is.addAndGet(script.getRegisteredModules().size()));
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Loaded " + is + " module from " + FDPClient.scriptManager.getScripts().size() + " scripts", x + 145, y + 105, new Color(217, 217, 217).getRGB());
            AtomicInteger i1 = new AtomicInteger();
            FDPClient.scriptManager.getScripts().forEach(script -> i1.addAndGet((int) script.getRegisteredModules().stream().filter(Module::getState).count()));
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Enable " + i1 + " module from " + FDPClient.scriptManager.getScripts().size() + " scripts", x + 145, y + 115, new Color(217, 217, 217).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C14, "Tips: FDPClient is not responsible for script security, if you have any doubts, please consult the developer", x + 130, y + 140, new Color(161, 161, 161).getRGB());

        } else if (menuType == ScriptMenuType.Online) {
            SmoothRenderUtils.drawRoundRect(x + 8, y + 108, x + 112, y + 128, 3f, new Color(29, 155, 240, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Main Page", x + 12, y + 56, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Local Scripts", x + 12, y + 85, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Subscribes", x + 12, y + 143, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C18, "Online Scripts", x + 12, y + 114, new Color(255, 255, 255, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Online Scripts", x + 126, y + 6, new Color(255, 255, 255).getRGB());
            int i = 0;
            GL11.glPushMatrix();
            GL11.glEnable(3089);
            RenderUtils.makeScissorBox(x + 120, y + 20, x + 500, y + 275);
            List<Script> scriptList = new ArrayList<>();
            FDPClient.scriptManager.getScripts().stream().filter(Script::isOnline).forEach(scriptList::add);
            for (Script script : scriptList) {
                CFontRenderer.DisplayFonts(FontLoaders.C18, script.scriptName, x + 134, y - scroll + 40 + (i * 30), script.getState() ? new Color(255, 255, 255).getRGB() : new Color(180, 180, 180).getRGB());
                StringBuilder authors = new StringBuilder();
                int length = 0;
                for (String author : script.scriptAuthors) {
                    length++;
                    if (length != script.scriptAuthors.length) authors.append(author).append("/");
                    else {
                        authors.append(author);
                    }
                }
                CFontRenderer.DisplayFonts(FontLoaders.C16, "§aOnline §f| §3Authors: §f" + authors + " §3Version: §f" + script.scriptVersion, x + 134, y - scroll + 52 + (i * 30), new Color(255, 255, 255).getRGB());
                if (i + 1 != scriptList.size())
                    SmoothRenderUtils.drawRect(x + 130, y - scroll + 64.8 + (i * 30), x + 490, y - scroll + 65 + (i * 30), new Color(211, 211, 211, 95).getRGB());
                if (i != scriptList.size())
                    SmoothRenderUtils.drawRoundRect(x + 450, y - scroll + 41 + (i * 30), x + 490, y - scroll + 59 + (i * 30), 3f, new Color(29, 155, 240, 255).getRGB());
                if (i != scriptList.size())
                    CFontRenderer.DisplayFonts(FontLoaders.C16, script.getState() ? "Loaded" : "Unload", x + 455 + (script.getState() ? -1 : 0), y - scroll + 46 + (i * 30), !script.getState() ? new Color(220, 220, 220, 200).getRGB() : new Color(255, 255, 255, 255).getRGB());
                if (i != scriptList.size())
                    for (Module registeredModule : script.getRegisteredModules()) {
                        SmoothRenderUtils.drawRoundRect(x + 425, y - scroll + 41 + (i * 30), x + 445, y - scroll + 59 + (i * 30), 3f, new Color(29, 155, 240, 255).getRGB());
                        CFontRenderer.DisplayFonts(FontLoaders.C16, registeredModule.getState() ? "On" : "Off", x + 426 + (registeredModule.getState() ? 1 : 0), y - scroll + 46 + (i * 30), !registeredModule.getState() ? new Color(220, 220, 220, 200).getRGB() : new Color(255, 255, 255, 255).getRGB());
                    }
                i++;
            }
            GL11.glDisable(3089);
            GL11.glPopMatrix();
            RenderUtils.drawGradientSidewaysV(x + 120, y + 20, x + 500, y + 30, new Color(24, 24, 24).getRGB(), new Color(24, 24, 24, 0).getRGB());
            RenderUtils.drawGradientSidewaysV(x + 120, y + 265, x + 500, y + 275, new Color(24, 24, 24, 0).getRGB(), new Color(24, 24, 24).getRGB());
            RenderUtils.drawRect(x + 120, y + 274f, x + 500f, y + 274.4f, new Color(176, 176, 176, 89).getRGB());
            SmoothRenderUtils.drawRoundRect(x + 450, y + 280, x + 490, y + 295, 3f, new Color(29, 155, 240, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Reload", x + 455, y + 284, new Color(255, 255, 255, 255).getRGB());
            SmoothRenderUtils.drawRoundRect(x + 405, y + 280, x + 445, y + 295, 3f, new Color(29, 155, 240, 255).getRGB());
            CFontRenderer.DisplayFonts(FontLoaders.C16, "Unload", x + 410, y + 284, new Color(255, 255, 255, 255).getRGB());

        } else if (menuType == ScriptMenuType.Subscribes) {
            try {
                SmoothRenderUtils.drawRoundRect(x + 8, y + 137, x + 112, y + 157, 3f, new Color(29, 155, 240, 255).getRGB());
                CFontRenderer.DisplayFonts(FontLoaders.C16, "Subscribes", x + 126, y + 6, new Color(255, 255, 255).getRGB());
                CFontRenderer.DisplayFonts(FontLoaders.C18, "Main Page", x + 12, y + 56, new Color(255, 255, 255, 255).getRGB());
                CFontRenderer.DisplayFonts(FontLoaders.C18, "Local Scripts", x + 12, y + 85, new Color(255, 255, 255, 255).getRGB());
                CFontRenderer.DisplayFonts(FontLoaders.C18, "Subscribes", x + 12, y + 143, new Color(255, 255, 255, 255).getRGB());
                CFontRenderer.DisplayFonts(FontLoaders.C18, "Online Scripts", x + 12, y + 114, new Color(255, 255, 255, 255).getRGB());
                int i = 0;
                GL11.glPushMatrix();
                GL11.glEnable(3089);
                RenderUtils.makeScissorBox(x + 120, y + 20, x + 500, y + 275);
                List<ScriptSubscribe> subscribes = Subscriptions.subscribes;
                for (ScriptSubscribe script : subscribes) {
                    CFontRenderer.DisplayFonts(FontLoaders.C18, script.name, x + 134, y - scroll + 40 + (i * 30), script.state ? new Color(255, 255, 255).getRGB() : new Color(180, 180, 180).getRGB());
                    CFontRenderer.DisplayFonts(FontLoaders.C16, "§aOnline §f| §7" + script.url, x + 134, y - scroll + 52 + (i * 30), new Color(255, 255, 255).getRGB());
                    if (i + 1 != subscribes.size())
                        SmoothRenderUtils.drawRect(x + 130, y - scroll + 64.8 + (i * 30), x + 490, y - scroll + 65 + (i * 30), new Color(211, 211, 211, 95).getRGB());
                    if (i != subscribes.size())
                        SmoothRenderUtils.drawRoundRect(x + 450, y - scroll + 41 + (i * 30), x + 490, y - scroll + 59 + (i * 30), 3f, new Color(29, 155, 240, 255).getRGB());
                    if (i != subscribes.size())
                        CFontRenderer.DisplayFonts(FontLoaders.C16, script.state ? "Remove" : "Unload", x + 455 + (script.state ? -1 : 0), y - scroll + 46 + (i * 30), !script.state ? new Color(220, 220, 220, 200).getRGB() : new Color(255, 255, 255, 255).getRGB());
                    i++;
                }
                GL11.glDisable(3089);
                GL11.glPopMatrix();
                RenderUtils.drawGradientSidewaysV(x + 120, y + 20, x + 500, y + 30, new Color(24, 24, 24).getRGB(), new Color(24, 24, 24, 0).getRGB());
                RenderUtils.drawGradientSidewaysV(x + 120, y + 265, x + 500, y + 275, new Color(24, 24, 24, 0).getRGB(), new Color(24, 24, 24).getRGB());
                RenderUtils.drawRect(x + 120, y + 274f, x + 500f, y + 274.4f, new Color(176, 176, 176, 89).getRGB());
                SmoothRenderUtils.drawRoundRect(x + 450, y + 280, x + 490, y + 295, 3f, new Color(29, 155, 240, 255).getRGB());
                CFontRenderer.DisplayFonts(FontLoaders.C16, "Add", x + 459, y + 284, new Color(255, 255, 255, 255).getRGB());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (translated) {
            translated = false;
        }
    }

    public void mouseScroll(int mouseX, int mouseY, int amount) {
        if (mouseX > x - 120 && mouseX < x + 450 && mouseY > y + 30 && mouseY < y + 280) {
            scrollTo = (int) ((float) scrollTo - (amount / 120 * 28));
            scroll = scroll + (amount / 120 * -5);
        }


    }
}
