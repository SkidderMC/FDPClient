/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.ccbluex.liquidbounce.utils.timer.TickTimer;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

// TODO: RECODE IT AS SPEED AND ADD VALUE DISPLAYABLE
@ModuleInfo(name = "Fly", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F, autoDisable = EnumAutoDisableType.FLAG)
public class Fly extends Module {

    public final ListValue modeValue = new ListValue("Mode", new String[]{
            "Vanilla",
            "SmoothVanilla",

            // NCP
            "NCP",
            "OldNCP",

            // RedeSky
            "RedeSkyCollide",
            "RedeSkySmooth",

            // Verus
            "Verus",
            "Verus2",
            "Verus3",

            // AAC
            "AAC1.9.10",
            "AAC3.0.5",
            "AAC3.1.6-Gomme",
            "AAC3.3.12",
            "AAC3.3.12-Glide",
            "AAC3.3.13",
            "AAC4.X-Glide",
            "AAC5.2.0",
            "AAC5.2.0-Fast",
            "AAC5.2.0-Vanilla",
            "AAC5.2.0-Smooth",

            // CubeCraft
            "CubeCraft",

            // Hypixel
            "Hypixel",
            "BoostHypixel",
            "FreeHypixel",
            "HypixelNew",

            // Rewinside
            "Rewinside",
            "TeleportRewinside",

            // Other server specific flys
            "Mineplex",
            "NeruxVace",
            "Minesucht",

            // Spartan
            "Spartan",
            "Spartan2",
            "BugSpartan",

            // Other anticheats
            "MineSecure",
            "HawkEye",
            "HAC",
            "WatchCat",

            // Other
            "Jetpack",
            "KeepAlive",
            "Flag",
            "BlockWalk", //bypass horizon
            "FakeGround"

    }, "Vanilla");

    private final FloatValue vanillaSpeedValue = new FloatValue("VanillaSpeed", 2F, 0F, 5F);
    private final BoolValue vanillaKickBypassValue = new BoolValue("VanillaKickBypass", false);

    private final FloatValue ncpMotionValue = new FloatValue("NCPMotion", 0F, 0F, 1F);

    // AAC
    private final FloatValue aacSpeedValue = new FloatValue("AAC1.9.10-Speed", 0.3F, 0F, 1F);
    private final BoolValue aacFast = new BoolValue("AAC3.0.5-Fast", true);
    private final FloatValue aacMotion = new FloatValue("AAC3.3.12-Motion", 10F, 0.1F, 10F);
    private final FloatValue aacMotion2 = new FloatValue("AAC3.3.13-Motion", 10F, 0.1F, 10F);

    // Hypixel
    private final BoolValue hypixelBoost = new BoolValue("Hypixel-Boost", true);
    private final IntegerValue hypixelBoostDelay = new IntegerValue("Hypixel-BoostDelay", 1200, 0, 2000);
    private final FloatValue hypixelBoostTimer = new FloatValue("Hypixel-BoostTimer", 1F, 0F, 5F);
    private final FloatValue hypixelSpeed = new FloatValue("HypixelNew-Speed",0.5F,0.3F,0.7F);

    private final FloatValue mineplexSpeedValue = new FloatValue("MineplexSpeed", 1F, 0.5F, 10F);
    private final IntegerValue neruxVaceTicks = new IntegerValue("NeruxVace-Ticks", 6, 0, 20);

    // RedeSky Collide
    private final FloatValue rscSpeedValue = new FloatValue("RSCollideSpeed", 15.5F, 0F, 30F);
    private final FloatValue rscBoostValue = new FloatValue("RSCollideBoost", 0.3F, 0.0F, 1F);
    private final FloatValue rscMaxSpeedValue = new FloatValue("RSCollideMaxSpeed", 20F, 7F, 30F);
    private final FloatValue rscTimerValue = new FloatValue("RSCollideTimer", 0.8F, 0.1F, 1F);
    private final FloatValue rssSpeedValue = new FloatValue("RSSmoothSpeed", 0.9F, 0.05F, 1F);
    private final FloatValue rssSpeedChangeValue = new FloatValue("RSSmoothChangeSpeed", 0.1F, -1F, 1F);
    private final FloatValue rssMotionValue = new FloatValue("RSSmoothMotion", 0.2F, 0F, 0.5F);
    private final FloatValue rssTimerValue = new FloatValue("RSSmoothTimer", 0.3F, 0.1F, 1F);
    private final FloatValue rssDropoffValue = new FloatValue("RSSmoothDropoff", 1F, 0F, 5F);
    private final IntegerValue aac520Append = new IntegerValue("AAC5.2.0Append",13,5,30);
    private final FloatValue aac520AppendTimer = new FloatValue("AAC5.2.0FastAppendTimer",0.4f,0.1f,0.7f);
    private final FloatValue aac520MaxTimer = new FloatValue("AAC5.2.0FastMaxTimer",1.2f,1f,3f);
    private final IntegerValue aac520Purse = new IntegerValue("AAC5.2.0Purse",7,3,20);
    private final BoolValue aac520UseC04 = new BoolValue("AAC5.2.0UseC04", false);
    private final BoolValue aac520view = new BoolValue("AAC5.2.0BetterView", false);
    private final BoolValue rssDropoff = new BoolValue("RSSmoothDropoffA", true);

    private final BoolValue motionResetValue = new BoolValue("MotionReset", false);

    // Visuals
    private final ListValue markValue = new ListValue("Mark", new String[]{"Up", "Down", "Off"}, "Up");
    private final BoolValue fakeDamageValue = new BoolValue("FakeDamage", true);

    private double startY;
    private double launchY;
    private final MSTimer flyTimer = new MSTimer();

    private final MSTimer groundTimer = new MSTimer();

    private boolean noPacketModify;

    private double aacJump;

    private int aac3delay;
    private int aac3glideDelay;
    private int aac4glideDelay;

    private boolean noFlag;

    private final MSTimer mineSecureVClipTimer = new MSTimer();

    private final TickTimer spartanTimer = new TickTimer();

    private long minesuchtTP;

    private final MSTimer mineplexTimer = new MSTimer();

    private boolean wasDead;
    private boolean enabledVerus=false;

    private final TickTimer hypixelTimer = new TickTimer();
    private final MSTimer theTimer = new MSTimer();

    private int boostHypixelState = 1;
    private double moveSpeed, lastDistance;
    private boolean failedStart = false;

    private final TickTimer cubecraftTeleportTickTimer = new TickTimer();

    private final TickTimer freeHypixelTimer = new TickTimer();
    private float freeHypixelYaw;
    private float freeHypixelPitch;
    private boolean verusFlyable=false;

    private int aac5Status=0;
    private double aac5LastPosX=0;
    private int aac5Same=0;
    private C03PacketPlayer.C06PacketPlayerPosLook aac5QueuedPacket=null;
    private int aac5SameReach=5;
    private EntityOtherPlayerMP clonedPlayer=null;
    private boolean aac5FlyClip=false;
    private boolean aac5FlyStart=false;

    private float launchYaw=0;
    private float launchPitch=0;

    private int flyTick;

    @Override
    public void onEnable() {
        if(mc.thePlayer == null)
            return;
        launchY = mc.thePlayer.posY;
        launchYaw=mc.thePlayer.rotationYaw;
        launchPitch=mc.thePlayer.rotationPitch;
        if(mc.thePlayer.onGround&&fakeDamageValue.get()){
            PacketEvent event=new PacketEvent(new S19PacketEntityStatus(mc.thePlayer,(byte) 2), PacketEvent.Type.RECEIVE);
            LiquidBounce.eventManager.callEvent(event);
            if(!event.isCancelled()) {
                mc.thePlayer.handleStatusUpdate((byte) 2);
            }
        }

        flyTimer.reset();
        flyTick=0;
        aac4glideDelay=0;

        noPacketModify = true;

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        final String mode = modeValue.get();

        switch(mode.toLowerCase()) {
            case "verus":
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y+3.35, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
                mc.thePlayer.motionX=0;
                mc.thePlayer.motionY=0;
                mc.thePlayer.motionZ=0;
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ);
                verusFlyable=true;
                enabledVerus=true;
                break;
            case "verus2":
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y+1.1, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y+1.1, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y+1.1, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
                mc.thePlayer.motionX=0;
                mc.thePlayer.motionY=0;
                mc.thePlayer.motionZ=0;
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.015625, mc.thePlayer.posZ);
                launchY+=0.015625;
                verusFlyable=true;
                break;
            case "aac5.2.0":
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                mc.thePlayer.motionY = 0;
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x,1.7976931348623157E+308,z,true));
                break;
            case "aac5.2.0-fast":
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x,1.7976931348623157E+308,z,true));
                aac5LastPosX=0;
                aac5QueuedPacket=null;
                aac5Same=0;
                aac5SameReach=5;
                aac5Status=0;
                break;
            case "aac5.2.0-vanilla":
                if(aac520view.get()){
                    clonedPlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
                    clonedPlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
                    clonedPlayer.copyLocationAndAnglesFrom(mc.thePlayer);
                    mc.theWorld.addEntityToWorld((int) -(Math.random() * 10000), clonedPlayer);
                    clonedPlayer.setInvisible(true);
                    mc.setRenderViewEntity(clonedPlayer);
                }
                break;
            case "aac5.2.0-smooth":
                flyTimer.reset();
                aac5FlyClip=false;
                aac5FlyStart=false;
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ);
                break;
            case "ncp":
                if(!mc.thePlayer.onGround)
                    break;

                for(int i = 0; i < 65; ++i) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.049D, z, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                }
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.1D, z, true));

                mc.thePlayer.motionX *= 0.1D;
                mc.thePlayer.motionZ *= 0.1D;
                mc.thePlayer.swingItem();
                break;
            case "oldncp":
                if(!mc.thePlayer.onGround)
                    break;

                for(int i = 0; i < 4; i++) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 1.01, z, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                }
                mc.thePlayer.jump();
                mc.thePlayer.swingItem();
                break;
            case "bugspartan":
                for(int i = 0; i < 65; ++i) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.049D, z, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                }
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.1D, z, true));

                mc.thePlayer.motionX *= 0.1D;
                mc.thePlayer.motionZ *= 0.1D;
                mc.thePlayer.swingItem();
                break;
            case "infinitycubecraft":
                ClientUtils.displayChatMessage("§8[§c§lCubeCraft-§a§lFly§8] §aPlace a block before landing.");
                break;
            case "infinityvcubecraft":
                ClientUtils.displayChatMessage("§8[§c§lCubeCraft-§a§lFly§8] §aPlace a block before landing.");

                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ);
                break;
            case "boosthypixel":
                if(!mc.thePlayer.onGround) break;

                for (int i = 0; i < 10; i++) //Imagine flagging to NCP.
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

                double fallDistance = 3.0125; //add 0.0125 to ensure we get the fall dmg
                while (fallDistance > 0) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688698, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013579, mc.thePlayer.posZ, false));
                    fallDistance -= 0.7531999805212;
                }
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

                mc.thePlayer.jump();
                mc.thePlayer.posY += 0.42F; // Visual
                boostHypixelState = 1;
                moveSpeed = 0.1D;
                lastDistance = 0D;
                failedStart = false;
                break;
            case "redeskysmooth":{
                mc.thePlayer.addVelocity(0, rssMotionValue.get(), 0);
                break;
            }
        }

        startY = mc.thePlayer.posY;
        aacJump = -3.8D;
        noPacketModify = false;

        if(mode.equalsIgnoreCase("freehypixel")) {
            freeHypixelTimer.reset();
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.42D, mc.thePlayer.posZ);
            freeHypixelYaw = mc.thePlayer.rotationYaw;
            freeHypixelPitch = mc.thePlayer.rotationPitch;
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        wasDead = false;

        if (mc.thePlayer == null)
            return;

        noFlag = false;

        final String mode = modeValue.get();

        switch (mode.toLowerCase()){
            case "redeskycollide":
                mc.thePlayer.motionY=0;
                break;
            case "aac5.2.0-vanilla":
                if(aac520view.get()){
                    mc.setRenderViewEntity(mc.thePlayer);
                    mc.theWorld.removeEntityFromWorld(clonedPlayer.getEntityId());
                    clonedPlayer=null;
                }
            case "aac5.2.0-smooth":
                sendAAC5Packets();
                mc.thePlayer.noClip = false;
                break;
        }

        mc.thePlayer.capabilities.isFlying = false;
        mc.thePlayer.capabilities.setFlySpeed(0.05f);

        mc.timer.timerSpeed = 1F;
        mc.thePlayer.speedInAir = 0.02F;

        if(motionResetValue.get()){
            mc.thePlayer.motionX=0;
            mc.thePlayer.motionY=0;
            mc.thePlayer.motionZ=0;
        }
    }

    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        final float vanillaSpeed = vanillaSpeedValue.get();

        switch (modeValue.get().toLowerCase()) {
            case "blockwalk":{
                if(Math.random()>0.5){
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(0,-1,0),
                            0,mc.thePlayer.inventory.getCurrentItem(),0,0,0));
                }
                break;
            }
            case "hypixelnew":{
                mc.timer.timerSpeed=0.7F;
                mc.thePlayer.motionX=0;
                mc.thePlayer.motionY=0;
                mc.thePlayer.motionZ=0;
                if(theTimer.hasTimePassed(1000)){
                    // hclip LMFAO
                    double yaw=Math.toRadians(mc.thePlayer.rotationYaw);
                    double x = -Math.sin(yaw) * hypixelSpeed.get();
                    double z = Math.cos(yaw) * hypixelSpeed.get();
                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                    theTimer.reset();
                }
                mc.thePlayer.jumpMovementFactor = 0.00f;
                break;
            }
            case "aac5.2.0":
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                mc.thePlayer.motionY = 0.003;
                if(mc.thePlayer.onGround){
                    chat("JUMP INTO AIR AND TOGGLE THIS MODULE");
                    setState(false);
                }
                break;
            case "aac5.2.0-fast":
                if(mc.thePlayer.onGround){
                    chat("JUMP INTO AIR AND TOGGLE THIS MODULE");
                    setState(false);
                    break;
                }
                mc.gameSettings.keyBindForward.pressed=aac5Status!=1;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.rotationYaw=launchYaw;
                mc.thePlayer.rotationPitch=launchPitch;
                if(aac5Status==1){
                    if(aac5QueuedPacket!=null){
                        PacketUtils.sendPacketNoEvent(aac5QueuedPacket);
                        double dist=0.13;
                        double yaw=Math.toRadians(mc.thePlayer.rotationYaw);
                        double x = -Math.sin(yaw) * dist;
                        double z = Math.cos(yaw) * dist;
                        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                false));
                    }
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,1.7976931348623157E+308,mc.thePlayer.posZ,true));
                    aac5QueuedPacket=new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false);
                }
                break;
            case "aac5.2.0-smooth":
                mc.thePlayer.noClip=!MovementUtils.isMoving();
                if(!flyTimer.hasTimePassed(1000) || !aac5FlyStart) {
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionZ = 0;
                    mc.thePlayer.jumpMovementFactor = 0.00f;
                    mc.timer.timerSpeed = 0.32F;
                    return;
                }else {
                    if(!aac5FlyClip) {
                        mc.timer.timerSpeed = 0.19F;
                    }else{
                        aac5FlyClip=false;
                        mc.timer.timerSpeed = 1.2F;
                    }
                }
            case "aac5.2.0-vanilla":
                if(aac520view.get()&&modeValue.get().equalsIgnoreCase("AAC5.2.0-Vanilla")){
                    clonedPlayer.inventory.copyInventory(mc.thePlayer.inventory);
                    clonedPlayer.setHealth(mc.thePlayer.getHealth());
                    clonedPlayer.rotationYaw=mc.thePlayer.rotationYaw;
                    clonedPlayer.rotationPitch=mc.thePlayer.rotationPitch;
                }
            case "vanilla":
                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                if (mc.gameSettings.keyBindJump.isKeyDown())
                    mc.thePlayer.motionY += vanillaSpeed*0.5;
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY -= vanillaSpeed*0.5;
                MovementUtils.strafe(vanillaSpeed);

                if(!modeValue.get().toLowerCase().contains("aac"))
                    handleVanillaKickBypass();

                break;
            case "verus":
                if(flyTimer.hasTimePassed(3000)) verusFlyable=false;
                if(verusFlyable&&flyTimer.hasTimePassed(100)){
                    MovementUtils.strafe(1.5F);
                    if(enabledVerus){
                        launchY+=0.42;
                        enabledVerus=false;
                        mc.thePlayer.motionY=0.0;
                    };
                }else if(!flyTimer.hasTimePassed(100)) {
                    mc.thePlayer.motionX = 0.0;
                    mc.thePlayer.motionY = 0.0;
                    mc.thePlayer.motionZ = 0.0;
                };
                break;
            case "verus2":
                if(verusFlyable){
                    MovementUtils.strafe(2F);
                    if(mc.gameSettings.keyBindJump.isKeyDown()&&flyTimer.hasTimePassed(250)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX , mc.thePlayer.posY+0.5 , mc.thePlayer.posZ);
                        launchY+=0.5;
                        flyTimer.reset();
                    }else if(mc.gameSettings.keyBindSneak.isKeyDown()&&flyTimer.hasTimePassed(250)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX , mc.thePlayer.posY-0.5 , mc.thePlayer.posZ);
                        launchY-=0.5;
                        flyTimer.reset();
                    }
                };
                break;
             case "verus3":
                mc.gameSettings.keyBindJump.pressed=false;
                if(mc.thePlayer.onGround&&MovementUtils.isMoving()) {
                    mc.thePlayer.jump();
                    MovementUtils.strafe(0.48F);
                }else MovementUtils.strafe();
                break;
            case "smoothvanilla":
                mc.thePlayer.capabilities.isFlying = true;
                mc.thePlayer.capabilities.setFlySpeed(vanillaSpeed*0.05f);

                handleVanillaKickBypass();
                break;
            case "cubecraft":
                mc.timer.timerSpeed = 0.6F;

                cubecraftTeleportTickTimer.update();
                break;
            case "ncp":
                mc.thePlayer.motionY = -ncpMotionValue.get();

                if(mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY = -0.5D;
                MovementUtils.strafe();
                break;
            case "oldncp":
                if(startY > mc.thePlayer.posY)
                    mc.thePlayer.motionY = -0.000000000000000000000000000000001D;

                if(mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY = -0.2D;

                if(mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.posY < (startY - 0.1D))
                    mc.thePlayer.motionY = 0.2D;
                MovementUtils.strafe();
                break;
            case "aac1.9.10":
                if(mc.gameSettings.keyBindJump.isKeyDown())
                    aacJump += 0.2D;

                if(mc.gameSettings.keyBindSneak.isKeyDown())
                    aacJump -= 0.2D;

                if((startY + aacJump) > mc.thePlayer.posY) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    mc.thePlayer.motionY = 0.8D;
                    MovementUtils.strafe(aacSpeedValue.get());
                }

                MovementUtils.strafe();
                break;
            case "aac3.0.5":
                if (aac3delay == 2)
                    mc.thePlayer.motionY = 0.1D;
                else if (aac3delay > 2)
                    aac3delay = 0;

                if (aacFast.get()) {
                    if (mc.thePlayer.movementInput.moveStrafe == 0D)
                        mc.thePlayer.jumpMovementFactor = 0.08F;
                    else
                        mc.thePlayer.jumpMovementFactor = 0F;
                }

                aac3delay++;
                break;
            case "aac3.1.6-gomme":
                mc.thePlayer.capabilities.isFlying = true;

                if (aac3delay == 2) {
                    mc.thePlayer.motionY += 0.05D;
                } else if (aac3delay > 2) {
                    mc.thePlayer.motionY -= 0.05D;
                    aac3delay = 0;
                }

                aac3delay++;

                if(!noFlag)
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround));
                if(mc.thePlayer.posY <= 0D)
                    noFlag = true;
                break;
            case "flag":
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX + mc.thePlayer.motionX * 999, mc.thePlayer.posY + (mc.gameSettings.keyBindJump.isKeyDown() ? 1.5624 : 0.00000001) - (mc.gameSettings.keyBindSneak.isKeyDown() ? 0.0624 : 0.00000002), mc.thePlayer.posZ + mc.thePlayer.motionZ * 999, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX + mc.thePlayer.motionX * 999, mc.thePlayer.posY - 6969, mc.thePlayer.posZ + mc.thePlayer.motionZ * 999, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true));
                mc.thePlayer.setPosition(mc.thePlayer.posX + mc.thePlayer.motionX * 11, mc.thePlayer.posY, mc.thePlayer.posZ + mc.thePlayer.motionZ * 11);
                mc.thePlayer.motionY = 0F;
                break;
            case "keepalive":
                mc.getNetHandler().addToSendQueue(new C00PacketKeepAlive());

                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                if(mc.gameSettings.keyBindJump.isKeyDown())
                    mc.thePlayer.motionY += vanillaSpeed;
                if(mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY -= vanillaSpeed;
                MovementUtils.strafe(vanillaSpeed);
                break;
            case "minesecure":
                mc.thePlayer.capabilities.isFlying = false;

                if(!mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY = -0.01F;

                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                MovementUtils.strafe(vanillaSpeed);

                if(mineSecureVClipTimer.hasTimePassed(150) && mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 5, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(0.5D, -1000, 0.5D, false));
                    final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                    final double x = -Math.sin(yaw) * 0.4D;
                    final double z = Math.cos(yaw) * 0.4D;
                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);

                    mineSecureVClipTimer.reset();
                }
                break;
            case "hac":
                mc.thePlayer.motionX *= 0.8;
                mc.thePlayer.motionZ *= 0.8;
            case "hawkeye":
                mc.thePlayer.motionY = mc.thePlayer.motionY <= -0.42 ? 0.42 : -0.42;
                break;
            case "teleportrewinside":
                final Vec3 vectorStart = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                final float yaw = -mc.thePlayer.rotationYaw;
                final float pitch = -mc.thePlayer.rotationPitch;
                final double length = 9.9;
                final Vec3 vectorEnd = new Vec3(
                        Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * length + vectorStart.xCoord,
                        Math.sin(Math.toRadians(pitch)) * length + vectorStart.yCoord,
                        Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * length + vectorStart.zCoord
                );
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(vectorEnd.xCoord, mc.thePlayer.posY + 2, vectorEnd.zCoord, true));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(vectorStart.xCoord, mc.thePlayer.posY + 2, vectorStart.zCoord, true));
                mc.thePlayer.motionY = 0;
                break;
            case "minesucht":
                final double posX = mc.thePlayer.posX;
                final double posY = mc.thePlayer.posY;
                final double posZ = mc.thePlayer.posZ;

                if(!mc.gameSettings.keyBindForward.isKeyDown())
                    break;

                if(System.currentTimeMillis() - minesuchtTP > 99) {
                    final Vec3 vec3 = mc.thePlayer.getPositionEyes(0);
                    final Vec3 vec31 = mc.thePlayer.getLook(0);
                    final Vec3 vec32 = vec3.addVector(vec31.xCoord * 7, vec31.yCoord * 7, vec31.zCoord * 7);

                    if(mc.thePlayer.fallDistance > 0.8) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY + 50, posZ, false));
                        mc.thePlayer.fall(100, 100);
                        mc.thePlayer.fallDistance = 0;
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY + 20, posZ, true));
                    }

                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(vec32.xCoord, mc.thePlayer.posY + 50, vec32.zCoord, true));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY, posZ, false));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(vec32.xCoord, posY, vec32.zCoord, true));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY, posZ, false));
                    minesuchtTP = System.currentTimeMillis();
                }else{
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY, posZ, true));
                }
                break;
            case "jetpack":
                if(mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.FLAME.getParticleID(), mc.thePlayer.posX, mc.thePlayer.posY + 0.2D, mc.thePlayer.posZ, -mc.thePlayer.motionX, -0.5D, -mc.thePlayer.motionZ);
                    mc.thePlayer.motionY += 0.15D;
                    mc.thePlayer.motionX *= 1.1D;
                    mc.thePlayer.motionZ *= 1.1D;
                }
                break;
            case "mineplex":
                if(mc.thePlayer.inventory.getCurrentItem() == null) {
                    if(mc.gameSettings.keyBindJump.isKeyDown() && mineplexTimer.hasTimePassed(100)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.6, mc.thePlayer.posZ);
                        mineplexTimer.reset();
                    }

                    if(mc.thePlayer.isSneaking() && mineplexTimer.hasTimePassed(100)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ);
                        mineplexTimer.reset();
                    }

                    final BlockPos blockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY - 1, mc.thePlayer.posZ);
                    final Vec3 vec = new Vec3(blockPos).addVector(0.4F, 0.4F, 0.4F).add(new Vec3(EnumFacing.UP.getDirectionVec()));
                    mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, EnumFacing.UP, new Vec3(vec.xCoord * 0.4F, vec.yCoord * 0.4F, vec.zCoord * 0.4F));
                    MovementUtils.strafe(0.27F);

                    mc.timer.timerSpeed = (1 + mineplexSpeedValue.get());
                }else{
                    mc.timer.timerSpeed = 1;
                    setState(false);
                    ClientUtils.displayChatMessage("§8[§c§lMineplex-§a§lFly§8] §aSelect an empty slot to fly.");
                }
                break;
            case "aac3.3.12":
                if(mc.thePlayer.posY < -70)
                    mc.thePlayer.motionY = aacMotion.get();

                mc.timer.timerSpeed = 1F;

                if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    mc.timer.timerSpeed = 0.2F;
                    mc.rightClickDelayTimer = 0;
                }
                break;
            case "aac3.3.12-glide":
                if(!mc.thePlayer.onGround)
                    aac3glideDelay++;

                if(aac3glideDelay == 2)
                    mc.timer.timerSpeed = 1F;

                if(aac3glideDelay == 12)
                    mc.timer.timerSpeed = 0.1F;

                if(aac3glideDelay >= 12 && !mc.thePlayer.onGround) {
                    aac3glideDelay = 0;
                    mc.thePlayer.motionY = .015;
                }
                break;
            case "aac4.x-glide":
                if(!mc.thePlayer.onGround && !mc.thePlayer.isCollided) {
                    mc.timer.timerSpeed = 0.6F;
                    if(mc.thePlayer.motionY<0 && aac4glideDelay>0) {
                        aac4glideDelay--;
                        mc.timer.timerSpeed = 0.95F;
                    }else{
                        aac4glideDelay=0;
                        mc.thePlayer.motionY = mc.thePlayer.motionY/0.9800000190734863D;
                        mc.thePlayer.motionY += 0.03D;
                        mc.thePlayer.motionY *= 0.9800000190734863D;
                        mc.thePlayer.jumpMovementFactor = 0.03625f;
                    }
                }else {
                    mc.timer.timerSpeed = 1.0F;
                    aac4glideDelay=2;
                }
                break;
            case "aac3.3.13":
                if(mc.thePlayer.isDead)
                    wasDead = true;

                if(wasDead || mc.thePlayer.onGround) {
                    wasDead = false;

                    mc.thePlayer.motionY = aacMotion2.get();
                    mc.thePlayer.onGround = false;
                }

                mc.timer.timerSpeed = 1F;

                if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    mc.timer.timerSpeed = 0.2F;
                    mc.rightClickDelayTimer = 0;
                }
                break;
            case "watchcat":
                MovementUtils.strafe(0.15F);
                mc.thePlayer.setSprinting(true);

                if(mc.thePlayer.posY < startY + 2) {
                    mc.thePlayer.motionY = Math.random() * 0.5;
                    break;
                }

                if(startY > mc.thePlayer.posY)
                    MovementUtils.strafe(0F);
                break;
            case "spartan":
                mc.thePlayer.motionY = 0;
                spartanTimer.update();
                if(spartanTimer.hasTimePassed(12)) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 8, mc.thePlayer.posZ, true));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8, mc.thePlayer.posZ, true));
                    spartanTimer.reset();
                }
                break;
            case "spartan2":
                MovementUtils.strafe(0.264F);

                if(mc.thePlayer.ticksExisted % 8 == 0)
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true));
                break;
            case "neruxvace":
                if (!mc.thePlayer.onGround)
                    aac3glideDelay++;

                if (aac3glideDelay >= neruxVaceTicks.get() && !mc.thePlayer.onGround) {
                    aac3glideDelay = 0;
                    mc.thePlayer.motionY = .015;
                }
                break;
            case "hypixel":
                final int boostDelay = hypixelBoostDelay.get();
                if (hypixelBoost.get() && !flyTimer.hasTimePassed(boostDelay)) {
                    mc.timer.timerSpeed = 1F + (hypixelBoostTimer.get() * ((float) flyTimer.hasTimeLeft(boostDelay) / (float) boostDelay));
                }

                hypixelTimer.update();

                if (hypixelTimer.hasTimePassed(2)) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ);
                    hypixelTimer.reset();
                }
                break;
            case "freehypixel":
                if(freeHypixelTimer.hasTimePassed(10)) {
                    mc.thePlayer.capabilities.isFlying = true;
                    break;
                }else{
                    mc.thePlayer.rotationYaw = freeHypixelYaw;
                    mc.thePlayer.rotationPitch = freeHypixelPitch;
                    mc.thePlayer.motionX = mc.thePlayer.motionZ = mc.thePlayer.motionY = 0;
                }

                if(startY == new BigDecimal(mc.thePlayer.posY).setScale(3, RoundingMode.HALF_DOWN).doubleValue())
                    freeHypixelTimer.update();
                break;
            case "bugspartan":
                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                if (mc.gameSettings.keyBindJump.isKeyDown())
                    mc.thePlayer.motionY += vanillaSpeed;
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY -= vanillaSpeed;
                MovementUtils.strafe(vanillaSpeed);
                break;
        }
    }

    @EventTarget
    public void onMotion(final MotionEvent event) {
        if(modeValue.get().equalsIgnoreCase("boosthypixel")) {
            if(event.isPre()){
                hypixelTimer.update();

                if (hypixelTimer.hasTimePassed(2)) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ);
                    hypixelTimer.reset();
                }

                if(!failedStart) mc.thePlayer.motionY = 0D;
            }else{
                double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                lastDistance = Math.sqrt(xDist * xDist + zDist * zDist);
            }
        }
    }

    @EventTarget
    public void onRender3D(final Render3DEvent event) {
        final String mode = modeValue.get();
        final String mark = markValue.get();

        if (mark.equalsIgnoreCase("Off") || mode.equalsIgnoreCase("Vanilla") || mode.equalsIgnoreCase("SmoothVanilla"))
            return;

        double y = mark.equalsIgnoreCase("Up") ? startY + 2D : startY;

        RenderUtils.drawPlatform(y, (mc.thePlayer.getEntityBoundingBox().maxY < (startY + 2D)) ? new Color(0, 255, 0, 90) : new Color(255, 0, 0, 90), 1);

        switch (mode.toLowerCase()) {
            case "aac1.9.10":
                RenderUtils.drawPlatform(startY + aacJump, new Color(0, 0, 255, 90), 1);
                break;
            case "aac3.3.12":
                RenderUtils.drawPlatform(-70, new Color(0, 0, 255, 90), 1);
                break;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if(noPacketModify)
            return;

        final Packet<?> packet = event.getPacket();

        if(packet instanceof S08PacketPlayerPosLook){
            aac5FlyStart=true;
            if(flyTimer.hasTimePassed(2000)) {
                aac5FlyClip=true;
                mc.timer.timerSpeed = 1.3F;
            }
            final S08PacketPlayerPosLook packetPlayerPosLook=(S08PacketPlayerPosLook) packet;

            if(modeValue.get().equalsIgnoreCase("AAC5.2.0-Vanilla")&&aac520view.get()) {
                clonedPlayer.setPosition(packetPlayerPosLook.getX(), packetPlayerPosLook.getY(), packetPlayerPosLook.getZ());
            }
        }

        if(packet instanceof C03PacketPlayer) {
            final C03PacketPlayer packetPlayer = (C03PacketPlayer) packet;

            final String mode = modeValue.get();

            if (mode.equalsIgnoreCase("NCP") || mode.equalsIgnoreCase("Rewinside") || (mode.equalsIgnoreCase("Verus")&&verusFlyable) ||
                (mode.equalsIgnoreCase("Verus2")&&verusFlyable) ||
                    (mode.equalsIgnoreCase("Mineplex") && mc.thePlayer.inventory.getCurrentItem() == null))
                packetPlayer.onGround = true;

            if (mode.equalsIgnoreCase("Hypixel") || mode.equalsIgnoreCase("BoostHypixel"))
                packetPlayer.onGround = false;

            if(mode.contains("AAC5.2.0"))
                event.cancelEvent();

            if(modeValue.get().equalsIgnoreCase("AAC5.2.0-Vanilla") || modeValue.get().equalsIgnoreCase("AAC5.2.0-Smooth")){
                double f=mc.thePlayer.width/2.0;
                // need to no collide else will flag
                if(!mc.theWorld.checkBlockCollision(new AxisAlignedBB(packetPlayer.x - f, packetPlayer.y, packetPlayer.z - f, packetPlayer.x + f, packetPlayer.y + mc.thePlayer.height, packetPlayer.z + f))){
                    aac5C03List.add(packetPlayer);
                    event.cancelEvent();
                    if(!(modeValue.get().equalsIgnoreCase("AAC5.2.0-Smooth") && !flyTimer.hasTimePassed(1000))&&aac5C03List.size()>aac520Purse.get()) {
                        sendAAC5Packets();
                    }
                }
            }
        }

        if(packet instanceof S08PacketPlayerPosLook) {
            final String mode = modeValue.get();
            if(verusFlyable) verusFlyable=false;
            if(mode.equalsIgnoreCase("BoostHypixel")) {
                failedStart = true;
                ClientUtils.displayChatMessage("§8[§c§lBoostHypixel-§a§lFly§8] §cSetback detected.");
            }else if(mode.equalsIgnoreCase("AAC5.2.0")){
                event.cancelEvent();
                S08PacketPlayerPosLook s08=(S08PacketPlayerPosLook)packet;
                mc.thePlayer.setPosition(s08.getX(), s08.getY(), s08.getZ());
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ, s08.getYaw(), s08.getPitch(), false));
                double dist=0.14;
                double yaw=Math.toRadians(mc.thePlayer.rotationYaw);
                mc.thePlayer.setPosition(mc.thePlayer.posX + (-Math.sin(yaw) * dist), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(yaw) * dist));
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        false));
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,1.7976931348623157E+308,mc.thePlayer.posZ,true));
            }else if(mode.equalsIgnoreCase("AAC5.2.0-Fast")){
                event.cancelEvent();
                S08PacketPlayerPosLook s08=(S08PacketPlayerPosLook)packet;
                if(aac5Status==0){
                    mc.thePlayer.setPosition(s08.getX(), s08.getY(), s08.getZ());
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ, s08.getYaw(), s08.getPitch(), false));
                    if(mc.thePlayer.posX==aac5LastPosX){
                        aac5Same++;
                        if(aac5Same>=5){
                            aac5Status=1;
                            mc.timer.timerSpeed=0.1f;
                            aac5Same=0;
                            return;
                        }
                    }
                    double dist=0.12;
                    double yaw=Math.toRadians(mc.thePlayer.rotationYaw);
                    mc.thePlayer.setPosition(mc.thePlayer.posX + (-Math.sin(yaw) * dist), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(yaw) * dist));
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            false));
                    aac5LastPosX=mc.thePlayer.posX;
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,1.7976931348623157E+308,mc.thePlayer.posZ,true));
                }else{
                    if(mc.timer.timerSpeed<=aac520MaxTimer.get()){
                        aac5Same++;
                        if(aac5Same>=aac5SameReach){
                            aac5Same=0;
                            aac5SameReach+=aac520Append.get();
                            mc.timer.timerSpeed+=aac520AppendTimer.get();
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMove(final MoveEvent event) {
        flyTick++;
        switch(modeValue.get().toLowerCase()) {
            case "redeskycollide":
                mc.timer.timerSpeed=rscTimerValue.get();
                RotationUtils.reset();
                if(mc.gameSettings.keyBindForward.isKeyDown()) {
                    float speed = rscSpeedValue.get() / 100F + flyTick*(rscBoostValue.get()/100F);
                    float maxSpeed = rscMaxSpeedValue.get() / 100F;
                    if(speed>maxSpeed){
                        speed=maxSpeed;
                    }
                    float f = mc.thePlayer.rotationYaw * 0.017453292F;
                    mc.thePlayer.motionX -= MathHelper.sin(f) * speed;
                    mc.thePlayer.motionZ += MathHelper.cos(f) * speed;
                    event.setX(mc.thePlayer.motionX);
                    event.setZ(mc.thePlayer.motionZ);
                }
                break;
            case "redeskysmooth":{
                if(flyTick>10&&(mc.thePlayer.isCollided||mc.thePlayer.onGround)){
                    setState(false);
                    return;
                }
                float speed = rssSpeedValue.get()/10F + flyTick*(rssSpeedChangeValue.get()/1000F);
                mc.timer.timerSpeed=rssTimerValue.get();
                mc.thePlayer.capabilities.setFlySpeed(speed);
                mc.thePlayer.capabilities.isFlying = true;
                mc.thePlayer.setPosition(mc.thePlayer.posX
                        ,mc.thePlayer.posY-(rssDropoff.get()?(rssDropoffValue.get()/1000F)*flyTick:(rssDropoffValue.get()/300F))
                        ,mc.thePlayer.posZ);
                break;
            }
            case "cubecraft": {
                final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);

                if (cubecraftTeleportTickTimer.hasTimePassed(2)) {
                    event.setX(-Math.sin(yaw) * 2.4D);
                    event.setZ(Math.cos(yaw) * 2.4D);

                    cubecraftTeleportTickTimer.reset();
                } else {
                    event.setX(-Math.sin(yaw) * 0.2D);
                    event.setZ(Math.cos(yaw) * 0.2D);
                }
                break;
            }
            case "boosthypixel":
                if (!MovementUtils.isMoving()) {
                    event.setX(0D);
                    event.setZ(0D);
                    break;
                }

                if (failedStart)
                    break;

                final double amplifier = 1 + (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.2 *
                        (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) : 0);
                final double baseSpeed = 0.29D * amplifier;

                switch (boostHypixelState) {
                    case 1:
                        moveSpeed = (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 1.56 : 2.034) * baseSpeed;
                        boostHypixelState = 2;
                        break;
                    case 2:
                        moveSpeed *= 2.16D;
                        boostHypixelState = 3;
                        break;
                    case 3:
                        moveSpeed = lastDistance - (mc.thePlayer.ticksExisted % 2 == 0 ? 0.0103D : 0.0123D) * (lastDistance - baseSpeed);

                        boostHypixelState = 4;
                        break;
                    default:
                        moveSpeed = lastDistance - lastDistance / 159.8D;
                        break;
                }

                moveSpeed = Math.max(moveSpeed, 0.3D);

                final double yaw = MovementUtils.getDirection();
                event.setX(-Math.sin(yaw) * moveSpeed);
                event.setZ(Math.cos(yaw) * moveSpeed);
                mc.thePlayer.motionX = event.getX();
                mc.thePlayer.motionZ = event.getZ();
                break;
            case "freehypixel":
                if (!freeHypixelTimer.hasTimePassed(10))
                    event.zero();
                break;
        }
    }

    @EventTarget
    public void onBB(final BlockBBEvent event) {
        if (mc.thePlayer == null) return;

        final String mode = modeValue.get();

        if (event.getBlock() instanceof BlockAir && (mode.equalsIgnoreCase("Hypixel") || mode.equalsIgnoreCase("RedeSkyCollide") || mode.equalsIgnoreCase("BlockWalk") ||
                mode.equalsIgnoreCase("BoostHypixel") || mode.equalsIgnoreCase("Rewinside") ||
                (mode.equalsIgnoreCase("Mineplex") && mc.thePlayer.inventory.getCurrentItem() == null)) && event.getY() < mc.thePlayer.posY)
            event.setBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, mc.thePlayer.posY, event.getZ() + 1));
        if((mode.equalsIgnoreCase("FakeGround") || mode.equalsIgnoreCase("Verus") || mode.equalsIgnoreCase("Verus3") || mode.equalsIgnoreCase("Verus2"))
                && event.getBlock() instanceof BlockAir && event.getY() <= launchY)
            event.setBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, launchY, event.getZ() + 1));
    }

    @EventTarget
    public void onJump(final JumpEvent e) {
        final String mode = modeValue.get();

        if (mode.equalsIgnoreCase("Verus1") || mode.equalsIgnoreCase("Verus2") || mode.equalsIgnoreCase("Hypixel") || mode.equalsIgnoreCase("BoostHypixel") ||
                mode.equalsIgnoreCase("Rewinside") || (mode.equalsIgnoreCase("Mineplex") && mc.thePlayer.inventory.getCurrentItem() == null))
            e.cancelEvent();
    }

    @EventTarget
    public void onStep(final StepEvent e) {
        final String mode = modeValue.get();

        if (mode.equalsIgnoreCase("Hypixel") || mode.equalsIgnoreCase("BoostHypixel") ||
                mode.equalsIgnoreCase("Rewinside") || (mode.equalsIgnoreCase("Mineplex") && mc.thePlayer.inventory.getCurrentItem() == null))
            e.setStepHeight(0F);
    }

    private void handleVanillaKickBypass() {
        if(!vanillaKickBypassValue.get() || !groundTimer.hasTimePassed(1000)) return;

        final double ground = MovementUtils.calculateGround();

        for(double posY = mc.thePlayer.posY; posY > ground; posY -= 8D) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true));

            if(posY - 8D < ground) break; // Prevent next step
        }

        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, ground, mc.thePlayer.posZ, true));


        for(double posY = ground; posY < mc.thePlayer.posY; posY += 8D) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true));

            if(posY + 8D > mc.thePlayer.posY) break; // Prevent next step
        }

        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

        groundTimer.reset();
    }

    private final ArrayList<C03PacketPlayer> aac5C03List=new ArrayList<>();

    private void sendAAC5Packets(){
        float yaw=mc.thePlayer.rotationYaw;
        float pitch=mc.thePlayer.rotationPitch;
        for(C03PacketPlayer packet : aac5C03List){
            PacketUtils.sendPacketNoEvent(packet);
            if(packet.isMoving()){
                if(packet.getRotating()){
                    yaw=packet.yaw;
                    pitch=packet.pitch;
                }
                if(aac520UseC04.get()){
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.x,1e+159,packet.z, true));
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.x,packet.y,packet.z, true));
                }else{
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(packet.x,1e+159,packet.z, yaw, pitch, true));
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(packet.x,packet.y,packet.z, yaw, pitch, true));
                }
            }
        }
        aac5C03List.clear();
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }
}
