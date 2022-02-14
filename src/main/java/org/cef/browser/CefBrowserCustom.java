package org.cef.browser;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import net.ccbluex.liquidbounce.ui.cef.CefRenderManager;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import org.cef.CefClient;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

/**
 * CefBrowserOsr but with custom rendering
 * @see org.cef.browser.CefBrowser_N is fucking package private
 * @author montoyo, Feather Client Team, modified by Liulihaocai
 */
public class CefBrowserCustom extends CefBrowser_N implements CefRenderHandler {
    private final ICefRenderer renderer_;
    private boolean justCreated_ = false;
    private final Rectangle browser_rect_ = new Rectangle(0, 0, 1, 1);
    private final Point screenPoint_ = new Point(0, 0);
    private final boolean isTransparent_;
    private final Component dc_ = new Component(){};

    public CefBrowserCustom(CefClient client, String url, boolean transparent, CefRequestContext context, ICefRenderer renderer) {
        this(client, url, transparent, context, renderer, null, null);
    }

    public CefBrowserCustom(CefClient client, String url, boolean transparent, CefRequestContext context, ICefRenderer renderer, CefBrowserCustom parent, Point inspectAt) {
        super(client, url, context, parent, inspectAt);
        this.isTransparent_ = transparent;
        this.renderer_ = renderer;
        CefRenderManager.INSTANCE.getBrowsers().add(this);
    }

    @Override
    public void createImmediately() {
        this.justCreated_ = true;
        this.createBrowserIfRequired(false);
    }

    @Override
    public Component getUIComponent() {
        return this.dc_;
    }

    @Override
    public CefRenderHandler getRenderHandler() {
        return this;
    }

    @Override
    protected CefBrowser_N createDevToolsBrowser(CefClient client, String url, CefRequestContext context, CefBrowser_N parent, Point inspectAt) {
        return new CefBrowserCustom(client, url, this.isTransparent_, context, null, this, inspectAt);
    }

    private synchronized long getWindowHandle() {
        return 0L;
    }

    @Override
    public Rectangle getViewRect(CefBrowser browser) {
        return this.browser_rect_;
    }

    @Override
    public Point getScreenPoint(CefBrowser browser, Point viewPoint) {
        Point screenPoint = new Point(this.screenPoint_);
        screenPoint.translate(viewPoint.x, viewPoint.y);
        return screenPoint;
    }

    @Override
    public void onPopupShow(CefBrowser browser, boolean show) {
        if (!show) {
            this.renderer_.onPopupClosed();
            this.invalidate();
        }
    }

    @Override
    public void onPopupSize(CefBrowser browser, Rectangle size) {
        this.renderer_.onPopupSize(size);
    }

    private static class PaintData {
        private ByteBuffer buffer;
        private int width;
        private int height;
        private Rectangle[] dirtyRects;
        private boolean hasFrame;
        private boolean fullReRender;
    }

    private final PaintData paintData = new PaintData();

    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects,
                        ByteBuffer buffer, int width, int height) {
        if(popup)
            return;

        final int size = (width * height) << 2;

        synchronized(paintData) {
            if(buffer.limit() > size)
                ClientUtils.INSTANCE.logWarn("Skipping MCEF browser frame, data is too heavy"); //TODO: Don't spam
            else {
                if(paintData.hasFrame) //The previous frame was not uploaded to GL texture, so we skip it and render this on instead
                    paintData.fullReRender = true;

                if(paintData.buffer == null || size != paintData.buffer.capacity()) //This only happens when the browser gets resized
                    paintData.buffer = BufferUtils.createByteBuffer(size);

                paintData.buffer.position(0);
                paintData.buffer.limit(buffer.limit());
                buffer.position(0);
                paintData.buffer.put(buffer);
                paintData.buffer.position(0);

                paintData.width = width;
                paintData.height = height;
                paintData.dirtyRects = dirtyRects;
                paintData.hasFrame = true;
            }
        }
    }

    public void mcefUpdate() {
        synchronized(paintData) {
            if(paintData.hasFrame) {
                renderer_.onPaint(false, paintData.dirtyRects, paintData.buffer, paintData.width, paintData.height, paintData.fullReRender);
                paintData.hasFrame = false;
                paintData.fullReRender = false;
            }
        }
    }

    @Override
    public boolean onCursorChange(CefBrowser browser, int cursorType) {
        return true;
    }

    @Override
    public boolean startDragging(CefBrowser browser, CefDragData dragData, int mask, int x, int y) {
        return false;
    }

    @Override
    public void updateDragCursor(CefBrowser browser, int operation) {
    }

    private void createBrowserIfRequired(boolean hasParent) {
        long windowHandle = 0L;
        if (hasParent) {
            windowHandle = this.getWindowHandle();
        }
        if (this.getNativeRef("CefBrowser") == 0L) {
            if (this.getParentBrowser() != null) {
                this.createDevTools(this.getParentBrowser(), this.getClient(), windowHandle, true, this.isTransparent_, null, this.getInspectAt());
            } else {
                this.createBrowser(this.getClient(), windowHandle, this.getUrl(), true, this.isTransparent_, null, this.getRequestContext());
            }
        } else if (hasParent && this.justCreated_) {
            this.notifyAfterParentChanged();
            this.setFocus(true);
            this.justCreated_ = false;
        }
    }

    private void notifyAfterParentChanged() {
        this.getClient().onAfterParentChanged(this);
    }

    @Override
    public boolean getScreenInfo(CefBrowser browser, CefScreenInfo screenInfo) {
        int depth_per_component = 8;
        int depth = 32;
        double scaleFactor_ = 1.0;
        screenInfo.Set(scaleFactor_, depth, depth_per_component, false, this.browser_rect_.getBounds(), this.browser_rect_.getBounds());
        return true;
    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        return null;
    }

    @Override
    public void close(boolean force) {
        CefRenderManager.INSTANCE.getBrowsers().remove(this);
        this.renderer_.destroy();
        super.close(force);
    }

    // these methods are fucking protected in the superclass, we need to wrap it

    public void wasResized_(int width, int height) {
        this.browser_rect_.setBounds(0, 0, width, height);
        super.wasResized(width, height);
    }

    public void mouseMoved(int x, int y, int mods) {
        MouseEvent ev = new MouseEvent(dc_, MouseEvent.MOUSE_MOVED, 0, mods, x, y, 0, false);
        sendMouseEvent(ev);
    }

    public void mouseInteracted(int x, int y, int mods, int btn, boolean pressed, int ccnt) {
        MouseEvent ev = new MouseEvent(dc_, pressed ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED, 0, mods, x, y, ccnt, false, remapMouseCode(btn));
        sendMouseEvent(ev);
    }

    private static int remapMouseCode(int kc) {
        switch (kc) {
            case 0: return 1;
            case 1: return 3;
            case 2: return 2;
            default: return 0;
        }
    }

    public void mouseScrolled(int x, int y, int mods, int amount, int rot) {
        MouseWheelEvent ev = new MouseWheelEvent(dc_, MouseEvent.MOUSE_WHEEL, 0, mods, x, y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, amount, rot);
        sendMouseWheelEvent(ev);
    }

    public void keyTyped(char c, int mods) {
        KeyEvent ev = new KeyEvent(dc_, KeyEvent.KEY_TYPED, 0, mods, 0, c);
        sendKeyEvent(ev);
    }

    /**
     * fill the gap between LWJGL and AWT key codes
     * https://stackoverflow.com/questions/15313469/java-keyboard-keycodes-list/31637206
     */
    private static int remapKeycode(int kc, char c) {
        switch(kc) {
            case Keyboard.KEY_BACK:   return 8;
            case Keyboard.KEY_DELETE: return 127;
            case Keyboard.KEY_RETURN: return 10;
            case Keyboard.KEY_ESCAPE: return 27;
            case Keyboard.KEY_LEFT:   return 37;
            case Keyboard.KEY_UP:     return 38;
            case Keyboard.KEY_RIGHT:  return 39;
            case Keyboard.KEY_DOWN:   return 40;
            case Keyboard.KEY_TAB:    return 9;
            case Keyboard.KEY_END:    return 35;
            case Keyboard.KEY_HOME:   return 36;
            case Keyboard.KEY_LSHIFT:
            case Keyboard.KEY_RSHIFT:   return 16;
            case Keyboard.KEY_LCONTROL:
            case Keyboard.KEY_RCONTROL:   return 17;
            case Keyboard.KEY_LMENU: // 其实是alt
            case Keyboard.KEY_RMENU:   return 18;

            default: return c;
        }
    }

    public void keyEventByKeyCode(int keyCode, char c, int mods, boolean pressed) {
        // we already processed the char in GuiView, so we don't need to do it again like MCEF does
        KeyEvent ev = new KeyEvent(dc_, pressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED, 0, mods, remapKeycode(keyCode, c), c);
        sendKeyEvent(ev);
    }

    @Override
    protected void finalize() throws Throwable {
        if(!isClosed()) {
            close(true); // NO FUCKING MEMORY LEAKS
        }
        super.finalize();
    }
}
