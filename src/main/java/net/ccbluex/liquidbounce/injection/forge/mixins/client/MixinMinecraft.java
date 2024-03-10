/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.client.SoundModule;
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.features.module.modules.player.DelayRemover;
import net.ccbluex.liquidbounce.features.module.modules.exploit.MultiActions;
import net.ccbluex.liquidbounce.handler.protocol.api.ProtocolFixes;
import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.ccbluex.liquidbounce.injection.forge.mixins.accessors.MinecraftForgeClientAccessor;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolMod;
import net.ccbluex.liquidbounce.utils.*;
import net.ccbluex.liquidbounce.utils.CPSCounterUtils;
import net.ccbluex.liquidbounce.utils.render.IconUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.block.material.Material;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.IStream;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Util;
import net.minecraftforge.client.MinecraftForgeClient;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;
import java.util.Objects;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public GuiScreen currentScreen;
    @Shadow
    public boolean skipRenderWorld;
    @Shadow
    public MovingObjectPosition objectMouseOver;
    @Shadow
    public WorldClient theWorld;
    @Shadow
    public EntityPlayerSP thePlayer;
    @Shadow
    public EffectRenderer effectRenderer;
    @Shadow
    public EntityRenderer entityRenderer;
    @Shadow
    public PlayerControllerMP playerController;
    @Shadow
    public int displayWidth;
    @Shadow
    public int displayHeight;
    @Shadow
    public int rightClickDelayTimer;
    @Shadow
    public GameSettings gameSettings;
    @Shadow
    private Entity renderViewEntity;
    @Shadow
    private boolean fullscreen;
    @Shadow
    public int leftClickCounter;
    private long lastFrame = Minecraft.getSystemTime();

    @Shadow
    public abstract IResourceManager getResourceManager();

    @Shadow
    public abstract RenderManager getRenderManager();

    @Inject(method = "toggleFullscreen", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setFullscreen(Z)V", remap = false))
    private void resolveScreenState(CallbackInfo ci) {
        if (!this.fullscreen && SystemUtils.IS_OS_WINDOWS) {
            Display.setResizable(false);
            Display.setResizable(true);
        }
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void init(CallbackInfo callbackInfo) {
        if (displayWidth < 1067)
            displayWidth = 1067;

        if (displayHeight < 622)
            displayHeight = 622;
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void startGame(CallbackInfo callbackInfo) {
        FDPClient.INSTANCE.initClient();
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.AFTER))
    public void step1(CallbackInfo ci) {
        SplashProgress.INSTANCE.setProgress(1, "textures");
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    public void step2(CallbackInfo ci) {
        SplashProgress.INSTANCE.setProgress(3, "Gui");
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void startVia(GameConfiguration p_i45547_1_, CallbackInfo ci) {
        ProtocolBase.init(ProtocolMod.PLATFORM);
    }

    @Inject(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void createDisplay(CallbackInfo callbackInfo) {
        ClientUtils.INSTANCE.setTitle();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void clearLoadedMaps(WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        if (worldClientIn != this.theWorld) {
            this.entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(
            method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER)
    )
    private void clearRenderCache(CallbackInfo ci) {
        //noinspection ResultOfMethodCallIgnored
        MinecraftForgeClient.getRenderPass(); // Ensure class is loaded, strange accessor issue
        MinecraftForgeClientAccessor.getRegionCache().invalidateAll();
        MinecraftForgeClientAccessor.getRegionCache().cleanUp();
    }

    @Redirect(
            method = "runGameLoop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/stream/IStream;func_152935_j()V")
    )
    private void skipTwitchCode1(IStream instance) {
        // No-op
    }

    @Redirect(
            method = "runGameLoop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/stream/IStream;func_152922_k()V")
    )
    private void skipTwitchCode2(IStream instance) {
        // No-op
    }

    @Inject(method = "displayCrashReport", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;getFile()Ljava/io/File;"))
    public void displayCrashReport(CrashReport crashReportIn, CallbackInfo ci) {
        String message = crashReportIn.getCauseStackTraceOrString();
        JOptionPane.showMessageDialog(null, "Game crashed!\n" +
                        "Please create a issue: \n" + GitUtils.gitInfo.get("git.remote.origin.url").toString().split("\\.git")[0] + "/issues/new\n" +
                        "Please make a screenshot of this screen and send it to developers\n"
                        + message,
                "oops, game crashed!", JOptionPane.ERROR_MESSAGE);
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void displayGuiScreen(CallbackInfo callbackInfo) {
        if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || (currentScreen != null && currentScreen.getClass().getSimpleName().equals("ModGuiMainMenu"))) {
            currentScreen =  FDPClient.mainMenu;

            ScaledResolution scaledResolution = new ScaledResolution(MinecraftInstance.mc);
            currentScreen.setWorldAndResolution(MinecraftInstance.mc, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
            skipRenderWorld = false;
        }

        FDPClient.eventManager.callEvent(new ScreenEvent(currentScreen));
    }

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void runGameLoop(final CallbackInfo callbackInfo) {
        final long currentTime = Minecraft.getSystemTime();
        final int deltaTime = (int) (currentTime - lastFrame);
        lastFrame = currentTime;

        RenderUtils.deltaTime = deltaTime;
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void runTick(final CallbackInfo callbackInfo) {
        StaticStorage.scaledResolution = new ScaledResolution((Minecraft)(Object) this);
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;joinPlayerCounter:I", shift = At.Shift.BEFORE))
    private void onTick(final CallbackInfo callbackInfo) {
        FDPClient.eventManager.callEvent(new TickEvent());
    }

    @Inject(method = "dispatchKeypresses", at = @At(value = "HEAD"))
    private void onKey(CallbackInfo callbackInfo) {
        try {
            if (shouldDispatchEvent()) {
                FDPClient.eventManager.callEvent(new KeyEvent(getEventKeyOrCharacter()));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private boolean shouldDispatchEvent() {
        return Keyboard.getEventKeyState() && (currentScreen == null || (SoundModule.INSTANCE.getToggleIgnoreScreenValue().get() && this.currentScreen instanceof GuiContainer));
    }

    private int getEventKeyOrCharacter() {
        return Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() : Keyboard.getEventKey();
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

    /**
     * @author opZywl
     * @reason Fix Attack Order
     */
    @Overwrite
    public void clickMouse() {
        CPSCounterUtils.registerClick(CPSCounterUtils.MouseButton.LEFT);
        if (this.leftClickCounter <= 0) {
            if (this.objectMouseOver != null && Objects.requireNonNull(this.objectMouseOver.typeOfHit) != MovingObjectPosition.MovingObjectType.ENTITY) {
                this.thePlayer.swingItem();
            }

            if (this.objectMouseOver != null) {
                switch (this.objectMouseOver.typeOfHit) {
                    case ENTITY:
                        ProtocolFixes.sendFixedAttack(this.thePlayer, this.objectMouseOver.entityHit);
                        break;

                    case BLOCK:
                        BlockPos blockpos = this.objectMouseOver.getBlockPos();

                        if (this.theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air) {
                            this.playerController.clickBlock(blockpos, this.objectMouseOver.sideHit);
                            break;
                        }

                    case MISS:
                    default:
                        if (this.playerController.isNotCreative() && !Objects.requireNonNull(FDPClient.moduleManager.getModule(DelayRemover.class)).getState() && FDPClient.moduleManager.getModule(DelayRemover.class).getNoClickDelay().get()) {
                            this.leftClickCounter = 0;
                        }
                }
            }
        }
    }
    @Inject(method = "middleClickMouse", at = @At("HEAD"))
    private void middleClickMouse(CallbackInfo ci) {
        CPSCounterUtils.registerClick(CPSCounterUtils.MouseButton.MIDDLE);
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER))
    private void rightClickMouse(final CallbackInfo callbackInfo) {
        CPSCounterUtils.registerClick(CPSCounterUtils.MouseButton.RIGHT);

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
            if (Objects.requireNonNull(rotations).getHeadValue().get()) {
                thePlayer.rotationYawHead = yaw;
            }
            if (rotations.getBodyValue().get()) {
                thePlayer.renderYawOffset = yaw;
            }
        }
    }

    @Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
    private char resolveForeignKeyboards() {
        return (char) (Keyboard.getEventCharacter() + 256);
    }

    /**
     * @author opZywl
     * @reason Fix ClickDelay
     */
    @Overwrite
    private void sendClickBlockToController(boolean leftClick) {
        if (!leftClick)
            this.leftClickCounter = 0;

        if (this.leftClickCounter <= 0 && (!this.thePlayer.isUsingItem() || Objects.requireNonNull(FDPClient.moduleManager.getModule(MultiActions.class)).getState())) {
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

    @Inject(method = "setWindowIcon", at = @At("HEAD"), cancellable = true)
    private void setWindowIcon(CallbackInfo callbackInfo) {
      //  try {
            if (Util.getOSType() == Util.EnumOS.OSX) {
                MacOS.icon();
                Display.setIcon(IconUtils.fav());
            }
    /*
             else {
                BufferedImage image = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/minecraft/fdpclient/misc/32.png"))); // need to impliment 16x and 64x
                ByteBuffer bytebuffer = ImageUtils.readImageToBuffer(image); // What the fuck? ImageUtils.resizeImage(image, 16, 16)
                if (bytebuffer == null) {
                    throw new Exception("Error when loading image.");
                } else {
                    Display.setIcon(new ByteBuffer[]{bytebuffer, ImageUtils.readImageToBuffer(image)});
                    callbackInfo.cancel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

     */
    }

    @Redirect(method="loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/LoadingScreenRenderer;resetProgressAndMessage(Ljava/lang/String;)V"))
    public void loadWorld(LoadingScreenRenderer loadingScreenRenderer, String string) {
    }

    @Redirect(method="loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/LoadingScreenRenderer;displayLoadingString(Ljava/lang/String;)V"))
    public void loadWorld1(LoadingScreenRenderer loadingScreenRenderer, String string) {
    }

    @Redirect(method="loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at=@At(value="INVOKE", target="Ljava/lang/System;gc()V", remap=false))
    public void loadWorld2() {
    }

    @Inject(method="toggleFullscreen()V", at=@At(value="INVOKE", target="Lorg/lwjgl/opengl/Display;setFullscreen(Z)V", shift=At.Shift.AFTER, remap=false), require=1, allow=1)
    private void toggleFullscreen(CallbackInfo callbackInfo) {
        if (!this.fullscreen) {
            Display.setResizable(false);
            Display.setResizable(true);
        }
    }

    @ModifyConstant(method = "getLimitFramerate", constant = @Constant(intValue = 30))
    public int getLimitFramerate(int constant) {
        return 60;
    }
}
