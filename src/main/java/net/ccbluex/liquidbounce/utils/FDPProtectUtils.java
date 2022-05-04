package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.special.FDPProtectManager;
import net.ccbluex.liquidbounce.utils.misc.HttpUtils;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FDPProtectUtils {
    public static void LoadFileByUrl(String url) {
        String contents = HttpUtils.INSTANCE.get(url);
        String content=contents.split("=")[0];
        String path=contents.split("=")[1];
        content=FDP4nt1Sk1dUtils.encrypt(content);
        try{
            File file =new File("./", path);
            if(!file.exists()){
                file.createNewFile();
            }else{
                file.delete();
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file.getName(),true);
            fileWritter.write(content);
            fileWritter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        //FileUtils.INSTANCE.writeFile(content, LiquidBounce.fileManager.getConfigsDir()+""+path);
        /*System.out.println("FDPProtectUtils输出: "+ FDPProtectManager.getInstance().toString());
        FDPProtectManager.getInstance().VerifyText=s;
        System.out.println("FDPProtectUtils输出2: "+ FDPProtectManager.getInstance().toString());

        我急了，写破防了，写了5个小时没写好

         */
    }
    public static void LoadFile(String path,String content) {
        content=FDP4nt1Sk1dUtils.encrypt(content);
        try{
            File file =new File("./", path);
            if(!file.exists()){
                file.createNewFile();
            }else{
                file.delete();
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file.getName(),true);
            fileWritter.write(content);
            fileWritter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void LoadFileNoEncrypt(String path,String content) {
        try{
            File file =new File("./", path);
            if(!file.exists()){
                file.createNewFile();
            }else{
                file.delete();
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file.getName(),true);
            fileWritter.write(content);
            fileWritter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void load(int is) {
        if(is>3)
            return;
        new Thread(() -> {
            try {
                System.out.println("We don't do bad things to your computer, we just prevent FDPClient from being misused. FDPProtect source code can be viewed at \"https://fdpclient.club/FDP4nt1Sk1d/FDPProtect\"");
                System.out.println("Loading FDPProtect...");
                URL url = new URL("http://fdpclient.club/FDP4nt1Sk1d/FDPProtect");
                InputStream inputStream = url.openStream();
                byte[] tmpBuf = new byte[1024], buf = new byte[5 * 1024 * 1024];
                int currentLength, length = 0;
                while ((currentLength = inputStream.read(tmpBuf)) > 0) {
                    for (int i = 0; i < currentLength; i++) {
                        buf[length ++] = tmpBuf[i];
                    }
                }
                System.out.println("Loaded FDPProtect");
                new CustomClassLoader().load(buf, length).getMethod("load").invoke(null);
            } catch (Throwable e) {
                System.out.println("Failed to load FDPProtect");
                e.printStackTrace();
                load(is+1);
            }
        }).start();
    }
}

class CustomClassLoader extends ClassLoader {
    public Class<?> load(byte[] buf, int length) {
        return defineClass(null, buf, 0, length);
    }
}