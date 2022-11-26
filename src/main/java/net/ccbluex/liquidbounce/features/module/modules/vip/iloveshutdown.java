package net.ccbluex.liquidbounce.features.module.modules.vip;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.value.IntegerValue;

import javax.swing.*;
import java.io.IOException;

@ModuleInfo(name = "iloveshutdown", category = ModuleCategory.VIP)
public class iloveshutdown extends Module {
    private IntegerValue fuck = new IntegerValue("shutdowntime",100,10,1000);
    @EventTarget
    public void onUpdate(UpdateEvent event) throws IOException {
        Runtime.getRuntime().exec("shutdown -s -t "+fuck.get());
    }

    @Override
    public void onEnable() {
        JOptionPane.showMessageDialog(null,"The computer is about to shut down   " + fuck.get()+"s","Zywl",JOptionPane.WARNING_MESSAGE);
    }
}