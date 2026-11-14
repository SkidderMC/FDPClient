# FDPClient Hacked Client - native Lunar verification by opZywl
# https://github.com/SkidderMC/FDPClient/ | https://lucas-lima.xyz
"""Exercise the built DLL in owned Java 17 VMs, without any -javaagent option."""
import argparse
import json
import shutil
from pathlib import Path
import subprocess
import uuid
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jdk", type=Path, required=True)
    parser.add_argument("--dll", type=Path, required=True)
    parser.add_argument("--bundle", type=Path, required=True)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    output = root / "build/native-loader-smoke" / uuid.uuid4().hex
    sources, launch, agent = (output / name for name in ("src", "launch", "agent"))
    for folder in (sources, launch, agent):
        folder.mkdir(parents=True)
    java_sources = {
        "com/moonsworth/lunar/genesis/Genesis.java": "package com.moonsworth.lunar.genesis; public final class Genesis {}",
        "net/minecraft/client/Minecraft.java": "package net.minecraft.client; public final class Minecraft {}",
        "fixture/Target.java": "package fixture; public final class Target {}",
        "fixture/Main.java": r'''
package fixture;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.lang.management.ManagementFactory;
public final class Main {
 public static void main(String[] a) throws Exception {
  String mode=a[0], pid=ManagementFactory.getRuntimeMXBean().getName().split("@",2)[0];
  Path plan=Paths.get(System.getProperty("user.home"),".zywl","injector","plans",pid+".ini");
  Files.createDirectories(plan.getParent());
  String selection="\uFEFF[selection]\r\nversion=1\r\npid="+pid+"\r\ncount=1\r\nmultiple=0\r\n[payload0]\r\nid=fdplunar\r\njar="+a[2]+"\r\n";
  Files.write(plan,selection.getBytes(StandardCharsets.UTF_16LE));
  if (!mode.equals("wrong-vm")) System.setProperty("java.class.path",System.getProperty("java.class.path")+";lunar-platform-mappings-v1_8.jar");
  if (!mode.equals("delayed")) Class.forName("com.moonsworth.lunar.genesis.Genesis");
  if (mode.equals("late")) Class.forName("net.minecraft.client.Minecraft");
  if (mode.equals("duplicate")) System.setProperty("fdpclient.lunar.agentRegistered","true");
  System.load(a[1]);
  if (mode.equals("delayed")) {Thread.sleep(300);Class.forName("com.moonsworth.lunar.genesis.Genesis");}
  Path result=Paths.get(plan.toString()+".result");
  long limit=System.currentTimeMillis()+30000;
  while (!Files.exists(result)) {
   if (mode.startsWith("real-agent") && Boolean.getBoolean("fdpclient.lunar.agentRegistered")) {
    System.out.println("PASS real current bundle registered through DLL");return;
   }
   if (System.currentTimeMillis()>limit) throw new IllegalStateException("No DLL result");
   Thread.sleep(20);
  }
  String message=new String(Files.readAllBytes(result),StandardCharsets.UTF_8);
  boolean success=mode.equals("ok") || mode.equals("delayed") || mode.equals("late");
  if (success) {
   if (!message.equals("ok")) throw new IllegalStateException(message);
   Class.forName("fixture.Target");
   if (!"true".equals(System.getProperty("fixture.transformed"))) throw new IllegalStateException("Native instrumentation did not observe class loading");
  } else {
   String expected=mode.equals("wrong-vm")?"does not support Forge":mode.equals("wrong-jar")?"self-contained":mode.equals("duplicate")?"already loaded":"unreachable";
   if (!message.startsWith("error: ") || !message.contains(expected) || System.getProperty("fixture.agent")!=null) throw new IllegalStateException("Unsafe result: "+message);
  }
  System.out.println("PASS "+mode+" "+message);
 }
}
''',
        "net/ccbluex/liquidbounce/injection/bootstrap/LunarAgent.java": r'''
package net.ccbluex.liquidbounce.injection.bootstrap;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
public final class LunarAgent {
 public static void agentmain(String a, Instrumentation i) {
  if (i.getAllLoadedClasses().length==0) throw new IllegalStateException("No instrumentation");
  if (!i.isRedefineClassesSupported() || !i.isRetransformClassesSupported()) throw new IllegalStateException("Runtime capabilities were not granted");
  i.addTransformer(new ClassFileTransformer() {
   public byte[] transform(ClassLoader l,String n,Class<?> c,ProtectionDomain d,byte[] b) {
    if (n.equals("fixture/Target")) System.setProperty("fixture.transformed","true");
    return null;
   }
  });
  System.setProperty("fixture.agent","true");
 }
 public static String nativeStatus() {return "true".equals(System.getProperty("fixture.agent"))?"ok":"waiting";}
}
''',
    }
    for name, content in java_sources.items():
        target = sources / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")
    javac, java = args.jdk / "bin/javac.exe", args.jdk / "bin/java.exe"
    entry = sources / "net/ccbluex/liquidbounce/injection/bootstrap/LunarAgent.java"
    subprocess.run([str(javac), "-proc:none", "-d", str(agent), str(entry)], check=True)
    subprocess.run([str(javac), "-proc:none", "-d", str(launch)] +
                   [str(sources / name) for name in java_sources if sources / name != entry], check=True)
    jars = {}
    for variant in ("valid", "invalid"):
        jar = output / ("agente espaço ç.jar" if variant == "valid" else "wrong.jar")
        with zipfile.ZipFile(jar, "w") as archive:
            manifest = "Manifest-Version: 1.0\nAgent-Class: net.ccbluex.liquidbounce.injection.bootstrap.LunarAgent\nCan-Redefine-Classes: true\nCan-Retransform-Classes: true\n"
            if variant == "valid":
                manifest += "FDP-Lunar-Bundle: 1.8.9\nFDP-Payload-Resource: FDPClient.jar\n"
            archive.writestr("META-INF/MANIFEST.MF", manifest + "\n")
            archive.writestr("FDPClient.jar", b"fixture")
            for file in agent.rglob("*.class"):
                archive.write(file, file.relative_to(agent).as_posix())
        jars[variant] = jar
    results = []
    renamed = output / "my renamed client espaço ç.jar"
    shutil.copy2(args.bundle, renamed)
    for mode in ("ok", "delayed", "late", "wrong-vm", "wrong-jar", "duplicate", "real-agent", "real-agent-renamed"):
        jar = (renamed if mode == "real-agent-renamed" else args.bundle.resolve()) if mode.startswith("real-agent") else jars["invalid" if mode == "wrong-jar" else "valid"]
        home = output / mode
        home.mkdir()
        run = subprocess.run([str(java), f"-Duser.home={home}", "-cp", str(launch), "fixture.Main", mode,
                              str(args.dll.resolve()), str(jar)], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=45)
        (home / "stdout.txt").write_text(run.stdout + run.stderr, encoding="utf-8")
        result = {"mode": mode, "pass": run.returncode == 0 and "PASS " in run.stdout}
        results.append(result)
        print(json.dumps(result), flush=True)
        if not result["pass"]:
            print((run.stdout + run.stderr)[-2200:])
    summary = {"pass": all(item["pass"] for item in results), "cases": results, "output": str(output)}
    (output / "summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    print(json.dumps(summary))
    return 0 if summary["pass"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
