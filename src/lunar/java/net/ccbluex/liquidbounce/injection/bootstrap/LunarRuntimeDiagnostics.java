/*
 * FDPClient Hacked Client
 * Lunar runtime diagnostics by opZywl | https://lucas-lima.xyz
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.lang.management.ManagementFactory;
import java.nio.file.*;
import javax.management.*;

/** Records HotSpot's underlying class-linkage failure without extra JVM arguments. */
final class LunarRuntimeDiagnostics implements AutoCloseable {
    private MBeanServer server;
    private ObjectName command;
    private Path output;

    static LunarRuntimeDiagnostics start() {
        LunarRuntimeDiagnostics diagnostics = new LunarRuntimeDiagnostics();
        try {
            Path directory = Paths.get(System.getProperty("user.home"), ".zywl", "fdpclient", "logs");
            Files.createDirectories(directory);
            diagnostics.server = ManagementFactory.getPlatformMBeanServer();
            diagnostics.command = new ObjectName("com.sun.management:type=DiagnosticCommand");
            diagnostics.output = directory.resolve("runtime-" + System.currentTimeMillis() + ".log");
            diagnostics.configure("redefine+class*=debug");
        } catch (Exception unavailable) {
            diagnostics.output = null;
            System.err.println("[FDPCLIENT] JVM runtime diagnostics unavailable: " + unavailable);
        }
        return diagnostics;
    }

    String location() { return output == null ? "" : "; JVM diagnostic log: " + output; }

    private void configure(String what) throws Exception {
        String path = output.toAbsolutePath().toString().replace('\\', '/');
        server.invoke(command, "vmLog", new Object[]{new String[]{"output=\"" + path + "\"", "what=" + what}},
            new String[]{"[Ljava.lang.String;"});
    }

    public void close() {
        if (output == null) return;
        try { configure("all=off"); }
        catch (Exception unavailable) { System.err.println("[FDPCLIENT] Could not close JVM runtime diagnostics: " + unavailable); }
    }
}
