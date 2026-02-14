/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.combat.HitSelect;
import net.ccbluex.liquidbounce.features.module.modules.combat.TickBase;
import net.ccbluex.liquidbounce.features.module.modules.other.FastPlace;
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration;
import net.ccbluex.liquidbounce.handler.api.ClientUpdate;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AbortBreaking;
import net.ccbluex.liquidbounce.features.module.modules.exploit.MultiActions;
import net.ccbluex.liquidbounce.injection.forge.SplashProgressLock;
import net.ccbluex.liquidbounce.ui.client.gui.GuiMainMenu;
import net.ccbluex.liquidbounce.ui.client.gui.GuiUpdate;
import net.ccbluex.liquidbounce.utils.attack.CPSCounter;
import net.ccbluex.liquidbounce.utils.client.ClientUtils;
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar;
import net.ccbluex.liquidbounce.utils.io.MiscUtils;
import net.ccbluex.liquidbounce.utils.movement.BPSUtils;
import net.ccbluex.liquidbounce.utils.render.IconUtils;
import net.ccbluex.liquidbounce.utils.render.MiniMapRegister;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Util;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Mixin(Minecraft.class)
@SideOnly(Side.CLIENT)
public abstract class MixinMinecraft {

    @Shadow
    public GuiScreen currentScreen;

    @Shadow
    public boolean skipRenderWorld;

    @Shadow
    public int leftClickCounter;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public EntityPlayerSP thePlayer;

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
    public abstract void displayGuiScreen(GuiScreen guiScreenIn);

    @Unique
    private CompletableFuture<?> liquidBounce$preloadFuture;

    @Inject(method = "run", at = @At("HEAD"))
    private void init(CallbackInfo callbackInfo) {
        if (displayWidth < 1067) displayWidth = 1067;

        if (displayHeight < 622) displayHeight = 622;

        liquidBounce$preloadFuture = FDPClient.INSTANCE.preload();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 1))
    private void hook(CallbackInfo ci) {
        EventManager.INSTANCE.call(GameLoopEvent.INSTANCE);
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void startGame(CallbackInfo callbackInfo) throws InterruptedException {
        try {
            liquidBounce$preloadFuture.get();
        } catch (ExecutionException e) {
            final String message = "Preload task error. Please check the cause below.";
            ClientUtils.INSTANCE.getLOGGER().error(message, e);
            throw new IllegalStateException(message, e);
        }

        FDPClient.INSTANCE.startClient();
    }

    @Inject(method = "startGame", at = @At(value = "NEW", target = "net/minecraft/client/renderer/texture/TextureManager"))
    private void waitForLock(CallbackInfo ci) {
        long end = System.currentTimeMillis() + 20000;

        while (end < System.currentTimeMillis() && SplashProgressLock.INSTANCE.isAnimationRunning()) {
            synchronized (SplashProgressLock.INSTANCE) {
                try {
                    SplashProgressLock.INSTANCE.wait(10000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V", shift = At.Shift.AFTER))
    private void afterMainScreen(CallbackInfo callbackInfo) {
        if (ClientUpdate.INSTANCE.hasUpdate()) {
            displayGuiScreen(new GuiUpdate());
        }
    }

    @Inject(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", shift = At.Shift.AFTER, remap = false))
    private void createDisplay(CallbackInfo callbackInfo) {
        if (ClientConfiguration.INSTANCE.getClientTitle()) {
            Display.setTitle(FDPClient.INSTANCE.getClientTitle());
        }
    }

    /**
     * AI_Kolbasa Fix: Решаем NoSuchMethodError: ScaledResolution.<init>
     * Используем прямой каст (Minecraft) (Object) this вместо статического mc.
     */
    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void handleDisplayGuiScreen(CallbackInfo callbackInfo) {
        if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || (currentScreen != null && currentScreen.getClass().getName().startsWith("net.labymod") && currentScreen.getClass().getSimpleName().equals("ModGuiMainMenu"))) {
            currentScreen = new GuiMainMenu();

            Minecraft minecraftInstance = (Minecraft) (Object) this;
            ScaledResolution scaledResolution = new ScaledResolution(minecraftInstance);
            
            currentScreen.setWorldAndResolution(minecraftInstance, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
            skipRenderWorld = false;
        }

        EventManager.INSTANCE.call(new ScreenEvent(currentScreen));
    }

    @Unique
    private long fdp$lastFrame = fdp$getTime();

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void runGameLoop(final CallbackInfo callbackInfo) {
        final long currentTime = fdp$getTime();
        final int deltaTime = (int) (currentTime - fdp$lastFrame);
        fdp$lastFrame = currentTime;

        RenderUtils.INSTANCE.setDeltaTime(deltaTime);
    }

    @Unique
    public long fdp$getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void injectGameRuntimeTicks(CallbackInfo ci) {
        ClientUtils.INSTANCE.setRunTimeTicks(ClientUtils.INSTANCE.getRunTimeTicks() + 1);
        SilentHotbar.INSTANCE.updateSilentSlot();
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    private void injectEndTickEvent(CallbackInfo ci) {
        EventManager.INSTANCE.call(TickEndEvent.INSTANCE);
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;joinPlayerCounter:I", ordinal = 0))
    private void onTick(final CallbackInfo callbackInfo) {
        EventManager.INSTANCE.call(GameTickEvent.INSTANCE);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V", shift = At.Shift.AFTER))
    private void onKey(CallbackInfo callbackInfo) {
        int keyCode = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        boolean pressed = Keyboard.getEventKeyState();

        if (currentScreen == null) {
            EventManager.INSTANCE.call(new KeyStateEvent(keyCode, pressed));
        }

        if (pressed && currentScreen == null) {
            EventManager.INSTANCE.call(new KeyEvent(keyCode));
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovingObjectPosition;getBlockPos()Lnet/minecraft/util/BlockPos;"))
    private void onClickBlock(CallbackInfo callbackInfo) {
        final BlockPos blockPos = objectMouseOver.getBlockPos();
        if (leftClickCounter == 0 && theWorld.getBlockState(blockPos).getBlock().getMaterial() != Material.air) {
            EventManager.INSTANCE.call(new ClickBlockEvent(blockPos, objectMouseOver.sideHit));
        }
    }

    @Inject(method = "setWindowIcon", at = @At("HEAD"), cancellable = true)
    private void setWindowIcon(CallbackInfo callbackInfo) {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            if (ClientConfiguration.INSTANCE.getClientTitle()) {
                if (IconUtils.initLwjglIcon()) {
                    callbackInfo.cancel();
                }
            }
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void shutdown(CallbackInfo callbackInfo) {
        FDPClient.INSTANCE.stopClient();
    }

    @Inject(method = "displayCrashReport", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;instance()Lnet/minecraftforge/fml/common/FMLCommonHandler;", remap = false))
    private void injectDisplayCrashReport(CrashReport crashReport, CallbackInfo callbackInfo) {
        MiscUtils.showErrorPopup(crashReport.getCrashCause(), "Game crashed! ", MiscUtils.generateCrashInfo());
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void clickMouse(CallbackInfo callbackInfo) {
        if (HitSelect.shouldCancelClick(objectMouseOver, thePlayer)) {
            callbackInfo.cancel();
            return;
        }

        if (AutoClicker.INSTANCE.handleEvents()) {
            leftClickCounter = 0;
        }

        if (leftClickCounter <= 0) {
            CPSCounter.INSTANCE.registerClick(CPSCounter.MouseButton.LEFT);
        }
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"))
    private void middleClickMouse(CallbackInfo ci) {
        CPSCounter.INSTANCE.registerClick(CPSCounter.MouseButton.MIDDLE);
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER))
    private void rightClickMouse(final CallbackInfo callbackInfo) {
        CPSCounter.INSTANCE.registerClick(CPSCounter.MouseButton.RIGHT);

        final FastPlace fastPlace = FastPlace.INSTANCE;
        if (!fastPlace.handleEvents()) return;

        if (fastPlace.getOnlyBlocks() && (thePlayer.getHeldItem() == null || !(thePlayer.getHeldItem().getItem() instanceof ItemBlock)))
            return;

        if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos blockPos = objectMouseOver.getBlockPos();
            IBlockState blockState = theWorld.getBlockState(blockPos);
            if (blockState.getBlock().hasTileEntity(blockState)) return;
        } else if (fastPlace.getFacingBlocks()) return;

        rightClickDelayTimer = fastPlace.getSpeed();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void loadWorld(WorldClient p_loadWorld_1_, String p_loadWorld_2_, final CallbackInfo callbackInfo) {
        if (theWorld != null) {
            MiniMapRegister.INSTANCE.unloadAllChunks();
        }

        EventManager.INSTANCE.call(new WorldEvent(p_loadWorld_1_));
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z"))
    private boolean injectMultiActions(EntityPlayerSP instance) {
        ItemStack itemStack = instance.itemInUse;
        if (MultiActions.INSTANCE.handleEvents()) itemStack = null;
        return itemStack != null;
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;resetBlockRemoving()V"))
    private void injectAbortBreaking(PlayerControllerMP instance) {
        if (!AbortBreaking.INSTANCE.handleEvents()) {
            instance.resetBlockRemoving();
        }
    }

    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Ljava/util/Queue;isEmpty()Z", remap = false))
    private boolean injectTickBase(Queue instance) {
        return TickBase.INSTANCE.getDuringTickModification() || instance.isEmpty();
    }

    @Redirect(method = {"middleClickMouse", "rightClickMouse"}, at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I"))
    private int injectSilentHotbar(InventoryPlayer instance) {
        return SilentHotbar.INSTANCE.getCurrentSlot();
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;inventory:Lnet/minecraft/entity/player/InventoryPlayer;"))
    private void injectSilentHotbarManualPressDetection(CallbackInfo ci) {
        SilentHotbar.INSTANCE.setPressedAtSlot(true);
    }

    /**
     * @author CCBlueX
     */
    @ModifyConstant(method = "getLimitFramerate", constant = @Constant(intValue = 30))
    public int getLimitFramerate(int constant) {
        return 60;
    }
}
