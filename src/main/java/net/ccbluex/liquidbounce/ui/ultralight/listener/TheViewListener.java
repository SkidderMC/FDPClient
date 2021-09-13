package net.ccbluex.liquidbounce.ui.ultralight.listener;

import com.labymedia.ultralight.UltralightView;
import com.labymedia.ultralight.input.UltralightCursor;
import com.labymedia.ultralight.math.IntRect;
import com.labymedia.ultralight.plugin.view.MessageLevel;
import com.labymedia.ultralight.plugin.view.MessageSource;
import com.labymedia.ultralight.plugin.view.UltralightViewListener;
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine;

/**
 * Example view listener for the main view.
 * <p>
 * Instances of view listeners receive view bound events and can be used to update visual aspects such as the cursor and
 * the tooltip.
 * <p>
 * A view listener is registered using {@link UltralightView#setViewListener(UltralightViewListener)}.
 */
public class TheViewListener implements UltralightViewListener {
    private final UltralightView view;

    public TheViewListener(UltralightView view){
        this.view=view;
    }

    /**
     * Called by Ultralight when the page title changes.
     *
     * @param title The new page title
     */
    @Override
    public void onChangeTitle(String title) {
        UltralightEngine.INSTANCE.getLogger().info("View title has changed: " + title);
    }

    /**
     * Called by Ultralight when the view URL changes.
     *
     * @param url The new page url
     */
    @Override
    public void onChangeURL(String url) {
        UltralightEngine.INSTANCE.getLogger().info("View url has changed: " + url);
    }

    /**
     * Called by Ultralight when the displayed tooltip changes.
     *
     * THIS WILL SPAM THE SAME MESSAGE
     *
     * @param tooltip The new page tooltip
     */
    @Override
    public void onChangeTooltip(String tooltip) {
//        UltralightEngine.INSTANCE.getLogger().info("View tooltip has changed: " + tooltip);
    }

    /**
     * Called by Ultralight when the cursor changes. Ultralight supports a lot of cursors, but currently not a custom
     * one.
     *
     * THIS WILL SPAM THE SAME MESSAGE
     *
     * @param cursor The new page cursor
     */
    @Override
    public void onChangeCursor(UltralightCursor cursor) {
//        UltralightEngine.INSTANCE.getLogger().info("View cursor has changed: " + cursor);
    }

    /**
     * Called when a message is added to the console. This includes, but is not limited to, {@code console.log} and
     * friends.
     *
     * @param source       The source the message originated from
     * @param level        The severity of the message
     * @param message      The message itself
     * @param lineNumber   The line the message originated from
     * @param columnNumber The column the message originated from
     * @param sourceId     The id of the source
     */
    @Override
    public void onAddConsoleMessage(
            MessageSource source,
            MessageLevel level,
            String message,
            long lineNumber,
            long columnNumber,
            String sourceId
    ) {
        UltralightEngine.INSTANCE.getLogger().info(
                "View message: ["
                        + source.name() + "/" + level.name() + "] "
                        + sourceId + ":" + lineNumber + ":" + columnNumber + ": " + message);
    }

    /**
     * Called by Ultralight when a new view is requested. This is your chance to either open the view in an external
     * browser, in a new application internal tab, or, if desired, to just ignore it entirely.
     *
     * @param openerUrl The URL of the page that initiated this request
     * @param targetUrl The URL that the new View will navigate to
     * @param isPopup   Whether or not this was triggered by window.open()
     * @param popupRect Popups can optionally request certain dimensions and coordinates via window.open(). You can
     *                  choose to respect these or not by resizing/moving the View to this rect.
     * @return The view to display the new URL in, or {@code null}, if the request should not be further handled by
     * Ultralight
     */
    @Override
    public UltralightView onCreateChildView(String openerUrl, String targetUrl, boolean isPopup, IntRect popupRect) {
        UltralightEngine.INSTANCE.getLogger().info("View wants child: ");
        UltralightEngine.INSTANCE.getLogger().info("\tFrom: " + openerUrl);
        UltralightEngine.INSTANCE.getLogger().info("\tTo: " + targetUrl);
        UltralightEngine.INSTANCE.getLogger().info("\tIs popup: " + isPopup);
        if (popupRect.isValid()) {
            UltralightEngine.INSTANCE.getLogger().info("\tTarget rect: (" +
                    popupRect.x() + "," + popupRect.y() + " -> " + popupRect.width() + "," + popupRect.height() + ")");
        } else {
            UltralightEngine.INSTANCE.getLogger().info("\tTarget rect: NONE");
        }
        UltralightEngine.INSTANCE.getLogger().info("Cancelling request, multi view not implemented");

        // Returning null will stop Ultralight from further handling the request, ignoring it altogether
        return null;
    }
}