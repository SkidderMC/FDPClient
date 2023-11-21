package cc.paimonmc.viamcp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import io.netty.channel.EventLoop;
import io.netty.channel.local.LocalEventLoopGroup;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.logging.log4j.LogManager;
import cc.paimonmc.viamcp.gui.AsyncVersionSlider;
import cc.paimonmc.viamcp.loader.MCPBackwardsLoader;
import cc.paimonmc.viamcp.loader.MCPRewindLoader;
import cc.paimonmc.viamcp.loader.MCPViaLoader;
import cc.paimonmc.viamcp.platform.MCPViaInjector;
import cc.paimonmc.viamcp.platform.MCPViaPlatform;
import cc.paimonmc.viamcp.utils.JLoggerToLog4j;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;

public class ViaMCP {
    public final static int PROTOCOL_VERSION = 47;
    private static final ViaMCP instance = new ViaMCP();

    public static ViaMCP getInstance() {
        return instance;
    }

    private final Logger jLogger = new JLoggerToLog4j(LogManager.getLogger("ViaMCP"));
    private final CompletableFuture<Void> INIT_FUTURE = new CompletableFuture<>();

    private ExecutorService ASYNC_EXEC;
    private EventLoop EVENT_LOOP;

    private File file;
    private int version;
    private String lastServer;

    public static boolean hidden;

    public static void staticInit() {
        getInstance().start();
    }

    /**
     * Version Slider that works Asynchronously with the Version GUI
     * Please initialize this before usage with initAsyncSlider() or initAsyncSlider(x, y, width (min. 110), height)
     */
    public AsyncVersionSlider asyncSlider;

    public void start() {
        try {
            final ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ViaMCP-%d").build();
            ASYNC_EXEC = Executors.newFixedThreadPool(8, factory);

            EVENT_LOOP = new LocalEventLoopGroup(1, factory).next();
            EVENT_LOOP.submit(INIT_FUTURE::join);

            setVersion(PROTOCOL_VERSION);
            this.file = new File("ViaMCP");
            if (this.file.mkdir()) {
                this.getjLogger().info("Creating ViaMCP Folder");
            }

            Via.init(ViaManagerImpl.builder().injector(new MCPViaInjector()).loader(new MCPViaLoader()).platform(new MCPViaPlatform(file)).build());

            MappingDataLoader.enableMappingsCache();
            ((ViaManagerImpl) Via.getManager()).init();

            new MCPBackwardsLoader(file);
            new MCPRewindLoader(file);

            INIT_FUTURE.complete(null);

            //ViaMCP.getInstance().initAsyncSlider();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    public Logger getjLogger() {
        return jLogger;
    }

    public CompletableFuture<Void> getInitFuture() {
        return INIT_FUTURE;
    }

    public ExecutorService getAsyncExecutor() {
        return ASYNC_EXEC;
    }

    public EventLoop getEventLoop() {
        return EVENT_LOOP;
    }

    public File getFile() {
        return file;
    }

    public String getLastServer() {
        return lastServer;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public void setFile(final File file) {
        this.file = file;
    }

    public void setLastServer(final String lastServer) {
        this.lastServer = lastServer;
    }

    public static boolean isHidden() {
        return hidden;
    }

    public static void setHidden(boolean hidden) {
        ViaMCP.hidden = hidden;
    }
}
