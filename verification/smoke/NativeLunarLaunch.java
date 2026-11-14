/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Native Lunar verification by opZywl | https://lucas-lima.xyz
 */
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Test-only launcher: gives the real DLL the early JVM phase used by ZAVZ's watcher. */
public final class NativeLunarLaunch {
    public static void main(String[] args) throws Throwable {
        Class<?> genesis = Class.forName("com.moonsworth.lunar.genesis.Genesis", false, ClassLoader.getSystemClassLoader());
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@", 2)[0];
        Path plan = Paths.get(System.getProperty("user.home"), ".zywl", "injector", "plans", pid + ".ini");
        Files.createDirectories(plan.getParent());
        String selection = "\uFEFF[selection]\r\nversion=1\r\npid=" + pid + "\r\ncount=1\r\nmultiple=0\r\n"
            + "[payload0]\r\nid=fdplunar\r\njar=" + System.getProperty("fdp.native.jar") + "\r\n";
        Files.write(plan, selection.getBytes(StandardCharsets.UTF_16LE));
        System.load(System.getProperty("fdp.native.dll"));
        Path result = Paths.get(plan.toString() + ".result");
        long deadline = System.currentTimeMillis() + 30000;
        while (!Boolean.getBoolean("fdpclient.lunar.agentRegistered")) {
            if (Files.exists(result)) throw new IllegalStateException(new String(Files.readAllBytes(result), StandardCharsets.UTF_8));
            if (System.currentTimeMillis() > deadline) throw new IllegalStateException("Native DLL did not register the FDP agent");
            Thread.sleep(10);
        }
        System.out.println("[FDP-NATIVE] Current agent registered by DLL before Genesis entered Minecraft");
        try { genesis.getMethod("main", String[].class).invoke(null, (Object) args); }
        catch (InvocationTargetException failure) { throw failure.getCause(); }
    }
}
