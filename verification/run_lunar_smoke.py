# FDPClient - Lunar compatibility by opZywl
# https://github.com/SkidderMC/FDPClient/
# Reference: https://lucas-lima.xyz

"""Launch an offline Lunar 1.8.9 smoke test in a new, isolated game directory.

The production early-start agent installs the real mixins. A separate test agent
checks the resulting client and sandbox world without using the user's account.
"""
import argparse
import json
import os
from pathlib import Path
import subprocess
import shutil
import time
import uuid
import zipfile


def copy_browser_assets(previous, game):
    previous = previous.resolve(strict=True)
    checksum_file = previous / "config/mcefFiles.sha256"
    entries = ["mcef2.json", "config/mcefFiles.lst", "config/mcefFiles.sha256"]
    entries += [line[64:].lstrip() for line in checksum_file.read_text(encoding="utf-8").splitlines() if line]
    entries += [line.strip() for line in (previous / "config/mcefFiles.lst").read_text(encoding="utf-8").splitlines()
                if line.strip() and not line.startswith("#")]
    for name in dict.fromkeys(entries):
        source = (previous / name).resolve(strict=True)
        target = (game / name).resolve()
        if not source.is_relative_to(previous) or not target.is_relative_to(game):
            raise SystemExit("Browser cache entry leaves its profile")
        if source.is_dir():
            target.mkdir(parents=True, exist_ok=True)
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)
    fonts = previous / "FDPCLIENT/fonts"
    if fonts.is_dir():
        for font in fonts.rglob("*"):
            if font.is_file() and font.suffix.lower() in {".ttf", ".otf"}:
                source = font.resolve(strict=True)
                if not source.is_relative_to(previous):
                    raise SystemExit("Font cache entry leaves its profile")
                target = game / font.relative_to(previous)
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(source, target)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("--lunar-home", type=Path, default=Path.home() / ".lunarclient")
    parser.add_argument("--jdk", type=Path, default=os.environ.get("JAVA_HOME"))
    parser.add_argument("--java", type=Path, help="Java 17 executable; otherwise use Lunar's installed runtime")
    parser.add_argument("--jvm-arg", action="append", default=[], help="Additional JVM option; repeat as --jvm-arg=OPTION")
    parser.add_argument("--timeout", type=int, default=360)
    delivery = parser.add_mutually_exclusive_group(required=True)
    delivery.add_argument("--agent", type=Path, help="Production early-start Lunar agent")
    delivery.add_argument("--native-dll", type=Path, help="Deliver the current Lunar bundle through its real Windows DLL")
    parser.add_argument("--probe-classpath-file", type=Path, help="Compile the Forge API probe using writeSmokeClasspath's output")
    parser.add_argument("--browser", action="store_true", help="Also download/start NextGen's embedded browser")
    parser.add_argument("--browser-assets-from", type=Path, help="Reuse a previous smoke profile's verified browser cache")
    parser.add_argument("--cold-cache", action="store_true", help="Bake in an isolated copy without using Lunar's existing class cache")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    jar = args.jar.resolve(strict=True)
    with zipfile.ZipFile(jar) as archive:
        manifest = archive.read("META-INF/MANIFEST.MF")
        expected = b"FDP-Lunar-Bundle: 1.8.9" if args.native_dll else b"FDP-Lunar-Mode: javaagent-1.8.9"
        if expected not in manifest:
            raise SystemExit("The production JAR has no FDP Lunar entry")
    sandbox = root / "build/lunar-smoke" / uuid.uuid4().hex
    game, home = sandbox / "game", sandbox / "home"
    for folder in (game, home):
        folder.mkdir(parents=True)
    if args.browser_assets_from:
        copy_browser_assets(args.browser_assets_from, game)
    snapshot = sandbox / "FDPClient.jar"
    shutil.copy2(jar, snapshot)
    jar = snapshot
    (game / "options.txt").write_text("lang:en_US\npauseOnLostFocus:false\nguiScale:2\nrenderDistance:4\nsoundCategory_master:0.0\n", encoding="utf-8")
    agent_classes = sandbox / "agent-classes"
    agent_classes.mkdir()
    extension = ".exe" if os.name == "nt" else ""
    javac = args.jdk / "bin" / ("javac" + extension)
    subprocess.run([str(javac), "-source", "8", "-target", "8", "-d", str(agent_classes),
                    str(root / "verification/smoke/LunarSmokeAgent.java")], check=True)
    if args.native_dll:
        subprocess.run([str(javac), "-source", "8", "-target", "8", "-d", str(agent_classes),
                        str(root / "verification/smoke/NativeLunarLaunch.java")], check=True)
    if args.probe_classpath_file:
        compile_classpath = args.probe_classpath_file.read_text(encoding="utf-8").strip()
        subprocess.run([str(javac), "-source", "8", "-target", "8", "-cp", compile_classpath,
                        "-d", str(agent_classes), str(root / "verification/smoke/ForgeApiProbe.java")], check=True)
    agent = sandbox / "smoke-agent.jar"
    with zipfile.ZipFile(agent, "w") as archive:
        archive.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nPremain-Class: LunarSmokeAgent\n\n")
        for path in agent_classes.rglob("*.class"):
            archive.write(path, path.relative_to(agent_classes).as_posix())
    lunar = args.lunar_home.resolve(strict=True)
    multiver = lunar / "offline/multiver"
    required = ["common-0.1.0-SNAPSHOT-all-nomappings.jar", "lunar-platform-mappings-v1_8.jar",
                "optifine-0.1.0-SNAPSHOT-all.jar", "genesis-0.1.0-SNAPSHOT-all.jar",
                "legacy-0.1.0-SNAPSHOT-all-nomappings.jar", "lunar-lang.jar", "lunar.jar"]
    for name in required:
        if not (multiver / name).is_file():
            raise SystemExit(f"Launch Lunar 1.8.9 once first; missing {name}")
    runtimes = list(lunar.glob("jre/**/bin/javaw.exe"))
    if not args.java and not runtimes:
        raise SystemExit("Lunar's Java runtime is not installed yet; finish its download or supply --java with Java 17")
    java = args.java.resolve(strict=True) if args.java else max(runtimes, key=lambda p: p.stat().st_mtime)
    natives = max((p.parent for p in (multiver / "natives").rglob("lwjgl64.dll")), key=lambda p: p.stat().st_mtime)
    ui = max((p for p in (lunar / "ui").iterdir() if p.is_dir()), key=lambda p: p.stat().st_mtime)
    external = ["OptiFine_v1_8.jar"] + [p.relative_to(multiver).as_posix() for p in multiver.rglob("*.json")
                  if p.relative_to(multiver).parts[0] not in {"cache", "natives", "overrides"}]
    # Lunar also reads these data files relative to user.home, independently of gameDir.
    for name in external:
        if name.endswith(".json"):
            target = home / ".lunarclient/offline/multiver" / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(multiver / name, target)
    if args.cold_cache:
        copied = sandbox / "offline/multiver"
        for source in multiver.rglob("*"):
            relative = source.relative_to(multiver)
            if not source.is_file() or relative.parts[0] in {"cache", "natives", "overrides"}:
                continue
            target = copied / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)
        multiver = copied
    report = sandbox / "report.txt"
    command = [str(java), f"-javaagent:{agent}", f"-Dfdp.smoke.jar={jar}", f"-Dfdp.smoke.report={report}",
               f"-Dfdp.smoke.startupSeconds={min(300, args.timeout)}",
               "--add-modules", "jdk.naming.dns", "--add-exports", "jdk.naming.dns/com.sun.jndi.dns=java.naming",
               "--add-opens", "java.base/java.io=ALL-UNNAMED", "-Dlunar.webosr.url=file:index.html",
               "-Xmx3072m", f"-Djava.library.path={natives}", f"-Duser.home={home}",
               f"-Dichor.logsFile={sandbox / 'ichor.log'}", f"-Dichor.fabric.localModPath={sandbox / 'mods'}",
               "-Dichor.usingIsolatedProfiles=true", "-cp", os.pathsep.join(str(multiver / n) for n in required),
               "com.moonsworth.lunar.genesis.Genesis", "--version", "1.8.9", "--launcherVersion", "3.7.31-ow",
               "--username", "FdpCompatibilityTest", "--uuid", "11111111222233334444555555555555", "--accessToken", "0",
               "--userProperties", "{}", "--assetIndex", "1.8", "--gameDir", str(game),
               "--assetsDir", str(lunar / "shared/assets"), "--texturesDir", str(lunar / "textures"),
               "--uiDir", str(ui), "--webosrDir", str(natives), "--workingDirectory", ".", "--classpathDir", str(multiver),
               "--width", "1067", "--height", "622", "--ichorClassPath", ",".join(required),
               "--ichorExternalFiles", ",".join(external)]
    if args.probe_classpath_file:
        command.insert(1, f"-Dfdp.smoke.probeJar={agent}")
    command[1:1] = args.jvm_arg
    if args.browser:
        command.insert(1, "-Dfdp.smoke.browser=true")
    if args.agent:
        production_agent = sandbox / "production-agent.jar"
        shutil.copy2(args.agent.resolve(strict=True), production_agent)
        command.insert(1, f"-javaagent:{production_agent}={jar}")
        command.insert(2, "-Dfdp.smoke.early=true")
    if args.native_dll:
        main_index = command.index("com.moonsworth.lunar.genesis.Genesis")
        command[main_index] = "NativeLunarLaunch"
        classpath_index = command.index("-cp") + 1
        command[classpath_index] += os.pathsep + str(agent)
        command[1:1] = ["-Dfdp.smoke.early=true", f"-Dfdp.native.jar={jar}",
                        f"-Dfdp.native.dll={args.native_dll.resolve(strict=True)}"]
    with (sandbox / "stdout.txt").open("wb") as output:
        process = subprocess.Popen(command, cwd=game, stdout=output, stderr=subprocess.STDOUT,
                                   env=dict(os.environ, USERPROFILE=str(home), HOME=str(home)),
                                   creationflags=0x08000000 if os.name == "nt" else 0)
    (sandbox / "pid.txt").write_text(str(process.pid), encoding="utf-8")
    print(f"Sandbox: {sandbox}", flush=True)
    deadline = time.monotonic() + args.timeout
    try:
        while process.poll() is None and time.monotonic() < deadline:
            if report.exists() and "FAIL" in report.read_text(encoding="utf-8"):
                break
            time.sleep(0.5)
    finally:
        if process.poll() is None:
            if os.name == "nt":
                subprocess.run(["taskkill", "/PID", str(process.pid), "/T", "/F"], capture_output=True)
            else:
                process.terminate()
            process.wait(timeout=30)
    text = report.read_text(encoding="utf-8") if report.exists() else "No test report"
    output = (sandbox / "stdout.txt").read_text(encoding="utf-8", errors="replace")
    fatal = [marker for marker in ("[FDPCLIENT] Lunar agent error", "java.lang.instrument ASSERTION FAILED",
                                  'Exception in thread "Client thread"', 'Exception in thread "FDP Lunar CEF"',
                                  "Lunar browser operation failed", "Lunar browser frame failed", "StackOverflowError") if marker in output]
    summary = {"exit_code": process.returncode, "pass": process.returncode == 0 and "PASS Lunar startup" in text and not fatal,
               "fatal_diagnostics": fatal,
               "report": text, "sandbox": str(sandbox)}
    if args.native_dll:
        result = home / ".zywl/injector/plans" / f"{process.pid}.ini.result"
        native_status = result.read_text(encoding="utf-8") if result.exists() else "No native confirmation"
        summary["native_status"] = native_status
        summary["pass"] = summary["pass"] and native_status == "ok"
    (sandbox / "summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    print(json.dumps(summary, indent=2))
    return 0 if summary["pass"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
