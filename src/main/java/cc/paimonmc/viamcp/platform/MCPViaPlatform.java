package cc.paimonmc.viamcp.platform;

import cc.paimonmc.viamcp.ViaMCP;
import cc.paimonmc.viamcp.utils.FutureTaskId;
import cc.paimonmc.viamcp.utils.JLoggerToLog4j;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MCPViaPlatform implements ViaPlatform<UUID> {
    private final Logger logger = new JLoggerToLog4j(LogManager.getLogger("ViaVersion"));

    private final MCPViaConfig config;
    private final File dataFolder;
    private final ViaAPI<UUID> api;

    public MCPViaPlatform(final File dataFolder) {
        final Path configDir = dataFolder.toPath().resolve("ViaVersion");
        config = new MCPViaConfig(configDir.resolve("viaversion.yml").toFile());
        this.dataFolder = configDir.toFile();
        api = new MCPViaAPI();
    }

    public static String legacyToJson(final String legacy) {
        return GsonComponentSerializer.gson().serialize(LegacyComponentSerializer.legacySection().deserialize(legacy));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getPlatformName() {
        return "ViaMCP";
    }

    @Override
    public String getPlatformVersion() {
        return String.valueOf(ViaMCP.PROTOCOL_VERSION);
    }

    @Override
    public String getPluginVersion() {
        return "4.1.1";
    }

    @Override
    public FutureTaskId runAsync(final Runnable runnable) {
        return new FutureTaskId(CompletableFuture.runAsync(runnable, ViaMCP.getInstance().getAsyncExecutor()).exceptionally(throwable ->
        {
            if (!(throwable instanceof CancellationException)) {
                throwable.printStackTrace();
            }

            return null;
        })
        );
    }

    @Override
    public FutureTaskId runSync(final Runnable runnable) {
        return new FutureTaskId(ViaMCP.getInstance().getEventLoop().submit(runnable).addListener(errorLogger()));
    }

    @Override
    public PlatformTask runSync(final Runnable runnable, final long ticks) {
        return new FutureTaskId(ViaMCP.getInstance().getEventLoop().schedule(() -> runSync(runnable), ticks * 50, TimeUnit.MILLISECONDS).addListener(errorLogger()));
    }

    @Override
    public PlatformTask runRepeatingSync(final Runnable runnable, final long ticks) {
        return new FutureTaskId(ViaMCP.getInstance().getEventLoop().scheduleAtFixedRate(() -> runSync(runnable), 0, ticks * 50, TimeUnit.MILLISECONDS).addListener(errorLogger()));
    }

    private <T extends Future<?>> GenericFutureListener<T> errorLogger() {
        return future ->
        {
            if (!future.isCancelled() && future.cause() != null) {
                future.cause().printStackTrace();
            }
        };
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        return new ViaCommandSender[1337]; // What the fuck
    }

    private ViaCommandSender[] getServerPlayers() {
        return new ViaCommandSender[1337]; // What the fuck 2: Electric Boogaloo
    }

    @Override
    public void sendMessage(final UUID uuid, final String s) {
        // Don't even know why this needs to be overridden
    }

    @Override
    public boolean kickPlayer(final UUID uuid, final String s) {
        return false;
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ViaAPI<UUID> getApi() {
        return api;
    }

    @Override
    public ViaVersionConfig getConf() {
        return config;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return config;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public void onReload() {
        logger.info("ViaVersion was reloaded? (How did that happen)");
    }

    @Override
    public JsonObject getDump() {
        return new JsonObject();
    }

    @Override
    public boolean isOldClientsAllowed() {
        return true;
    }

    @Override
    public boolean hasPlugin(final String s) {
        return false;
    }
}
