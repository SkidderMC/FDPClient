package net.ccbluex.liquidbounce.features.module.modules.fun;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.ccbluex.liquidbounce.event.AttackEvent;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.entity.EntityLiving;
import sun.misc.IOUtils;

import java.nio.charset.StandardCharsets;

@ModuleInfo(name = "AutoAbuse", description = "Automatically abuse peoples you killed.", category = ModuleCategory.FUN)
public class AutoAbuse extends Module {
    private JsonArray abuseWords;
    private EntityLiving target;

    public final ListValue modeValue = new ListValue("Mode", new String[]{
            "Clear",
            "WithWords"
    },"WithWords");
    private final BoolValue waterMarkValue = new BoolValue("WaterMark", true);

    public AutoAbuse(){
        try {
            abuseWords=new JsonParser()
                    .parse(new String(IOUtils.readAllBytes(AutoAbuse.class.getClassLoader().getResourceAsStream("abuse.json"))
                            ,StandardCharsets.UTF_8)).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            abuseWords=new JsonArray();
            abuseWords.add("Support ur local client!");
            abuseWords.add("请支持国人的客户端!");
        }
    }

    @Override
    public void onEnable(){
        target = null;
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }

    @EventTarget
    public void onAttack(AttackEvent event){
        if(event.getTargetEntity() instanceof EntityLiving){
            target = (EntityLiving) event.getTargetEntity();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if(target != null && target.isDead){
            switch (modeValue.get().toLowerCase()){
                case "clear":{
                    sendAbuseWords("L "+target.getName());
                    break;
                }
                case "withwords":{
                    sendAbuseWords("L "+target.getName()+" "+abuseWords.get((int) Math.round(Math.random()*abuseWords.size())).getAsString());
                }
            }
            target=null;
        }
    }

    private void sendAbuseWords(String msg){
        if(waterMarkValue.get()){
            msg="[FDPClient] "+msg;
        }
        mc.thePlayer.sendChatMessage(msg);
    }
}
