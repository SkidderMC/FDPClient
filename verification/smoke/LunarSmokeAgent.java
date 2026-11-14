/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Test-only agent: observes the real startup, then exercises the client in its own sandbox. */
public final class LunarSmokeAgent {
    private static final Map<Object, AtomicInteger> OBSERVED_PACKETS = new ConcurrentHashMap<>();
    public static void premain(String ignored, Instrumentation instrumentation) {
        Thread worker = new Thread(() -> run(instrumentation), "FDP Lunar smoke");
        worker.setDaemon(true);
        worker.start();
    }

    private static void run(Instrumentation instrumentation) {
        Path report = Paths.get(System.getProperty("fdp.smoke.report"));
        try {
            Class<?> minecraft = null;
            long startupSeconds = Long.getLong("fdp.smoke.startupSeconds", 180L);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(startupSeconds);
            Object instance = null;
            while (System.nanoTime() < deadline) {
                // Do not resolve Minecraft's method signatures while Genesis is still defining its UI classes.
                if ((Boolean.getBoolean("fdp.smoke.early") || Boolean.getBoolean("fdp.smoke.forge")) &&
                    !Boolean.getBoolean("fdpclient.ready") && !Boolean.getBoolean("fdpclient.lunar.ready")) {
                    Thread.sleep(200);
                    continue;
                }
                if (minecraft == null) {
                    for (Class<?> loaded : instrumentation.getAllLoadedClasses()) {
                        if (loaded.getName().equals("net.minecraft.client.Minecraft")) { minecraft = loaded; break; }
                    }
                }
                if (minecraft != null) {
                    try {
                        instance = minecraft.getMethod("getMinecraft").invoke(null);
                        if (instance != null && minecraft.getField("currentScreen").get(instance) != null) break;
                    } catch (ReflectiveOperationException notReady) { }
                }
                Thread.sleep(200);
            }
            if (instance == null || minecraft == null) throw new IllegalStateException("Minecraft did not initialize in " + startupSeconds + " seconds");
            Thread.sleep(2000);
            ClassLoader game = minecraft.getClassLoader();
            Thread.currentThread().setContextClassLoader(game);
            ClassLoader payload;
            if (Boolean.getBoolean("fdpclient.lunar.early") || Boolean.getBoolean("fdp.smoke.forge")) {
                payload = game;
                Class<?> client = Class.forName("net.ccbluex.liquidbounce.FDPClient", true, game);
                write(report, "CLIENT_SOURCE " + client.getProtectionDomain().getCodeSource().getLocation() + "\n");
                Object singleton = client.getField("INSTANCE").get(null);
                if (!Boolean.TRUE.equals(client.getMethod("isStarted").invoke(singleton))) {
                    throw new IllegalStateException("FDP's startup mixin did not initialize the client",
                        (Throwable) client.getMethod("getStartupFailure").invoke(singleton));
                }
            } else {
                throw new IllegalStateException("Launch with the production Lunar agent or the Forge coremod");
            }
            write(report, "LOADER " + game.getClass().getName() + "\nEARLY " + Boolean.getBoolean("fdpclient.lunar.early") + "\n");
            final Class<?> host = minecraft;
            final Object mc = instance;
            final Map<String, AtomicInteger> events = new ConcurrentHashMap<>();
            final float[] initialGamma = new float[1];
            final AtomicInteger silentEvents = new AtomicInteger();
            final AtomicInteger regularEvents = new AtomicInteger();
            onGameThread(host, mc, () -> {
                subscribe(payload, events);
                Class<?> manager = Class.forName("net.ccbluex.liquidbounce.features.module.ModuleManager", true, payload);
                int count = ((Collection<?>) manager.getField("INSTANCE").get(null)).size();
                if (count != 298) throw new IllegalStateException("Expected 298 built-in modules, got " + count);
                Class<?> menu = Class.forName("net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule", true, payload);
                if (Boolean.getBoolean("fdpclient.lunar.early") && ((Integer) menu.getMethod("getKeyBind").invoke(menu.getField("INSTANCE").get(null))) != 157) {
                    throw new IllegalStateException("Lunar's menu must default to Right Control");
                }
                if (Boolean.getBoolean("fdp.smoke.forge") && ((Integer) menu.getMethod("getKeyBind").invoke(menu.getField("INSTANCE").get(null))) != 54) {
                    throw new IllegalStateException("Forge's menu must retain Right Shift");
                }
                menu.getMethod("onEnable").invoke(menu.getField("INSTANCE").get(null));
                Object screen = host.getField("currentScreen").get(mc);
                if (screen == null || !screen.getClass().getName().startsWith("net.ccbluex.liquidbounce.")) {
                    throw new IllegalStateException("FDP ClickGUI did not open");
                }
                write(report, "MODULES " + count + "\nSCREEN " + screen.getClass().getName() + "\n");
            });
            Thread.sleep(3000);
            for (String style : new String[]{"Black", "Zywl", "Dropdown", "FDP"}) {
                onGameThread(host, mc, () -> {
                    openStyle(payload, style);
                    Object screen = host.getField("currentScreen").get(mc);
                    if (screen == null || !screen.getClass().getName().startsWith("net.ccbluex.liquidbounce.")) {
                        throw new IllegalStateException("ClickGUI style did not open: " + style);
                    }
                    write(report, "STYLE " + style + " " + screen.getClass().getName() + "\n");
                });
                Thread.sleep(750);
            }
            onGameThread(host, mc, () -> {
                Object screen = host.getField("currentScreen").get(mc);
                if (screen == null) throw new IllegalStateException("ClickGUI closed unexpectedly");
                Class<?> settings = Class.forName("net.minecraft.world.WorldSettings", true, game);
                Class<?> mode = Class.forName("net.minecraft.world.WorldSettings$GameType", true, game);
                Class<?> type = Class.forName("net.minecraft.world.WorldType", true, game);
                @SuppressWarnings({"unchecked", "rawtypes"}) Object creative = Enum.valueOf((Class) mode, "CREATIVE");
                Object flat = type.getField("FLAT").get(null);
                Object worldSettings = settings.getConstructor(long.class, mode, boolean.class, boolean.class, type)
                    .newInstance(0L, creative, false, false, flat);
                host.getMethod("launchIntegratedServer", String.class, String.class, settings).invoke(mc, "FdpSmoke", "FdpSmoke", worldSettings);
            });
            long worldDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(70);
            while (System.nanoTime() < worldDeadline) {
                Object player = host.getField("thePlayer").get(mc);
                if (player != null && player.getClass().getField("ticksExisted").getInt(player) >= 40) break;
                Thread.sleep(250);
            }
            onGameThread(host, mc, () -> {
                Object player = host.getField("thePlayer").get(mc);
                if (player == null) throw new IllegalStateException("The sandbox world did not open");
                for (String name : new String[]{"IMixinEntity", "IMixinEntityPlayerSP"}) {
                    if (!Class.forName("net.ccbluex.liquidbounce.injection.implementations." + name, false, game).isInstance(player)) {
                        throw new IllegalStateException("The local player has no " + name + " mixin");
                    }
                }
                host.getField("timer"); // Must be public, as on Forge.
                Class<?> vector = Class.forName("net.minecraft.util.Vec3", false, game);
                if (Modifier.isFinal(vector.getField("xCoord").getModifiers())) throw new IllegalStateException("Vec3 access rules were not applied");
                String probeJar = System.getProperty("fdp.smoke.probeJar");
                if (probeJar != null) {
                    game.getClass().getMethod("addURL", URL.class).invoke(game, new File(probeJar).toURI().toURL());
                    Class.forName("net.ccbluex.liquidbounce.verification.ForgeApiProbe", true, game).getMethod("run").invoke(null);
                    write(report, "COMPAT Forge API probe passed\n");
                }
                File scriptFile = new File((File) host.getField("mcDataDir").get(mc), "smoke-script.js");
                String source = "var s = registerScript({name:'FdpSmoke',version:'1.0',authors:['Smoke']});"
                    + "s.on('load', function(){Java.type('java.lang.System').setProperty('fdp.smoke.script.loaded',"
                    + "String(mc.thePlayer.getPosition().getX()));});";
                Files.write(scriptFile.toPath(), source.getBytes(StandardCharsets.UTF_8));
                Class<?> script = Class.forName("net.ccbluex.liquidbounce.script.Script", true, game);
                Object compiled = script.getConstructor(File.class).newInstance(scriptFile);
                script.getMethod("initScript").invoke(compiled);
                if (System.getProperty("fdp.smoke.script.loaded") == null) throw new IllegalStateException("JavaScript did not call the Minecraft API");
                write(report, "SCRIPT JavaScript loaded and called Minecraft\n");
                Class<?> groundPacket = Class.forName("net.minecraft.network.play.client.C03PacketPlayer", true, game);
                Class<?> packet = Class.forName("net.minecraft.network.Packet", true, game);
                Object silent = groundPacket.getConstructor(boolean.class).newInstance(true);
                Object regular = groundPacket.getConstructor(boolean.class).newInstance(true);
                OBSERVED_PACKETS.put(silent, silentEvents);
                OBSERVED_PACKETS.put(regular, regularEvents);
                Class<?> packets = Class.forName("net.ccbluex.liquidbounce.utils.client.PacketUtils", true, game);
                packets.getMethod("sendPacketSilently", packet).invoke(null, silent);
                packets.getMethod("sendPacket", packet, boolean.class).invoke(null, regular, true);
                Object options = host.getField("gameSettings").get(mc);
                initialGamma[0] = options.getClass().getField("gammaSetting").getFloat(options);
                options.getClass().getField("gammaSetting").setFloat(options, 0.0F);
                Class<?> fullbright = Class.forName("net.ccbluex.liquidbounce.features.module.modules.visual.Fullbright", true, game);
                fullbright.getMethod("setState", boolean.class).invoke(fullbright.getField("INSTANCE").get(null), true);
                options.getClass().getField("thirdPersonView").setInt(options, 1);
                Class<?> stack = Class.forName("net.minecraft.item.ItemStack", true, game);
                Class<?> item = Class.forName("net.minecraft.item.Item", true, game);
                Object chest = Class.forName("net.minecraft.init.Items", true, game).getField("diamond_chestplate").get(null);
                Object armor = stack.getConstructor(item).newInstance(chest);
                Class<?> enchantment = Class.forName("net.minecraft.enchantment.Enchantment", true, game);
                stack.getMethod("addEnchantment", enchantment, int.class).invoke(armor, enchantment.getField("protection").get(null), 1);
                player.getClass().getMethod("setCurrentItemOrArmor", int.class, stack).invoke(player, 3, armor);
            });
            Thread.sleep(4000);
            onGameThread(host, mc, () -> {
                Object player = host.getField("thePlayer").get(mc);
                Object renderer = host.getMethod("getItemRenderer").invoke(mc);
                Field held = renderer.getClass().getDeclaredField("itemToRender");
                held.setAccessible(true);
                Object previous = held.get(renderer);
                Class<?> item = Class.forName("net.minecraft.item.Item", true, game);
                Class<?> stack = Class.forName("net.minecraft.item.ItemStack", true, game);
                Class<?> items = Class.forName("net.minecraft.init.Items", true, game);
                try {
                    for (String name : new String[]{"stick", "cooked_beef", "potionitem", "diamond_sword", "bow"}) {
                        Object using = stack.getConstructor(item).newInstance(items.getField(name).get(null));
                        held.set(renderer, using);
                        int duration = (Integer) stack.getMethod("getMaxItemUseDuration").invoke(using);
                        player.getClass().getMethod("setItemInUse", stack, int.class).invoke(player, using, Math.max(1, duration - 5));
                        renderer.getClass().getMethod("renderItemInFirstPerson", float.class).invoke(renderer, 0.5F);
                    }
                } finally {
                    player.getClass().getMethod("clearItemInUse").invoke(player);
                    held.set(renderer, previous);
                }
                write(report, "RENDER first-person NONE, EAT, DRINK, BLOCK and BOW actions\n");
            });
            if (Boolean.getBoolean("fdp.smoke.browser")) {
                onGameThread(host, mc, () -> openStyle(payload, "NextGen"));
                Class<?> runtime = Class.forName("net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenBrowserRuntime", true, game);
                Object browser = runtime.getField("INSTANCE").get(null);
                long browserDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(300);
                while (System.nanoTime() < browserDeadline) {
                    String state = runtime.getMethod("getState").invoke(browser).toString();
                    if (state.equals("FAILED")) throw new IllegalStateException("NextGen browser failed: " + runtime.getMethod("getLastErrorLog").invoke(browser));
                    if (state.equals("READY") && Boolean.TRUE.equals(runtime.getMethod("isBrowserReady").invoke(browser))) break;
                    Thread.sleep(500);
                }
                if (!Boolean.TRUE.equals(runtime.getMethod("isBrowserReady").invoke(browser))) {
                    throw new IllegalStateException("NextGen never produced a rendered browser texture: " + runtime.getMethod("getDetail").invoke(browser));
                }
                write(report, "STYLE NextGen native browser ready with rendered texture\n");
                Thread.sleep(10000);
            }
            onGameThread(host, mc, () -> {
                if (silentEvents.get() != 0 || regularEvents.get() != 1) {
                    throw new IllegalStateException("Silent/regular packet events: " + silentEvents.get() + "/" + regularEvents.get());
                }
                write(report, "PACKETS silent send bypassed events; regular send emitted exactly once\n");
                Object options = host.getField("gameSettings").get(mc);
                Field gamma = options.getClass().getField("gammaSetting");
                if (gamma.getFloat(options) <= 0.0F) throw new IllegalStateException("Fullbright did not change world brightness");
                Class<?> fullbright = Class.forName("net.ccbluex.liquidbounce.features.module.modules.visual.Fullbright", true, game);
                fullbright.getMethod("setState", boolean.class).invoke(fullbright.getField("INSTANCE").get(null), false);
                if (gamma.getFloat(options) != 0.0F) throw new IllegalStateException("Fullbright did not restore brightness");
                gamma.setFloat(options, initialGamma[0]);
                write(report, "MODULE Fullbright enabled and restored gamma\n");
                for (String name : new String[]{"GameLoopEvent", "GameTickEvent", "MotionEvent", "UpdateEvent", "RotationUpdateEvent", "Render2DEvent", "Render3DEvent", "PacketEvent", "WorldEvent"}) {
                    int count = events.get(name).get();
                    if (count == 0) throw new IllegalStateException("No real " + name + " events were observed");
                    write(report, "EVENT " + name + " " + count + "\n");
                }
                write(report, "PASS " + (Boolean.getBoolean("fdp.smoke.forge") ? "Forge" : "Lunar") + " startup, ClickGUI, world, player mixins, access rules, HUD and packet events\n");
                host.getMethod("shutdown").invoke(mc);
            });
        } catch (Throwable failure) {
            while (failure instanceof InvocationTargetException) failure = ((InvocationTargetException) failure).getCause();
            StringWriter trace = new StringWriter();
            failure.printStackTrace(new PrintWriter(trace));
            try { write(report, "FAIL\n" + trace); } catch (Exception secondary) { secondary.printStackTrace(); }
        }
    }

    private interface Action { void run() throws Exception; }

    private static void openStyle(ClassLoader loader, String name) throws Exception {
        Class<?> menu = Class.forName("net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule", true, loader);
        Object singleton = menu.getField("INSTANCE").get(null);
        Object style = menu.getMethod("getValue", String.class).invoke(singleton, "Style");
        style.getClass().getMethod("set", Object.class, boolean.class).invoke(style, name, false);
        menu.getMethod("onEnable").invoke(singleton);
    }

    private static void subscribe(ClassLoader loader, Map<String, AtomicInteger> events) throws Exception {
        Class<?> listenable = Class.forName("net.ccbluex.liquidbounce.event.Listenable", true, loader);
        Class<?> action = Class.forName("kotlin.jvm.functions.Function1", true, loader);
        Object unit = Class.forName("kotlin.Unit", true, loader).getField("INSTANCE").get(null);
        Object owner = java.lang.reflect.Proxy.newProxyInstance(loader, new Class<?>[]{listenable}, (proxy, method, arguments) -> {
            switch (method.getName()) {
                case "handleEvents": return true;
                case "getSubListeners": return Array.newInstance(listenable, 0);
                case "hashCode": return System.identityHashCode(proxy);
                case "equals": return proxy == arguments[0];
                case "toString": return "FdpSmokeListener";
                default: return null;
            }
        });
        Class<?> hook = Class.forName("net.ccbluex.liquidbounce.event.EventHook", true, loader);
        Class<?> manager = Class.forName("net.ccbluex.liquidbounce.event.EventManager", true, loader);
        Object eventManager = manager.getField("INSTANCE").get(null);
        for (String name : new String[]{"GameLoopEvent", "GameTickEvent", "MotionEvent", "UpdateEvent", "RotationUpdateEvent", "Render2DEvent", "Render3DEvent", "PacketEvent", "WorldEvent"}) {
            AtomicInteger count = new AtomicInteger();
            events.put(name, count);
            Object callback = java.lang.reflect.Proxy.newProxyInstance(loader, new Class<?>[]{action}, (proxy, method, arguments) -> {
                if (method.getName().equals("invoke")) {
                    count.incrementAndGet();
                    if (name.equals("PacketEvent") && arguments[0].getClass().getMethod("getEventType").invoke(arguments[0]).toString().equals("SEND")) {
                        Object packet = arguments[0].getClass().getMethod("getPacket").invoke(arguments[0]);
                        AtomicInteger tracked = OBSERVED_PACKETS.get(packet);
                        if (tracked != null) tracked.incrementAndGet();
                    }
                }
                return unit;
            });
            Object instance = hook.getConstructor(listenable, boolean.class, byte.class, action).newInstance(owner, true, (byte) -120, callback);
            manager.getMethod("registerEventHook", Class.class, hook).invoke(eventManager,
                Class.forName("net.ccbluex.liquidbounce.event." + name, false, loader), instance);
        }
    }

    private static void onGameThread(Class<?> host, Object mc, Action action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        host.getMethod("addScheduledTask", Runnable.class).invoke(mc, (Runnable) () -> {
            try { action.run(); result.complete(null); }
            catch (Throwable failure) { result.completeExceptionally(failure); }
        });
        result.get(90, TimeUnit.SECONDS);
    }

    private static void write(Path file, String text) throws IOException {
        Files.write(file, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
