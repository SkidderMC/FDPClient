<!-- FDPClient - Lunar compatibility by opZywl | https://lucas-lima.xyz -->
# Lunar Client 1.8.9

Use the current `fdpclient-inject-<version>.jar` with **ZAVZ** and official Lunar Client **1.8.9**.

1. Open Lunar Client 1.8.9. You can stay in the menu, singleplayer, or multiplayer.
2. In ZAVZ, open **JAR**, choose the new FDPClient Inject JAR from any folder, and click **Save selection**.
3. Click **Inject**, then **Inject into running Lunar**. Select the Lunar process and click **Inject**.
4. Wait for ZAVZ to confirm that FDPClient loaded.

Open the ClickGUI with **Right Control**, unless you saved another key.

ZAVZ includes the DLL. No `-javaagent` argument is needed. If FDP is already loaded in that game process, restart the game before loading another build.

You can rename the JAR. ZAVZ recognizes its contents. Keep it at the selected location; select it again if you move or update it. Older startup-only bundles need to be replaced with a current build for running-game injection.

Loading during startup also remains available through **Watch next Lunar startup**.

**Forge:** keep using the regular Forge JAR in the `mods` folder.

Known issue: the NextGen browser can still log native browser errors.
