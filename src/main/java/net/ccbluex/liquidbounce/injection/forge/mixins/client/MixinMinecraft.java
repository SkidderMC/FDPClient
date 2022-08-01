/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import com.guimc.fuckpcl.PCLChecker;
import com.guimc.fuckpcl.utils.WindowUtils;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.client.Modules;
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker;
import net.ccbluex.liquidbounce.features.module.modules.world.FastPlace;
import net.ccbluex.liquidbounce.features.module.modules.exploit.MultiActions;
import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.ccbluex.liquidbounce.utils.*;
import net.ccbluex.liquidbounce.utils.misc.MiscUtils;
import net.ccbluex.liquidbounce.utils.render.ImageUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.block.material.Material;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Util;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.AccessDeniedException;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public GuiScreen currentScreen;

    @Shadow
    public boolean skipRenderWorld;

    @Shadow
    private int leftClickCounter;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow
    public EffectRenderer effectRenderer;

    @Shadow
    public PlayerControllerMP playerController;

    @Shadow
    public int rightClickDelayTimer;

    @Shadow
    public GameSettings gameSettings;

    @Shadow
    @Final
    public File mcDataDir;

    @Shadow
    private boolean fullscreen;

    /**
     * @author Zywl
     * @reason
     */
    @Overwrite
    public int getLimitFramerate() {
        return this.gameSettings.limitFramerate;
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void startGame(CallbackInfo callbackInfo) throws AccessDeniedException {
        if (PCLChecker.INSTANCE.fullCheck(this.mcDataDir)) {
            Display.destroy();
            String warnStr = "Plain Craft Launcher is NOT supported with this client, please switch another Minecraft Launcher!";
            MiscUtils.INSTANCE.showErrorPopup(warnStr);
            throw new AccessDeniedException(warnStr);
        }
        FDPClient.INSTANCE.initClient();
    }

    @Inject(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void createDisplay(CallbackInfo callbackInfo) {
        ClientUtils.INSTANCE.setTitle();
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void displayGuiScreen(CallbackInfo callbackInfo) {
        if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || (currentScreen != null && currentScreen.getClass().getName().startsWith("net.labymod") && currentScreen.getClass().getSimpleName().equals("ModGuiMainMenu"))) {
            currentScreen = FDPClient.mainMenu;

            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
            skipRenderWorld = false;
        }

        FDPClient.eventManager.callEvent(new ScreenEvent(currentScreen));
    }

    private long lastFrame = getTime();

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void runGameLoop(final CallbackInfo callbackInfo) {
        final long currentTime = getTime();
        final int deltaTime = (int) (currentTime - lastFrame);
        lastFrame = currentTime;

        RenderUtils.deltaTime = deltaTime;
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void runTick(final CallbackInfo callbackInfo) {
        StaticStorage.scaledResolution = new ScaledResolution((Minecraft) (Object) this);
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;joinPlayerCounter:I", shift = At.Shift.BEFORE))
    private void onTick(final CallbackInfo callbackInfo) {
        FDPClient.eventManager.callEvent(new TickEvent());
    }

    @Inject(method = "dispatchKeypresses", at = @At(value = "HEAD"))
    private void onKey(CallbackInfo callbackInfo) {
        try {
            if (Keyboard.getEventKeyState() && (currentScreen == null || (Modules.INSTANCE.getToggleIgnoreScreenValue().get() && this.currentScreen instanceof GuiContainer)))
                FDPClient.eventManager.callEvent(new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovingObjectPosition;getBlockPos()Lnet/minecraft/util/BlockPos;"))
    private void onClickBlock(CallbackInfo callbackInfo) {
        if (this.leftClickCounter == 0 && theWorld.getBlockState(objectMouseOver.getBlockPos()).getBlock().getMaterial() != Material.air) {
            FDPClient.eventManager.callEvent(new ClickBlockEvent(objectMouseOver.getBlockPos(), this.objectMouseOver.sideHit));
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void shutdown(CallbackInfo callbackInfo) {
        FDPClient.INSTANCE.stopClient();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouse(CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.LEFT);
        if (FDPClient.moduleManager.getModule(AutoClicker.class).getState())
            leftClickCounter = 0; // fix hit delay lol
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"))
    private void middleClickMouse(CallbackInfo ci) {
        CPSCounter.registerClick(CPSCounter.MouseButton.MIDDLE);
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER))
    private void rightClickMouse(final CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT);

        final FastPlace fastPlace = FDPClient.moduleManager.getModule(FastPlace.class);

        if (fastPlace.getState())
            rightClickDelayTimer = fastPlace.getSpeedValue().get();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void loadWorld(WorldClient p_loadWorld_1_, String p_loadWorld_2_, final CallbackInfo callbackInfo) {
        FDPClient.eventManager.callEvent(new WorldEvent(p_loadWorld_1_));
    }

    @Inject(method = "getRenderViewEntity", at = @At("HEAD"))
    public void getRenderViewEntity(CallbackInfoReturnable<Entity> cir) {
        if (RotationUtils.targetRotation != null && thePlayer != null) {
            final Rotations rotations = FDPClient.moduleManager.getModule(Rotations.class);
            final float yaw = RotationUtils.targetRotation.getYaw();
            if (rotations.getHeadValue().get()) {
                thePlayer.rotationYawHead = yaw;
            }
            if (rotations.getBodyValue().get()) {
                thePlayer.renderYawOffset = yaw;
            }
        }
    }

    /**
     * @author CCBlueX
     * @reason
     */
    @Overwrite
    private void sendClickBlockToController(boolean leftClick) {
        if (!leftClick)
            this.leftClickCounter = 0;

        if (this.leftClickCounter <= 0 && (!this.thePlayer.isUsingItem() || FDPClient.moduleManager.getModule(MultiActions.class).getState())) {
            if (leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockPos = this.objectMouseOver.getBlockPos();

                if (this.leftClickCounter == 0)
                    FDPClient.eventManager.callEvent(new ClickBlockEvent(blockPos, this.objectMouseOver.sideHit));


                if (this.theWorld.getBlockState(blockPos).getBlock().getMaterial() != Material.air && this.playerController.onPlayerDamageBlock(blockPos, this.objectMouseOver.sideHit)) {
                    this.effectRenderer.addBlockHitEffects(blockPos, this.objectMouseOver.sideHit);
                    this.thePlayer.swingItem();
                }
            } else {
                this.playerController.resetBlockRemoving();
            }
        }
    }

    @Inject(method = "displayCrashReport", at = @At("HEAD"))
    private void displayCrashReport(CrashReport crashReport, CallbackInfo ci) {
        if (!WindowUtils.isWindows()) return;
        try {
            File file = new File("./", "FDPCrashLogs.txt");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            fileWritter.write("######################### FDP CRASH REPORT #########################\r\n\r\n" +
                    "If this problem persists, please send this file to the FDPClient developers! Website (where you can join the discord server): http://fdpinfo.github.io/\r\nThis file will be saved in \".minecraft/FDPCrashLogs.txt\"" +
                    "\r\n\r\n" +
                    " | 在没有错误日志的情况下诊断任何问题无异于闭眼开车!  --Apache官方文档\r\n" +
                    " | Troubleshooting any problem without the error log is like driving with your eyes closed.\r\n" +
                    " | From Apache official documentation Getting Started chapter\r\n" +
                    "   - INFO:\r\n" +
                    "   |   Version: " + FDPClient.CLIENT_VERSION + "\r\n" +
                    "   |   Time: " + System.currentTimeMillis() + "\r\n" +
                    "   |   OS: " + Util.getOSType() + "\r\n" +
                    "\r\n######################### FDP CRASH REPORT #########################\r\n" + crashReport.getCompleteReport());
            fileWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file1 = new File("./", "FDPCrashLogs.txt");
        ;
        String s = file1.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX) {
            try {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", s});
                return;
            } catch (IOException ioexception1) {
                ioexception1.printStackTrace();
            }
        } else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[]{s});

            try {
                Runtime.getRuntime().exec(s1);
                return;
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }

        boolean flag = false;

        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
            oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new Object[]{file1.toURI()});
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            flag = true;
        }

        if (flag) {
            Sys.openURL("file://" + s);
        }
    }

    @Inject(method = "setWindowIcon", at = @At("HEAD"), cancellable = true)
    private void setWindowIcon(CallbackInfo callbackInfo) throws IOException {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("/assets/minecraft/fdpclient/misc/icon.png"));
            if (image.getWidth() != 32 || image.getHeight() != 32) {
                image = ImageUtils.resizeImage(image, 32, 32);
            }
            Display.setIcon(new ByteBuffer[]{ImageUtils.readImageToBuffer(ImageUtils.resizeImage(image, 16, 16)),
                    ImageUtils.readImageToBuffer(image)});
            callbackInfo.cancel();
        }
    }

    @Redirect(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/LoadingScreenRenderer;resetProgressAndMessage(Ljava/lang/String;)V"))
    public void loadWorld(LoadingScreenRenderer loadingScreenRenderer, String string) {
    }

    @Redirect(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/LoadingScreenRenderer;displayLoadingString(Ljava/lang/String;)V"))
    public void loadWorld1(LoadingScreenRenderer loadingScreenRenderer, String string) {
    }

    @Redirect(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V", remap = false))
    public void loadWorld2() {
    }

    @Inject(method = "toggleFullscreen()V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setFullscreen(Z)V", shift = At.Shift.AFTER, remap = false), require = 1, allow = 1)
    private void toggleFullscreen(CallbackInfo callbackInfo) {
        if (!this.fullscreen) {
            Display.setResizable(false);
            Display.setResizable(true);
        }
    }
}
