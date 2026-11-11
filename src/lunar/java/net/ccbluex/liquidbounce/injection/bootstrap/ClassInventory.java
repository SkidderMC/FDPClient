/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** Package discovery must not depend on a particular host Log4j implementation. */
public final class ClassInventory {
    private ClassInventory() { }

    public static Set<String> names(ClassLoader loader, String packageName) throws IOException {
        String prefix = packageName.replace('.', '/') + "/";
        Set<String> result = new TreeSet<>();
        Set<String> visited = new HashSet<>();
        Enumeration<URL> resources = loader.getResources(prefix.substring(0, prefix.length() - 1));
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try {
                if (resource.getProtocol().equals("jar")) {
                    JarURLConnection connection = (JarURLConnection) resource.openConnection();
                    scan(new File(connection.getJarFileURL().toURI()), prefix, result, visited);
                } else if (resource.getProtocol().equals("file")) {
                    directory(Paths.get(resource.toURI()), packageName, result);
                }
            } catch (URISyntaxException invalid) { throw new IOException(invalid); }
        }
        // JARs are allowed to omit directory entries; getResources(package) then returns nothing.
        for (ClassLoader current = loader; current != null; current = current.getParent()) {
            if (!(current instanceof URLClassLoader)) continue;
            for (URL url : ((URLClassLoader) current).getURLs()) {
                if (!url.getProtocol().equals("file")) continue;
                try {
                    File file = new File(url.toURI());
                    if (file.isFile() && file.getName().endsWith(".jar")) scan(file, prefix, result, visited);
                    else if (file.isDirectory()) directory(file.toPath().resolve(prefix), packageName, result);
                } catch (URISyntaxException invalid) { throw new IOException(invalid); }
            }
        }
        String runtime = System.getProperty("fdpclient.lunar.runtimeJar");
        if (runtime != null) scan(new File(runtime), prefix, result, visited);
        return result;
    }

    private static void scan(File file, String prefix, Set<String> result, Set<String> visited) throws IOException {
        if (!visited.add(file.getCanonicalPath())) return;
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(prefix) && name.endsWith(".class")) result.add(name.substring(0, name.length() - 6).replace('/', '.'));
            }
        }
    }

    private static void directory(Path directory, String packageName, Set<String> result) throws IOException {
        if (!Files.isDirectory(directory)) return;
        try (java.util.stream.Stream<Path> files = Files.walk(directory)) {
            files.filter(path -> path.toString().endsWith(".class")).forEach(path -> {
                String relative = directory.relativize(path).toString().replace(File.separatorChar, '.');
                result.add(packageName + "." + relative.substring(0, relative.length() - 6));
            });
        }
    }
}
