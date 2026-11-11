# FDPClient - Lunar compatibility by opZywl
# https://github.com/SkidderMC/FDPClient/
# Reference: https://lucas-lima.xyz

"""Validate the shared production JAR's Forge and Lunar packaging contracts."""
import argparse
import hashlib
import json
import struct
import zipfile


def utf8_constants(data):
    count = struct.unpack_from(">H", data, 8)[0]
    offset, index = 10, 1
    values = set()
    sizes = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
             11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
    while index < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            size = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            values.add(data[offset:offset + size].decode("utf-8", errors="replace"))
            offset += size
        else:
            offset += sizes[tag]
            if tag in {5, 6}:
                index += 1
        index += 1
    return values


def verify(path, agent=None, bundle=None):
    with zipfile.ZipFile(path) as jar:
        names = set(jar.namelist())
        manifest = jar.read("META-INF/MANIFEST.MF").decode("utf-8").replace("\r\n ", "").replace("\n ", "")
        required = {
            "FMLCorePlugin": "net.ccbluex.liquidbounce.injection.forge.MixinLoader",
            "MixinConfigs": "fdpclient.forge.mixins.json",
            "FDP-Lunar-Mode": "javaagent-1.8.9",
        }
        attributes = dict(line.split(": ", 1) for line in manifest.splitlines() if ": " in line)
        for key, value in required.items():
            assert attributes.get(key) == value, f"{key}: {attributes.get(key)!r}"
            if key.endswith("Class") or key == "FMLCorePlugin":
                assert value.replace(".", "/") + ".class" in names, f"Missing {value}"
        config = json.loads(jar.read("fdpclient.forge.mixins.json"))
        refmap = json.loads(jar.read(config["refmap"]))
        assert refmap.get("mappings"), "Empty Forge mixin refmap"
        assert "org/objectweb/asm/ClassReader.class" in names, "Forge ASM is missing"
        bridge = utf8_constants(jar.read("org/spongepowered/asm/bridge/RemapperAdapterFML.class"))
        assert "org/objectweb/asm/commons/Remapper" in bridge, "The Forge remapper lost its host ASM identity"
        assert not any(n.startswith("net/minecraft/lunarcheck/") for n in names), "Test host was bundled"
        constants = utf8_constants(jar.read("net/ccbluex/liquidbounce/injection/lunar/ClientSlider.class"))
        assert "func_146119_b" in constants, "Shared Minecraft overrides were not reobfuscated to SRG"
        assert "mouseDragged" not in constants, "Development names remain in the slider"
        renderer = utf8_constants(jar.read("org/cef/browser/CefRenderer.class"))
        assert "net/minecraft/client/renderer/BufferBuilder" not in renderer, "JCEF still targets Minecraft 1.12"
        assert "func_178180_c" in renderer, "JCEF's 1.8.9 tessellator call was not reobfuscated"
        classes = 0
        for name in names:
            if not name.endswith(".class"):
                continue
            header = jar.read(name)[:8]
            major = struct.unpack_from(">H", header, 6)[0]
            assert major <= 52, f"{name}: Java class version {major}"
            classes += 1
    print(f"Forge/Lunar artifact verified: {classes} Java 8 classes, Forge entry, Lunar mode, SRG overrides and Forge ASM identity")
    if agent:
        with zipfile.ZipFile(agent) as jar:
            names = set(jar.namelist())
            assert not any(n.upper().endswith((".SF", ".RSA", ".DSA")) for n in names), "Agent has invalidated dependency signatures"
            assert "net/ccbluex/liquidbounce/FDPClient.class" not in names, "The agent must not load the application in the system loader"
            assert "net/ccbluex/liquidbounce/injection/lunar/engine/EngineBootstrap.class" in names
            assert "net/ccbluex/liquidbounce/libraries/mixin/mixin/Mixin.class" in names
            assert "net/ccbluex/liquidbounce/libraries/asm/ClassReader.class" in names
            assert "org/objectweb/asm/ClassReader.class" not in names
            assert "org/openjdk/nashorn/api/scripting/JSObject.class" in names
            service = "META-INF/services/net.ccbluex.liquidbounce.libraries.mixin.service.IMixinService"
            assert jar.read(service).strip() == b"net.ccbluex.liquidbounce.injection.lunar.engine.RuntimeMixinService"
            for name in names:
                if name.endswith(".class"):
                    major = struct.unpack_from(">H", jar.read(name), 6)[0]
                    maximum = 61 if name.startswith(("net/ccbluex/liquidbounce/libraries/lunar/", "org/openjdk/nashorn/")) else 52
                    assert major <= maximum, f"Agent contains incompatible bytecode: {name} ({major})"
        print("Lunar agent verified: private Mixin, Java 17 scripting and isolated services")
    if bundle:
        with zipfile.ZipFile(bundle) as jar:
            manifest = jar.read("META-INF/MANIFEST.MF").decode("utf-8").replace("\r\n ", "").replace("\n ", "")
            attributes = dict(line.split(": ", 1) for line in manifest.splitlines() if ": " in line)
            entry = "net.ccbluex.liquidbounce.injection.bootstrap.LunarAgent"
            assert attributes.get("Agent-Class") == entry, "Native agent entry is missing"
            assert attributes.get("Premain-Class") == entry, "Early agent entry is missing"
            assert attributes.get("FDP-Lunar-Bundle") == "1.8.9", "Wrong native bundle contract"
            assert attributes.get("FDP-Payload-Resource") == "FDPClient.jar", "Embedded payload contract is missing"
            with open(path, "rb") as payload:
                assert hashlib.sha256(jar.read("FDPClient.jar")).digest() == hashlib.sha256(payload.read()).digest(), "Bundle contains an outdated payload"
            if agent:
                with zipfile.ZipFile(agent) as separate:
                    for name in separate.namelist():
                        if not name.endswith("/") and name != "META-INF/MANIFEST.MF":
                            assert jar.read(name) == separate.read(name), f"Bundle contains an outdated agent entry: {name}"
        print("Lunar native bundle verified: current agent, exact current payload and startup manifest")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar")
    parser.add_argument("--agent")
    parser.add_argument("--bundle")
    args = parser.parse_args()
    verify(args.jar, args.agent, args.bundle)
