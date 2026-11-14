# FDPClient - Lunar compatibility by opZywl
# https://github.com/SkidderMC/FDPClient/
# Reference: https://lucas-lima.xyz

"""Verify the injection boundary without configuring ForgeGradle or launching Minecraft.

Supply ASM 9.7.1 core, tree and commons on --classpath. With --compile-runtime, supply
the mapped Minecraft classpath written by Gradle's writeSmokeClasspath task.
"""
import argparse
import os
from pathlib import Path
import subprocess
import uuid


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--classpath", required=True)
    parser.add_argument("--java-home", type=Path, default=os.environ.get("JAVA_HOME"))
    parser.add_argument("--lunar-map-jar", type=Path)
    parser.add_argument("--compile-runtime", action="store_true")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    output = root / "build" / "lunar-verification" / uuid.uuid4().hex
    output.mkdir(parents=True)
    binaries = args.java_home / "bin" if args.java_home else Path()
    extension = ".exe" if os.name == "nt" else ""
    javac = str(binaries / ("javac" + extension))
    java = str(binaries / ("java" + extension))
    sources = list((root / "src/lunar/java/net/ccbluex/liquidbounce/injection/bootstrap").glob("*.java"))
    if args.compile_runtime:
        sources += list((root / "src/lunar/java/net/ccbluex/liquidbounce/injection/lunar").glob("*.java"))
    sources += list((root / "src/lunarTest/java").rglob("*.java"))
    version = subprocess.run([javac, "-version"], capture_output=True, text=True, check=True)
    java8 = "1.8." in version.stdout + version.stderr
    target = ["-source", "8", "-target", "8"] if java8 else ["--release", "8"]
    subprocess.run([javac, *target, "-encoding", "UTF-8", "-cp", args.classpath,
                    "-d", str(output), *map(str, sources)], check=True)
    command = [java, "-cp", str(output) + os.pathsep + args.classpath,
               "net.ccbluex.liquidbounce.injection.bootstrap.LunarBridgeVerification"]
    if args.lunar_map_jar:
        command.append(str(args.lunar_map_jar.resolve()))
    subprocess.run(command, check=True)


if __name__ == "__main__":
    main()
