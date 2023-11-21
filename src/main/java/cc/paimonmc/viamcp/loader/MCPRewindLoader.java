package cc.paimonmc.viamcp.loader;

import com.viaversion.viaversion.api.Via;
import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;

import java.io.File;
import java.util.logging.Logger;

public class MCPRewindLoader implements ViaRewindPlatform {
    public MCPRewindLoader(final File file) {
        final ViaRewindConfigImpl conf = new ViaRewindConfigImpl(file.toPath().resolve("ViaRewind").resolve("config.yml").toFile());
        conf.reloadConfig();
        this.init(conf);
    }

    @Override
    public Logger getLogger() {
        return Via.getPlatform().getLogger();
    }
}
