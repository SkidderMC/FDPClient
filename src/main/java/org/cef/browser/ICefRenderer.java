package org.cef.browser;

import java.awt.*;
import java.nio.ByteBuffer;

/**
 * @author Feather Client Team
 */
public interface ICefRenderer {
    void render(double x1, double y1, double x2, double y2);

    void destroy();

    void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender);

    void onPopupSize(Rectangle var1);

    void onPopupClosed();
}

