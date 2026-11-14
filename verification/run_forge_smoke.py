# FDPClient - Lunar compatibility by opZywl
# https://github.com/SkidderMC/FDPClient/
# Reference: https://lucas-lima.xyz

"""Exercise the existing Forge development launcher in an isolated offline game directory."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import shutil
import time
import uuid
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jdk", type=Path, required=True)
    parser.add_argument("--timeout", type=int, default=600)
    parser.add_argument("--browser", action="store_true", help="Also verify NextGen's native browser texture")
    parser.add_argument("--browser-assets-from", type=Path, help="Reuse a previous smoke profile's browser cache")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    sandbox = root / "build/forge-smoke" / uuid.uuid4().hex
    game, home, classes = (sandbox / name for name in ("game", "home", "agent-classes"))
    for folder in (game, home, classes):
        folder.mkdir(parents=True)
    if args.browser_assets_from:
        from run_lunar_smoke import copy_browser_assets
        copy_browser_assets(args.browser_assets_from, game)
    (game / "options.txt").write_text("lang:en_US\npauseOnLostFocus:false\nguiScale:2\nrenderDistance:4\nsoundCategory_master:0.0\n", encoding="utf-8")
    javac = args.jdk / "bin/javac.exe"
    classpath = (root / "build/smoke-classpath.txt").read_text(encoding="utf-8").strip()
    subprocess.run([str(javac), "-source", "8", "-target", "8", "-cp", classpath, "-d", str(classes),
                    str(root / "verification/smoke/LunarSmokeAgent.java"),
                    str(root / "verification/smoke/ForgeApiProbe.java")], check=True)
    agent = sandbox / "smoke-agent.jar"
    with zipfile.ZipFile(agent, "w") as archive:
        archive.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nPremain-Class: LunarSmokeAgent\n\n")
        for path in classes.rglob("*.class"):
            archive.write(path, path.relative_to(classes).as_posix())
    report = sandbox / "report.txt"
    # runClient asks shadowJar for MCP development names. Preserve the tested SRG artifact.
    artifact = root / "build/libs/FDPClient-b17.jar"
    snapshot = sandbox / "FDPClient-production.jar"
    shutil.copy2(artifact, snapshot)
    from verify_lunar_artifact import utf8_constants
    with zipfile.ZipFile(snapshot) as archive:
        if "func_146119_b" not in utf8_constants(archive.read("net/ccbluex/liquidbounce/injection/lunar/ClientSlider.class")):
            raise SystemExit("Build the production JAR with gradlew.bat build before this test")
    init = sandbox / "smoke.init.gradle"
    quoted = lambda path: "'" + str(path).replace("\\", "/").replace("'", "\\'") + "'"
    init.write_text("""allprojects {
    afterEvaluate { p ->
        if (p.tasks.findByName('runClient') != null) {
            p.minecraft.runDir = %s
            p.minecraft.clientJvmArgs.removeAll { it.startsWith('-Dspotify.') }
            p.minecraft.clientJvmArgs += [
                '-javaagent:' + %s,
                '-Dfdp.smoke.forge=true',
                '-Dfdp.smoke.browser=%s',
                '-Dfdp.smoke.report=' + %s,
                '-Dfdp.smoke.probeJar=' + %s,
                '-Duser.home=' + %s
            ]
            p.tasks.runClient.workingDir = p.file(%s)
        }
    }
}
""" % (quoted(game), quoted(agent), str(args.browser).lower(), quoted(report), quoted(agent), quoted(home), quoted(game)), encoding="utf-8")
    command = [str(root / "gradlew.bat"), "runClient", "--no-daemon", "--console", "plain", "--init-script", str(init)]
    print(f"Sandbox: {sandbox}", flush=True)
    with (sandbox / "stdout.txt").open("wb") as output:
        process = subprocess.Popen(command, cwd=root, stdout=output, stderr=subprocess.STDOUT,
                                   env=dict(os.environ, JAVA_HOME=str(args.jdk)),
                                   creationflags=0x08000200 if os.name == "nt" else 0)
    deadline = time.monotonic() + args.timeout
    (sandbox / "pid.txt").write_text(str(process.pid), encoding="utf-8")
    try:
        while process.poll() is None and time.monotonic() < deadline:
            if report.exists() and "FAIL" in report.read_text(encoding="utf-8"):
                break
            if any((game / "crash-reports").glob("*.txt")):
                break
            time.sleep(0.5)
    finally:
        if process.poll() is None:
            # The process group belongs to this test launch; no existing game is targeted.
            subprocess.run(["taskkill", "/PID", str(process.pid), "/T", "/F"], capture_output=True)
            process.wait(timeout=30)
        shutil.copy2(snapshot, artifact)
    text = report.read_text(encoding="utf-8") if report.exists() else "No test report"
    output = (sandbox / "stdout.txt").read_text(encoding="utf-8", errors="replace")
    fatal = [marker for marker in ('Exception in thread "Client thread"', "StackOverflowError",
                                  "java.lang.instrument ASSERTION FAILED") if marker in output]
    summary = {"exit_code": process.returncode, "pass": process.returncode == 0 and "PASS Forge startup" in text and not fatal,
               "fatal_diagnostics": fatal,
               "report": text, "sandbox": str(sandbox)}
    (sandbox / "summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    print(json.dumps(summary, indent=2))
    return 0 if summary["pass"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
