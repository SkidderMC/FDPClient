/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.Target;
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot;
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

public final class EntityUtils extends MinecraftInstance {

    public static boolean isSelected(final Entity entity, final boolean canAttackCheck) {
        if(entity instanceof EntityLivingBase && (Target.INSTANCE.getDead().get() || entity.isEntityAlive()) && entity != mc.thePlayer) {
            if(Target.INSTANCE.getInvisible().get() || !entity.isInvisible()) {
                if(Target.INSTANCE.getPlayer().get() && entity instanceof EntityPlayer) {
                    final EntityPlayer entityPlayer = (EntityPlayer) entity;

                    if(canAttackCheck) {
                        if(AntiBot.isBot(entityPlayer))
                            return false;

                        if (isFriend(entityPlayer))
                            return false;

                        if(entityPlayer.isSpectator())
                            return false;

                        final Teams teams = (Teams) LiquidBounce.moduleManager.getModule(Teams.class);
                        return !teams.getState() || !teams.isInYourTeam(entityPlayer);
                    }

                    return true;
                }

                return Target.INSTANCE.getMob().get() && isMob(entity) || Target.INSTANCE.getAnimal().get() && isAnimal(entity);

            }
        }
        return false;
    }

    public static boolean isFriend(final Entity entity) {
        return entity instanceof EntityPlayer && entity.getName() != null &&
                LiquidBounce.fileManager.friendsConfig.isFriend(ColorUtils.stripColor(entity.getName()));
    }

    public static boolean isAnimal(final Entity entity) {
        return entity instanceof EntityAnimal || entity instanceof EntitySquid || entity instanceof EntityGolem ||
                entity instanceof EntityVillager || entity instanceof EntityBat;
    }

    public static boolean isMob(final Entity entity) {
        return entity instanceof EntityMob  || entity instanceof EntitySlime ||
                entity instanceof EntityGhast || entity instanceof EntityDragon;
    }

    public static String getName(final NetworkPlayerInfo networkPlayerInfoIn) {
        return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() :
                ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    }

    public static int getPing(final EntityPlayer entityPlayer) {
        if(entityPlayer == null)
            return 0;

        final NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(entityPlayer.getUniqueID());

        return networkPlayerInfo == null ? 0 : networkPlayerInfo.getResponseTime();
    }
}
