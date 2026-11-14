# FDPClient - runtime attachment verification by opZywl | https://lucas-lima.xyz
# https://github.com/SkidderMC/FDPClient/
"""Run the real DLL and agent against owned Minecraft-shaped VMs, without a game account or GUI."""
import argparse
import io
import json
from pathlib import Path
import struct
import subprocess
import uuid
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jdk', type=Path, required=True)
    parser.add_argument('--dll', type=Path, required=True)
    parser.add_argument('--bundle', type=Path, required=True)
    options = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    output = root / 'build/runtime-attach-smoke' / uuid.uuid4().hex
    source, launch, payload = (output / name for name in ('src', 'launch', 'payload'))
    for folder in (source, launch, payload):
        folder.mkdir(parents=True)
    sources = {
        'com/moonsworth/lunar/genesis/Genesis.java': 'package com.moonsworth.lunar.genesis; public final class Genesis {}',
        'net/minecraft/client/gui/GuiScreen.java': 'package net.minecraft.client.gui; public class GuiScreen {}',
        'net/minecraft/client/multiplayer/WorldClient.java': 'package net.minecraft.client.multiplayer; public final class WorldClient {}',
        'net/minecraft/client/FutureHost.java': 'package net.minecraft.client; public class FutureHost {public int value(){return 1;}}',
        'net/minecraft/client/PlainHost.java': 'package net.minecraft.client; public final class PlainHost {private final int value;public PlainHost(int v){value=v;}public int value(){return value;}}',
        'net/minecraft/client/resources/FileResourcePack.java': 'package net.minecraft.client.resources; public final class FileResourcePack {public FileResourcePack(java.io.File f){}}',
        'net/minecraft/client/Minecraft.java': r'''
package net.minecraft.client;
import java.util.*; import java.util.concurrent.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
public class Minecraft {
 private static Minecraft instance;
 private final List<Object> defaultPacks=new ArrayList<>();
 public GuiScreen currentScreen=new GuiScreen();
 public WorldClient theWorld;
 private final BlockingQueue<Runnable> tasks=new LinkedBlockingQueue<>();
 private volatile boolean running=true;
 private volatile int ticks;
 public Minecraft(boolean world){instance=this;if(world)theWorld=new WorldClient();}
 public static Minecraft getMinecraft(){return instance;}
 public CompletableFuture<Void> addScheduledTask(Runnable r){CompletableFuture<Void> f=new CompletableFuture<>();tasks.add(()->{try{r.run();f.complete(null);}catch(Throwable t){f.completeExceptionally(t);}});return f;}
 public void runClientLoop() throws Exception {while(running)runGameLoop();}
 public void runGameLoop() throws Exception {ticks++;Runnable r=tasks.poll(10,TimeUnit.MILLISECONDS);if(r!=null)r.run();}
 public void refreshResources(){checkThread();System.setProperty("fixture.resources","true");}
 public void checkThread(){if(!Thread.currentThread().getName().equals("Client thread"))throw new IllegalStateException("Not the client thread");}
 public Object world(){return theWorld;}
 public Object screen(){return currentScreen;}
 public int ticks(){return ticks;}
 public void stop(){running=false;}
}
''',
        'fixture/GameLoader.java': r'''
package fixture;
import java.net.*;import java.nio.file.*;import java.util.concurrent.*;
public final class GameLoader extends URLClassLoader {
 private final ConcurrentMap<String,byte[]> cache=new ConcurrentHashMap<>();
 public GameLoader(URL[] urls,Path classes)throws Exception{super(urls,ClassLoader.getSystemClassLoader());try(java.util.stream.Stream<Path> files=Files.walk(classes.resolve("net/minecraft"))){files.filter(p->p.toString().endsWith(".class")).forEach(p->{try{cache.put(classes.relativize(p).toString().replace('\\','.').replace('/','.').replaceAll("\\.class$",""),Files.readAllBytes(p));}catch(Exception e){throw new RuntimeException(e);}});}}
 public ConcurrentMap<String,byte[]> bytecodeCache(){return cache;}
 protected synchronized Class<?> loadClass(String n,boolean resolve)throws ClassNotFoundException{
  if(!n.startsWith("net.minecraft.")&&(!n.startsWith("net.ccbluex.liquidbounce.")||n.startsWith("net.ccbluex.liquidbounce.injection.bootstrap.")||n.startsWith("net.ccbluex.liquidbounce.libraries.")))return super.loadClass(n,resolve);
  Class<?> c=findLoadedClass(n);if(c==null){byte[] b=cache.get(n);c=b==null?findClass(n):defineClass(n,b,0,b.length);}if(resolve)resolveClass(c);return c;
 }
}
''',
        'fixture/Main.java': r'''
package fixture;
import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.lang.management.*;import java.lang.reflect.*;import java.lang.instrument.*;
public final class Main {
 public static void main(String[] args)throws Exception{
  Class.forName("com.moonsworth.lunar.genesis.Genesis");
  Path classes=Paths.get(args[0]);
  GameLoader game=new GameLoader(new java.net.URL[]{classes.toUri().toURL(),Paths.get(args[1]).toUri().toURL()},classes);
  System.setProperty("java.class.path",System.getProperty("java.class.path")+";"+args[1]);
  Class<?> mc=Class.forName("net.minecraft.client.Minecraft",true,game);
  Object instance=mc.getConstructor(boolean.class).newInstance(!args[4].equals("menu"));
  Object world=mc.getMethod("world").invoke(instance),screen=mc.getMethod("screen").invoke(instance);
  int fieldCount=mc.getDeclaredFields().length;
  Thread client=new Thread(()->{try{mc.getMethod("runClientLoop").invoke(instance);}catch(Throwable t){t.printStackTrace();}},"Client thread");client.start();
  String pid=ManagementFactory.getRuntimeMXBean().getName().split("@",2)[0];
  Path plan=Paths.get(System.getProperty("user.home"),".zywl","injector","plans",pid+".ini");Files.createDirectories(plan.getParent());
  String text="\uFEFF[selection]\r\nversion=1\r\npid="+pid+"\r\ncount=1\r\nmultiple=0\r\n[payload0]\r\nid=fdplunar\r\njar="+args[3]+"\r\n";
  Files.write(plan,text.getBytes(StandardCharsets.UTF_16LE));
  try{
   System.load(args[2]);Path result=Paths.get(plan.toString()+".result");long deadline=System.currentTimeMillis()+60000;
   while(!Files.exists(result)){if(System.currentTimeMillis()>deadline)throw new IllegalStateException("No runtime result");Thread.sleep(20);}
   String status=new String(Files.readAllBytes(result),StandardCharsets.UTF_8);
   if(args[4].equals("failure")){
    if(!status.startsWith("error: ")||!status.contains("did not finish starting"))throw new IllegalStateException(status);
    if(Boolean.getBoolean("fdpclient.ready"))throw new IllegalStateException("Failed bootstrap reported ready");
   }else{
    if(!status.equals("ok"))throw new IllegalStateException(status);
    long hookLimit=System.currentTimeMillis()+3000;while(!Boolean.getBoolean("fixture.hooked")&&System.currentTimeMillis()<hookLimit)Thread.sleep(10);
    if(!Boolean.getBoolean("fixture.hooked"))throw new IllegalStateException("Live mixin hook did not execute");
    if(!Boolean.getBoolean("fixture.preload")||!Boolean.getBoolean("fixture.resources")||!Boolean.getBoolean("fixture.started"))throw new IllegalStateException("Client task queue did not initialize resources");
    if(world!=null&&!Boolean.getBoolean("fixture.worldEvent"))throw new IllegalStateException("Existing world was not initialized");
    Class<?> agent=Class.forName("net.ccbluex.liquidbounce.injection.bootstrap.LunarAgent");
    Field active=agent.getDeclaredField("active");active.setAccessible(true);Field access=agent.getDeclaredField("instrumentation");access.setAccessible(true);
    System.clearProperty("fixture.hooked");((Instrumentation)access.get(active.get(null))).retransformClasses(mc);
    hookLimit=System.currentTimeMillis()+3000;while(!Boolean.getBoolean("fixture.hooked")&&System.currentTimeMillis()<hookLimit)Thread.sleep(10);
    if(!Boolean.getBoolean("fixture.hooked"))throw new IllegalStateException("Retransformation removed the installed hooks");
    Class<?> future=Class.forName("net.minecraft.client.FutureHost",true,game);Object futureInstance=future.getConstructor().newInstance();
    if((Integer)future.getMethod("value").invoke(futureInstance)!=10)throw new IllegalStateException("Future class did not receive runtime hooks");
    ((Instrumentation)access.get(active.get(null))).retransformClasses(future);
    if((Integer)future.getMethod("value").invoke(futureInstance)!=10)throw new IllegalStateException("Future class lost hooks during retransformation");
    Class<?> plain=Class.forName("net.minecraft.client.PlainHost",true,game);Object plainInstance=plain.getConstructor(int.class).newInstance(42);
    if((Integer)plain.getMethod("value").invoke(plainInstance)!=42)throw new IllegalStateException("Unmodified legacy class was rewritten during future loading");
    ((Instrumentation)access.get(active.get(null))).retransformClasses(plain);
    if((Integer)plain.getMethod("value").invoke(plainInstance)!=42)throw new IllegalStateException("Unmodified legacy class changed during retransformation");
   }
   if(mc.getDeclaredFields().length!=fieldCount||mc.getMethod("world").invoke(instance)!=world||mc.getMethod("screen").invoke(instance)!=screen)throw new IllegalStateException("Existing game state changed");
   int tick=(Integer)mc.getMethod("ticks").invoke(instance);Thread.sleep(50);if((Integer)mc.getMethod("ticks").invoke(instance)<=tick)throw new IllegalStateException("Game loop stopped");
   System.out.println("PASS runtime "+args[4]+" "+status);
  }finally{mc.getMethod("stop").invoke(instance);client.join(5000);}
 }
}
''',
    }
    application = {
        'net/ccbluex/liquidbounce/FDPClient.java': r'''
package net.ccbluex.liquidbounce;
import net.minecraft.client.Minecraft;import java.util.concurrent.*;
public final class FDPClient {
 public static final FDPClient INSTANCE=new FDPClient();private boolean started;
 private FDPClient(){Minecraft.getMinecraft().checkThread();}
 public CompletableFuture<Void> preload(){Minecraft.getMinecraft().checkThread();System.setProperty("fixture.preload","true");return CompletableFuture.completedFuture(null);}
 public void startClient(){Minecraft.getMinecraft().checkThread();started=!System.getProperty("fixture.mode").equals("failure");System.setProperty("fixture.started",Boolean.toString(started));}
 public boolean isStarted(){return started;}
 public Throwable getStartupFailure(){return new IllegalStateException("fixture failure");}
}
''',
        'net/ccbluex/liquidbounce/event/Event.java': 'package net.ccbluex.liquidbounce.event; public class Event {}',
        'net/ccbluex/liquidbounce/event/WorldEvent.java': 'package net.ccbluex.liquidbounce.event;public final class WorldEvent extends Event {public WorldEvent(net.minecraft.client.multiplayer.WorldClient w){}}',
        'net/ccbluex/liquidbounce/event/EventManager.java': 'package net.ccbluex.liquidbounce.event;public final class EventManager {public static final EventManager INSTANCE=new EventManager();public Event call(Event e){net.minecraft.client.Minecraft.getMinecraft().checkThread();System.setProperty("fixture.worldEvent","true");return e;}}',
        'net/ccbluex/liquidbounce/injection/forge/mixins/fixture/MinecraftHook.java': r'''
package net.ccbluex.liquidbounce.injection.forge.mixins.fixture;
import net.ccbluex.liquidbounce.libraries.mixin.mixin.*;
import net.ccbluex.liquidbounce.libraries.mixin.mixin.injection.*;
import net.ccbluex.liquidbounce.libraries.mixin.mixin.injection.callback.CallbackInfo;
@Mixin(targets="net.minecraft.client.Minecraft",remap=false)
public class MinecraftHook {
 @Unique private int fdp$fixture=7;
 @Inject(method="runGameLoop",at=@At("HEAD"),remap=false)
 private void run(CallbackInfo ci){if(fdp$fixture!=7)throw new IllegalStateException("Mixin state was not initialized");System.setProperty("fixture.hooked","true");}
}
''',
        'net/ccbluex/liquidbounce/injection/forge/mixins/fixture/FutureHook.java': r'''
package net.ccbluex.liquidbounce.injection.forge.mixins.fixture;
import net.ccbluex.liquidbounce.libraries.mixin.mixin.*;
import net.ccbluex.liquidbounce.libraries.mixin.mixin.injection.*;
import net.ccbluex.liquidbounce.libraries.mixin.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="net.minecraft.client.FutureHost",remap=false)
public class FutureHook {
 @Unique private int fdp$fixture=9;
 @Inject(method="value",at=@At("RETURN"),cancellable=true,remap=false)
 private void value(CallbackInfoReturnable<Integer> ci){ci.setReturnValue(ci.getReturnValueI()+fdp$fixture);}
}
''',
    }
    javac, java = options.jdk / 'bin/javac.exe', options.jdk / 'bin/java.exe'
    for name, content in {**sources, **application}.items():
        path = source / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding='utf-8')
    subprocess.run([str(javac), '--release', '8', '-proc:none', '-d', str(launch)] + [str(source / name) for name in sources], check=True)
    legacy = launch / 'net/minecraft/client/PlainHost.class'
    legacy_bytes = bytearray(legacy.read_bytes())
    legacy_bytes[6:8] = struct.pack('>H', 50)
    legacy.write_bytes(legacy_bytes)
    subprocess.run([str(javac), '--release', '8', '-proc:none', '-cp', str(options.bundle.resolve()) + ';' + str(launch), '-d', str(payload)] + [str(source / name) for name in application], check=True)
    pairs = {'func_71410_x': 'getMinecraft', 'func_71411_J': 'runGameLoop', 'func_152344_a': 'addScheduledTask', 'func_110436_a': 'refreshResources', 'field_110449_ao': 'defaultPacks', 'field_71441_e': 'theWorld', 'field_71462_r': 'currentScreen'}
    def utf(text):
        data = text.encode('utf-8')
        return struct.pack('>H', len(data)) + data
    mapping = struct.pack('>IBq', 0x05E8F046, 0, 1) + utf('fixture') + utf('fixture') + struct.pack('>i', 0)
    mapping += struct.pack('>i', 0) + struct.pack('>i', len(pairs))
    for old, new in pairs.items():
        mapping += utf(old) + utf('()V') + utf(new)
    mappings = output / 'lunar-platform-mappings-v1_8.jar'
    with zipfile.ZipFile(mappings, 'w') as jar:
        jar.writestr('lunar/searge2lunar_1.8.9.kin', mapping)
    fake_payload = io.BytesIO()
    with zipfile.ZipFile(options.bundle) as current, zipfile.ZipFile(io.BytesIO(current.read('FDPClient.jar'))) as actual, zipfile.ZipFile(fake_payload, 'w') as jar:
        runtime_class = 'net/ccbluex/liquidbounce/injection/lunar/LunarRuntime.class'
        jar.writestr(runtime_class, actual.read(runtime_class))
        jar.writestr('META-INF/fdpclient_at.cfg', '')
        jar.writestr('fdpclient.lunar.mixins.json', json.dumps({'required': True, 'minVersion': '0.8', 'package': 'net.ccbluex.liquidbounce.injection.forge.mixins.fixture', 'compatibilityLevel': 'JAVA_8', 'mixins': ['MinecraftHook', 'FutureHook'], 'injectors': {'defaultRequire': 1}}))
        for file in payload.rglob('*.class'):
            jar.write(file, file.relative_to(payload).as_posix())
    bundle = output / 'renamed runtime client.jar'
    with zipfile.ZipFile(options.bundle) as current, zipfile.ZipFile(bundle, 'w') as jar:
        for name in current.namelist():
            if name != 'FDPClient.jar':
                jar.writestr(name, current.read(name))
        jar.writestr('FDPClient.jar', fake_payload.getvalue())
    results = []
    for mode in ('menu', 'world', 'failure'):
        home = output / mode
        home.mkdir()
        run = subprocess.run([str(java), f'-Duser.home={home}', f'-Dfixture.mode={mode}', '-cp', str(launch), 'fixture.Main', str(launch.resolve()), str(mappings.resolve()), str(options.dll.resolve()), str(bundle.resolve()), mode], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=90)
        (home / 'output.txt').write_text(run.stdout + run.stderr, encoding='utf-8')
        passed = run.returncode == 0 and f'PASS runtime {mode}' in run.stdout
        results.append({'mode': mode, 'pass': passed})
        print(json.dumps(results[-1]), flush=True)
        if not passed:
            print((run.stdout + run.stderr)[-6000:], flush=True)
    summary = {'pass': all(item['pass'] for item in results), 'cases': results, 'output': str(output)}
    (output / 'summary.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')
    print(json.dumps(summary))
    return 0 if summary['pass'] else 1


if __name__ == '__main__':
    raise SystemExit(main())
