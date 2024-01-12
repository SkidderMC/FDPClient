/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.crash.CrashReport;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.SharedDrawable;
import org.lwjgl.util.glu.GLU;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.glLoadIdentity;

public class CustomSplashProgress {
    private static Drawable d;
    private static volatile boolean pause = false;
    private static volatile boolean done = false;
    private static Thread thread;
    private static volatile Throwable threadError;
    private static final Lock lock = new ReentrantLock(true);
    private static final IResourcePack mcPack;
    private static final IResourcePack fmlPack;
    private static IResourcePack miscPack;
    private static CustomSplashProgress.Texture fontTexture;
    private static CustomSplashProgress.Texture backgroundTexture;
    private static Properties config;
    private static boolean enabled;
    public static final Semaphore mutex;
    private static int max_texture_size;
    private static final IntBuffer buf;

    private static String getString(String name, String def) {
        String value = config.getProperty(name, def);
        config.setProperty(name, value);
        return value;
    }

    private static boolean getBool(boolean def) {
        return Boolean.parseBoolean(getString("enabled", Boolean.toString(def)));
    }

    public static void start() {
        File configFile = new File(Minecraft.getMinecraft().mcDataDir, "config/splash.properties");
        File parent = configFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        FileReader r = null;
        config = new Properties();

        try {
            r = new FileReader(configFile);
            config.load(r);
        } catch (IOException var24) {
            FMLLog.info("Could not load splash.properties, will create a default one");
        } finally {
            IOUtils.closeQuietly(r);
        }

        boolean defaultEnabled = !System.getProperty("os.name").toLowerCase().contains("mac");
        enabled = getBool(defaultEnabled) && (!FMLClientHandler.instance().hasOptifine() || Launch.blackboard.containsKey("optifine.ForgeSplashCompatible"));
        final ResourceLocation fontLoc = new ResourceLocation(getString("fontTexture", "minecraft:textures/font/ascii.png"));
        final ResourceLocation backgroundLoc = new ResourceLocation(getString("backgroundTexture", "fdpclient/misc/splash.png"));
        File miscPackFile = new File(Minecraft.getMinecraft().mcDataDir, getString("resourcePackPath", "resources"));
        FileWriter w = null;

        try {
            w = new FileWriter(configFile);
            config.store(w, "Splash screen properties");
        } catch (IOException var22) {
            FMLLog.log(Level.ERROR, var22, "Could not save the splash.properties file");
        } finally {
            IOUtils.closeQuietly(w);
        }

        miscPack = createResourcePack(miscPackFile);
        if (enabled) {
            FMLCommonHandler.instance().registerCrashCallable(new ICrashCallable() {
                public String call() {
                    return "' Vendor: '" + GL11.glGetString(7936) + "' Version: '" + GL11.glGetString(7938) + "' Renderer: '" + GL11.glGetString(7937) + "'";
                }

                public String getLabel() {
                    return "GL info";
                }
            });
            CrashReport report = CrashReport.makeCrashReport(new Throwable() {
                public String getMessage() {
                    return "This is just a prompt for computer specs to be printed. THIS IS NOT A ERROR";
                }

                public void printStackTrace(PrintWriter s) {
                    s.println(this.getMessage());
                }

                public void printStackTrace(PrintStream s) {
                    s.println(this.getMessage());
                }
            }, "Loading screen debug info");
            System.out.println(report.getCompleteReport());

            try {
                d = new SharedDrawable(Display.getDrawable());
                Display.getDrawable().releaseContext();
                d.makeCurrent();
            } catch (LWJGLException var21) {
                var21.printStackTrace();
                disableSplash(var21);
            }

            getMaxTextureSize();
            thread = new Thread(new Runnable() {
                public void run() {
                    this.setGL();
                    CustomSplashProgress.fontTexture = new CustomSplashProgress.Texture(fontLoc);
                    CustomSplashProgress.backgroundTexture = new CustomSplashProgress.Texture(backgroundLoc);
                    GL11.glEnable(3553);
                    new SplashFontRenderer();
                    GL11.glDisable(3553);

                    for(; !CustomSplashProgress.done; Display.sync(100)) {
                        ProgressManager.ProgressBar first = null;
                        ProgressManager.ProgressBar penult = null;
                        ProgressManager.ProgressBar last = null;
                        Iterator<ProgressManager.ProgressBar> i = ProgressManager.barIterator();

                        while(i.hasNext()) {
                            if (first == null) {
                                first = i.next();
                            } else {
                                penult = last;
                                last = i.next();
                            }
                        }

                        GL11.glClear(16384);
                        int w = Display.getWidth();
                        int h = Display.getHeight();
                        GL11.glViewport(0, 0, w, h);
                        GL11.glMatrixMode(5889);
                        glLoadIdentity();
                        GL11.glOrtho(320 - w / 2, 320 + w / 2, 240 + h / 2, 240 - h / 2, -1.0, 1.0);
                        GL11.glMatrixMode(5888);
                        glLoadIdentity();

                        int w2 = Display.getWidth();
                        int h2 = Display.getHeight();
                        int startX = 320;
                        int startY = 240;

                        GL11.glEnable(3553);
                        CustomSplashProgress.backgroundTexture.bind();
                        GL11.glBegin(7);
                        CustomSplashProgress.backgroundTexture.texCoord(0, 0.0F, 0.0F);
                        GL11.glVertex2f((-w2 / 2) + startX, (-h2 / 2) + startY);
                        CustomSplashProgress.backgroundTexture.texCoord(0, 0.0F, 1F);
                        GL11.glVertex2f((-w2 / 2) + startX, (h2 / 2) + startY);
                        CustomSplashProgress.backgroundTexture.texCoord(0, 1F, 1F);
                        GL11.glVertex2f((w2 / 2) + startX, (h2 / 2) + startY);
                        CustomSplashProgress.backgroundTexture.texCoord(0, 1F, 0.0F);
                        GL11.glVertex2f((w2 / 2) + startX, (-h2 / 2) + startY);
                        GL11.glEnd();
                        GL11.glDisable(3553);

                        if (first != null) {
                            GL11.glPushMatrix();
                            GL11.glTranslatef(120.0F, 310.0F, 0.0F);
                            if (penult != null) {
                                GL11.glTranslatef(0.0F, 55.0F, 0.0F);
                            }

                            if (last != null) {
                                GL11.glTranslatef(0.0F, 55.0F, 0.0F);
                            }

                            GL11.glPopMatrix();
                        }

                        CustomSplashProgress.mutex.acquireUninterruptibly();
                        Display.update();
                        CustomSplashProgress.mutex.release();
                        if (CustomSplashProgress.pause) {
                            this.clearGL();
                            this.setGL();
                        }
                    }

                    this.clearGL();
                }

                private void setGL() {
                    CustomSplashProgress.lock.lock();

                    try {
                        Display.getDrawable().makeCurrent();
                    } catch (LWJGLException var2) {
                        var2.printStackTrace();
                        throw new RuntimeException(var2);
                    }

                    GL11.glDisable(2896);
                    GL11.glDisable(2929);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                }

                private void clearGL() {
                    Minecraft mc = Minecraft.getMinecraft();
                    mc.displayWidth = Display.getWidth();
                    mc.displayHeight = Display.getHeight();
                    mc.resize(mc.displayWidth, mc.displayHeight);
                    GL11.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glEnable(2929);
                    GL11.glDepthFunc(515);
                    GL11.glEnable(3008);
                    GL11.glAlphaFunc(516, 0.1F);

                    try {
                        Display.getDrawable().releaseContext();
                    } catch (LWJGLException var6) {
                        var6.printStackTrace();
                        throw new RuntimeException(var6);
                    } finally {
                        CustomSplashProgress.lock.unlock();
                    }

                }
            });
            thread.setUncaughtExceptionHandler((t, e) -> {
                FMLLog.log(Level.ERROR, e, "Splash thread Exception");
                CustomSplashProgress.threadError = e;
            });
            thread.start();
            checkThreadState();
        }
    }

    public static void getMaxTextureSize() {
        if (max_texture_size != -1) {
        } else {
            for(int i = 16384; i > 0; i >>= 1) {
                GL11.glTexImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, (ByteBuffer)null);
                if (GL11.glGetTexLevelParameteri(32868, 0, 4096) != 0) {
                    max_texture_size = i;
                    return;
                }
            }

        }
    }

    private static void checkThreadState() {
        if (thread.getState() == Thread.State.TERMINATED || threadError != null) {
            throw new IllegalStateException("Splash thread", threadError);
        }
    }

    /** @deprecated */
    @Deprecated
    public static void pause() {
        if (enabled) {
            checkThreadState();
            pause = true;
            lock.lock();

            try {
                d.releaseContext();
                Display.getDrawable().makeCurrent();
            } catch (LWJGLException var1) {
                var1.printStackTrace();
                throw new RuntimeException(var1);
            }
        }
    }

    /** @deprecated */
    @Deprecated
    public static void resume() {
        if (enabled) {
            checkThreadState();
            pause = false;

            try {
                Display.getDrawable().releaseContext();
                d.makeCurrent();
            } catch (LWJGLException var1) {
                var1.printStackTrace();
                throw new RuntimeException(var1);
            }

            lock.unlock();
        }
    }

    public static void finish() {
        if (enabled) {
            try {
                checkThreadState();
                done = true;
                thread.join();
                d.releaseContext();
                Display.getDrawable().makeCurrent();
                fontTexture.delete();
            } catch (Exception e) {
                e.printStackTrace();
                disableSplash(e);
            }

        }
    }

    private static void disableSplash(Exception e) {
        if (disableSplash()) {
            throw new EnhancedRuntimeException(e) {
                protected void printStackTrace(EnhancedRuntimeException.WrappedPrintStream stream) {
                    stream.println("CustomSplashProgress has detected a error loading Minecraft.");
                    stream.println("This can sometimes be caused by bad video drivers.");
                    stream.println("We have automatically disabeled the new Splash Screen in config/splash.properties.");
                    stream.println("Try reloading minecraft before reporting any errors.");
                }
            };
        } else {
            throw new EnhancedRuntimeException(e) {
                protected void printStackTrace(EnhancedRuntimeException.WrappedPrintStream stream) {
                    stream.println("CustomSplashProgress has detected a error loading Minecraft.");
                    stream.println("This can sometimes be caused by bad video drivers.");
                    stream.println("Please try disabeling the new Splash Screen in config/splash.properties.");
                    stream.println("After doing so, try reloading minecraft before reporting any errors.");
                }
            };
        }
    }

    private static boolean disableSplash() {
        File configFile = new File(Minecraft.getMinecraft().mcDataDir, "config/splash.properties");
        File parent = configFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        enabled = false;
        config.setProperty("enabled", "false");
        FileWriter w = null;

        try {
            w = new FileWriter(configFile);
            config.store(w, "Splash screen properties");
            return true;
        } catch (IOException var8) {
            FMLLog.log(Level.ERROR, var8, "Could not save the splash.properties file");

        } finally {
            IOUtils.closeQuietly(w);
        }

        return false;
    }

    private static IResourcePack createResourcePack(File file) {
        return file.isDirectory() ? new FolderResourcePack(file) : new FileResourcePack(file);
    }

    public static void checkGLError(String where) {
        int err = GL11.glGetError();
        if (err != 0) {
            throw new IllegalStateException(where + ": " + GLU.gluErrorString(err));
        }
    }

    private static InputStream open(ResourceLocation loc) throws IOException {
        if (miscPack.resourceExists(loc)) {
            return miscPack.getInputStream(loc);
        } else {
            return fmlPack.resourceExists(loc) ? fmlPack.getInputStream(loc) : mcPack.getInputStream(loc);
        }
    }

    static {
        mcPack = Minecraft.getMinecraft().mcDefaultResourcePack;
        fmlPack = createResourcePack(FMLSanityChecker.fmlLocation);
        mutex = new Semaphore(1);
        max_texture_size = -1;
        buf = BufferUtils.createIntBuffer(4194304);
    }

    private static class SplashFontRenderer extends FontRenderer {
        public SplashFontRenderer() {
            super(Minecraft.getMinecraft().gameSettings, CustomSplashProgress.fontTexture.location, null, false);
            super.onResourceManagerReload(null);
        }

        protected void bindTexture(ResourceLocation location) {
            if (location != this.locationFontTexture) {
                throw new IllegalArgumentException();
            } else {
                CustomSplashProgress.fontTexture.bind();
            }
        }

        protected InputStream getResourceInputStream(ResourceLocation location) throws IOException {
            return Minecraft.getMinecraft().mcDefaultResourcePack.getInputStream(location);
        }
    }

    private static class Texture {

        private final ResourceLocation location;
        private final int name;
        private final int width;
        private final int height;
        private final int size;

        public Texture(ResourceLocation location) {
            InputStream s = null;

            try {
                this.location = location;
                s = CustomSplashProgress.open(location);
                ImageInputStream stream = ImageIO.createImageInputStream(s);
                Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) {
                    throw new IOException("No suitable reader found for image" + location);
                } else {
                    ImageReader reader = readers.next();
                    reader.setInput(stream);
                    int frames = reader.getNumImages(true);
                    BufferedImage[] images = new BufferedImage[frames];

                    int size;
                    for(size = 0; size < frames; ++size) {
                        images[size] = reader.read(size);
                    }

                    reader.dispose();
                    size = 1;
                    this.width = images[0].getWidth();

                    for(this.height = images[0].getHeight(); size / this.width * (size / this.height) < frames; size *= 2) {
                    }

                    this.size = size;
                    GL11.glEnable(3553);
                    synchronized(CustomSplashProgress.class) {
                        this.name = GL11.glGenTextures();
                        GL11.glBindTexture(3553, this.name);
                    }

                    GL11.glTexParameteri(3553, 10241, 9728);
                    GL11.glTexParameteri(3553, 10240, 9728);
                    GL11.glTexImage2D(3553, 0, 6408, size, size, 0, 32993, 33639, (IntBuffer)null);
                    CustomSplashProgress.checkGLError("Texture creation");

                    for(int i = 0; i * (size / this.width) < frames; ++i) {
                        for(int j = 0; i * (size / this.width) + j < frames && j < size / this.width; ++j) {
                            CustomSplashProgress.buf.clear();
                            BufferedImage image = images[i * (size / this.width) + j];

                            for(int k = 0; k < this.height; ++k) {
                                for(int l = 0; l < this.width; ++l) {
                                    CustomSplashProgress.buf.put(image.getRGB(l, k));
                                }
                            }

                            CustomSplashProgress.buf.position(0).limit(this.width * this.height);
                            GL11.glTexSubImage2D(3553, 0, j * this.width, i * this.height, this.width, this.height, 32993, 33639, CustomSplashProgress.buf);
                            CustomSplashProgress.checkGLError("Texture uploading");
                        }
                    }

                    GL11.glBindTexture(3553, 0);
                    GL11.glDisable(3553);
                }
            } catch (IOException var18) {
                var18.printStackTrace();
                throw new RuntimeException(var18);
            } finally {
                IOUtils.closeQuietly(s);
            }
        }

        public void bind() {
            GL11.glBindTexture(3553, this.name);
        }

        public void delete() {
            GL11.glDeleteTextures(this.name);
        }

        public float getU(int frame, float u) {
            return (float)this.width * ((float)(frame % (this.size / this.width)) + u) / (float)this.size;
        }

        public float getV(int frame, float v) {
            return (float)this.height * ((float)(frame / (this.size / this.width)) + v) / (float)this.size;
        }

        public void texCoord(int frame, float u, float v) {
            GL11.glTexCoord2f(this.getU(frame, u), this.getV(frame, v));
        }
    }
}