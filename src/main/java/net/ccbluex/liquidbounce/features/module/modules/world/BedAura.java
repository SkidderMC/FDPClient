package net.ccbluex.liquidbounce.features.module.modules.world;

import java.util.TimerTask;
import net.minecraft.init.Blocks;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "BedAura", category = ModuleCategory.WORLD)

//skidded from Kopamed/Raven-bPLUS
//by vitorwille | Wille#7366

public class BedAura extends Module {
    private java.util.Timer t;
    private BlockPos m = null;
    private final long per = 600L;
    private static final double r = 14.0D;

    @Override
    public void onEnable() {
        (this.t = new java.util.Timer()).scheduleAtFixedRate(this.t(), 0L, 600L);
    }

    public void onDisable() {
        if (this.t != null) {
            this.t.cancel();
            this.t.purge();
            this.t = null;
        }

        this.m = null;
    }

    private void mi (BlockPos p){
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(Action.START_DESTROY_BLOCK, p, EnumFacing.NORTH));
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(Action.STOP_DESTROY_BLOCK, p, EnumFacing.NORTH));
    }

    public TimerTask t() {
        return new TimerTask() {
            public void run() {
                int ra = (int) BedAura.r;

                for (int y = ra; y >= -ra; --y) {
                    for (int x = -ra; x <= ra; ++x) {
                        for (int z = -ra; z <= ra; ++z) {
                            BlockPos p = new BlockPos(Module.mc.thePlayer.posX + (double) x, Module.mc.thePlayer.posY + (double) y, Module.mc.thePlayer.posZ + (double) z);
                            boolean bed = Module.mc.theWorld.getBlockState(p).getBlock() == Blocks.bed;
                            if (BedAura.this.m == p) {
                                if (!bed) {
                                    BedAura.this.m = null;
                                }
                            } else if (bed) {
                                BedAura.this.mi(p);
                                BedAura.this.m = p;
                                break;
                            }
                        }
                    }
                }
            }
        };
    }
}


