/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EntityDamageEvent;
import net.ccbluex.liquidbounce.event.EntityMovementEvent;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.exploit.PackSpoofer;
import net.ccbluex.liquidbounce.features.module.modules.other.NoRotateSet;
import net.ccbluex.liquidbounce.handler.network.ClientFixes;
import net.ccbluex.liquidbounce.handler.spoof.ClientBrandRetriever;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.TransferUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.*;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.ccbluex.liquidbounce.handler.script.api.global.Chat.alert;

/**
 * The type Mixin net handler play client.
 */
@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient {

    /**
     * The Current server max players.
     */
    @Shadow
    public int currentServerMaxPlayers;
    @Shadow
    public boolean doneLoadingTerrain;
    @Shadow
    @Final
    private NetworkManager netManager;
    @Shadow
    private Minecraft gameController;
    @Shadow
    private WorldClient clientWorldController;

    /**
     * Gets player info.
     *
     * @param p_175102_1_ the p 175102 1
     * @return the player info
     */
    @Shadow
    public abstract NetworkPlayerInfo getPlayerInfo(UUID p_175102_1_);


    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private void handleResourcePack(final S48PacketResourcePackSend p_handleResourcePack_1_, final CallbackInfo callbackInfo) {
        final String url = p_handleResourcePack_1_.getURL();
        final String hash = p_handleResourcePack_1_.getHash();

        final PackSpoofer ps = FDPClient.moduleManager.getModule(PackSpoofer.class);

        if (ClientFixes.blockResourcePackExploit) {
            try {
                final String scheme = new URI(url).getScheme();
                final boolean isLevelProtocol = "level".equals(scheme);

                if(!"http".equals(scheme) && !"https".equals(scheme) && !isLevelProtocol)
                    throw new URISyntaxException(url, "Wrong protocol");

                if(isLevelProtocol && (url.contains("..") || !url.endsWith(".zip"))) {
                    String s2 = url.substring("level://".length());
                    File file1 = new File(this.gameController.mcDataDir, "saves");
                    File file2 = new File(file1, s2);

                    if (file2.isFile() && !url.toLowerCase().contains("fdpclient")) {
                        netManager.sendPacket(new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.ACCEPTED));
                        netManager.sendPacket(new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                    } else {
                        netManager.sendPacket(new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                    }

                    if (Objects.requireNonNull(ps).getState() && ps.getNotifyValue().get()) {
                        alert("§7[§b!§7] §b§lFDPCLIENT §c» §6Resourcepack exploit detected.");
                        alert("§7[§b!§7] §b§lFDPCLIENT §c» §7Exploit target directory: §r" + url);

                        throw new IllegalStateException("Invalid levelstorage resourcepack path");
                    } else {
                        callbackInfo.cancel(); // despite not having it enabled we still prevents anything from illegally checking files in your computer.
                    }

                }
            } catch (final URISyntaxException e) {
                alert("Failed to handle resource pack");
                netManager.sendPacket(new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                callbackInfo.cancel();
            } catch (final IllegalStateException e) {
                alert("Failed to handle resource pack");
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "handleEntityStatus", at = @At("HEAD"))
    public void handleDamagePacket(S19PacketEntityStatus packetIn, CallbackInfo callbackInfo) {
        if (packetIn.getOpCode() == 2) {
            Entity entity = packetIn.getEntity(this.clientWorldController);
            if (entity != null) {
                FDPClient.eventManager.callEvent(new EntityDamageEvent(entity));
                if (entity instanceof EntityPlayer)
                    FDPClient.hud.handleDamage((EntityPlayer) entity);
            }
        }
    }

    @Inject(method = "handleSpawnPlayer", at = @At("HEAD"), cancellable = true)
    private void handleSpawnPlayer(S0CPacketSpawnPlayer packetIn, CallbackInfo callbackInfo) {
        try {
            PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
            double d0 = (double) packetIn.getX() / 32.0D;
            double d1 = (double) packetIn.getY() / 32.0D;
            double d2 = (double) packetIn.getZ() / 32.0D;
            float f = (float) (packetIn.getYaw() * 360) / 256.0F;
            float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;
            EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(gameController.theWorld, getPlayerInfo(packetIn.getPlayer()).getGameProfile());
            entityotherplayermp.prevPosX = entityotherplayermp.lastTickPosX = entityotherplayermp.serverPosX = packetIn.getX();
            entityotherplayermp.prevPosY = entityotherplayermp.lastTickPosY = entityotherplayermp.serverPosY = packetIn.getY();
            entityotherplayermp.prevPosZ = entityotherplayermp.lastTickPosZ = entityotherplayermp.serverPosZ = packetIn.getZ();
            int i = packetIn.getCurrentItemID();

            if (i == 0) {
                entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = null;
            } else {
                entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = new ItemStack(Item.getItemById(i), 1, 0);
            }

            entityotherplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
            clientWorldController.addEntityToWorld(packetIn.getEntityID(), entityotherplayermp);
            List<DataWatcher.WatchableObject> list = packetIn.func_148944_c();

            if (list != null) {
                entityotherplayermp.getDataWatcher().updateWatchedObjectsFromList(list);
            }
        } catch (Exception e) {
            // ignore
        }
        callbackInfo.cancel();
    }

    @Inject(method = "handleJoinGame", at = @At("HEAD"), cancellable = true)
    private void handleJoinGameWithAntiForge(S01PacketJoinGame packetIn, final CallbackInfo callbackInfo) {
        if (MinecraftInstance.mc.isIntegratedServerRunning())
            return;

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, gameController);
        this.gameController.playerController = new PlayerControllerMP(gameController, (NetHandlerPlayClient) (Object) this);
        this.clientWorldController = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.gameController.mcProfiler);
        this.gameController.gameSettings.difficulty = packetIn.getDifficulty();
        this.gameController.loadWorld(this.clientWorldController);
        this.gameController.thePlayer.dimension = packetIn.getDimension();
        this.gameController.displayGuiScreen(new GuiDownloadTerrain((NetHandlerPlayClient) (Object) this));
        this.gameController.thePlayer.setEntityId(packetIn.getEntityId());
        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
        this.gameController.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
        this.gameController.playerController.setGameType(packetIn.getGameType());
        this.gameController.gameSettings.sendSettingsToServer();
        this.netManager.sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
        callbackInfo.cancel();
    }

    @Inject(method = "handleEntityMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z"))
    private void handleEntityMovementEvent(S14PacketEntity packetIn, final CallbackInfo callbackInfo) {
        final Entity entity = packetIn.getEntity(this.clientWorldController);

        if (entity != null)
            FDPClient.eventManager.callEvent(new EntityMovementEvent(entity));
    }

    /**
     * @author liulihaocai
     * Fixed by Co Dynamic
     * @reason Convert Explosion to Velocity
     */
    @Overwrite
    public void handleExplosion(S27PacketExplosion packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.gameController);
        Explosion explosion = new Explosion(this.gameController.theWorld, null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
        explosion.doExplosionB(true);
        // convert it to velocity packet
        // ONLY when it's a valid explosion (in affected range)
        if (!(Math.abs(packetIn.func_149149_c() * 8000.0) < 0.0001 && Math.abs(packetIn.func_149144_d() * 8000.0) < 0.0001 && Math.abs(packetIn.func_149147_e() * 8000.0) < 0.0001)) {
            S12PacketEntityVelocity packet = new S12PacketEntityVelocity(this.gameController.thePlayer.getEntityId(),
                    (this.gameController.thePlayer.motionX + packetIn.func_149149_c()) * 8000.0,
                    (this.gameController.thePlayer.motionY + packetIn.func_149144_d()) * 8000.0,
                    (this.gameController.thePlayer.motionZ + packetIn.func_149147_e()) * 8000.0);
            PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.RECEIVE);
            FDPClient.eventManager.callEvent(packetEvent);
            if (!packetEvent.isCancelled()) {
                handleEntityVelocity(packet);
            }
        }
    }

    @Redirect(
            method = "handleUpdateSign",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=Unable to locate sign at ", ordinal = 0)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;addChatMessage(Lnet/minecraft/util/IChatComponent;)V", ordinal = 0)
    )
    private void removeDebugMessage(EntityPlayerSP instance, IChatComponent component) {

    }

    @Inject(method = {"handleAnimation"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleAnimation(S0BPacketAnimation s0BPacketAnimation, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleEntityTeleport"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleEntityTeleport(S18PacketEntityTeleport s18PacketEntityTeleport, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleEntityMovement"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleEntityMovement(S14PacketEntity s14PacketEntity, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleEntityHeadLook"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleEntityHeadLook(S19PacketEntityHeadLook s19PacketEntityHeadLook, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleEntityProperties"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleEntityProperties(S20PacketEntityProperties s20PacketEntityProperties, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleEntityMetadata"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleEntityMetadata(S1CPacketEntityMetadata s1CPacketEntityMetadata, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleEntityEquipment"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleEntityEquipment(S04PacketEntityEquipment s04PacketEntityEquipment, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleDestroyEntities"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleDestroyEntities(S13PacketDestroyEntities s13PacketDestroyEntities, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleScoreboardObjective"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void handleScoreboardObjective(S3BPacketScoreboardObjective s3BPacketScoreboardObjective, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.clientWorldController, callbackInfo);
    }

    @Inject(method = {"handleConfirmTransaction"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S32PacketConfirmTransaction;getWindowId()I", ordinal = 0)}, cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void handleConfirmTransaction(S32PacketConfirmTransaction s32PacketConfirmTransaction, CallbackInfo callbackInfo, Container container, EntityPlayer entityPlayer) {
        this.fDPClient$cancelIfNull(entityPlayer, callbackInfo);
    }

    @Inject(method = {"handleSoundEffect"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V")}, cancellable = true)
    private void handleSoundEffect(S29PacketSoundEffect s29PacketSoundEffect, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.gameController.theWorld, callbackInfo);
    }

    @Inject(method = {"handleTimeUpdate"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V")}, cancellable = true)
    private void handleTimeUpdate(S03PacketTimeUpdate s03PacketTimeUpdate, CallbackInfo callbackInfo) {
        this.fDPClient$cancelIfNull(this.gameController.theWorld, callbackInfo);
    }

    /**
     * @author Co Dynamic
     * @reason NoRotateSet / S08 Silent Confirm
     */
    @Overwrite
    public void handlePlayerPosLook(S08PacketPlayerPosLook packetIn) {
        final NoRotateSet noRotateSet = FDPClient.moduleManager.getModule(NoRotateSet.class);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.gameController);
        EntityPlayer entityplayer = this.gameController.thePlayer;
        double d0 = packetIn.getX();
        double d1 = packetIn.getY();
        double d2 = packetIn.getZ();
        float f = packetIn.getYaw();
        float f1 = packetIn.getPitch();

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X)) {
            d0 += entityplayer.posX;
        } else {
            if (!TransferUtils.INSTANCE.getNoMotionSet()) {
                entityplayer.motionX = 0.0D;
            }
        }

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y)) {
            d1 += entityplayer.posY;
        } else {
            if (!TransferUtils.INSTANCE.getNoMotionSet()) {
                entityplayer.motionY = 0.0D;
            }
        }

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z)) {
            d2 += entityplayer.posZ;
        } else {
            if (!TransferUtils.INSTANCE.getNoMotionSet()) {
                entityplayer.motionZ = 0.0D;
            }
        }

        TransferUtils.INSTANCE.setNoMotionSet(false);

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X_ROT)) {
            f1 += entityplayer.rotationPitch;
        }

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT)) {
            f += entityplayer.rotationYaw;
        }

        float overwriteYaw = f;
        float overwritePitch = f1;

        boolean flag = false;

        if (TransferUtils.INSTANCE.getSilentConfirm()) {
            this.netManager.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(d0, d1, d2, f, f1, false));
            TransferUtils.INSTANCE.setSilentConfirm(false);
        } else {
            if (Objects.requireNonNull(noRotateSet).getState()) {
                if (!noRotateSet.getNoLoadingValue().get() || this.doneLoadingTerrain) {
                    flag = true;
                    if (!noRotateSet.getOverwriteTeleportValue().get()) {
                        overwriteYaw = entityplayer.rotationYaw;
                        overwritePitch = entityplayer.rotationPitch;
                    }
                }
            }
            if (flag) {
                if (noRotateSet.getRotateValue().get()) {
                    entityplayer.setPositionAndRotation(d0, d1, d2, entityplayer.rotationYaw, entityplayer.rotationPitch);
                } else {
                    entityplayer.setPosition(d0, d1, d2);
                }
            } else {
                entityplayer.setPositionAndRotation(d0, d1, d2, f, f1);
            }
            this.netManager.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(entityplayer.posX, entityplayer.getEntityBoundingBox().minY, entityplayer.posZ, overwriteYaw, overwritePitch, false));
        }

        if (!this.doneLoadingTerrain) {
            this.gameController.thePlayer.prevPosX = this.gameController.thePlayer.posX;
            this.gameController.thePlayer.prevPosY = this.gameController.thePlayer.posY;
            this.gameController.thePlayer.prevPosZ = this.gameController.thePlayer.posZ;
            this.doneLoadingTerrain = true;
            this.gameController.displayGuiScreen(null);
        }
    }

    private <T> void fDPClient$cancelIfNull(T t, CallbackInfo callbackInfo) {
        if (t == null) {
            callbackInfo.cancel();
        }
    }
}
