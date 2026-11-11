<!-- FDPClient - Lunar compatibility by opZywl | https://lucas-lima.xyz -->
# Lunar Client 1.8.9

Use `fdpclient-inject-<version>.jar` (for example `fdpclient-inject-b18.jar`) with **ZAVZ** to load FDPClient on official Lunar Client 1.8.9.

1. Close the game.
2. In ZAVZ, open **JAR**, select **FDPClient Inject**, choose the JAR and click **Save selection**.
3. Click **Start**, then **Watch next Lunar startup**.
4. Launch Lunar Client **1.8.9** and wait for ZAVZ to confirm that FDPClient loaded.

Open the ClickGUI with **Right Control**, unless you saved another key.

ZAVZ includes the DLL. No `-javaagent` argument is needed; remove an old FDP argument before using this method. Start the watcher before opening the game.

You can rename the JAR. ZAVZ checks its contents, not its filename. For updates, choose the new JAR in ZAVZ.

**Forge:** keep using the regular Forge JAR in the `mods` folder.

Known issue: the NextGen browser can still log native browser errors.
