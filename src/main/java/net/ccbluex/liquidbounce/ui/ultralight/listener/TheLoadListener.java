/*
 * Ultralight Java - Java wrapper for the Ultralight web engine
 * Copyright (C) 2020 - 2021 LabyMedia and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.ccbluex.liquidbounce.ui.ultralight.listener;

import com.labymedia.ultralight.UltralightView;
import com.labymedia.ultralight.databind.Databind;
import com.labymedia.ultralight.databind.DatabindConfiguration;
import com.labymedia.ultralight.databind.api.JavaAPI;
import com.labymedia.ultralight.javascript.JavascriptContext;
import com.labymedia.ultralight.javascript.JavascriptContextLock;
import com.labymedia.ultralight.javascript.JavascriptGlobalContext;
import com.labymedia.ultralight.javascript.JavascriptObject;
import com.labymedia.ultralight.plugin.loading.UltralightLoadListener;
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine;
import net.ccbluex.liquidbounce.ui.ultralight.support.JSBridge;
import net.ccbluex.liquidbounce.ui.ultralight.support.ViewContextProvider;

/**
 * Example load listener for the main view.
 * <p>
 * Instances of load listeners receive various events regarding the load progress of a page.
 * <p>
 * A load listener is registered using {@link UltralightView#setLoadListener(UltralightLoadListener)}.
 */
public class TheLoadListener implements UltralightLoadListener {
    private final UltralightView view;
    private final Databind databind;
    private final JavaAPI javaApi;
    private final ViewContextProvider contextProvider;

    /**
     * Constructs a new {@link TheLoadListener} for the given view.
     *
     * @param view The view the listener is being constructed for
     */
    public TheLoadListener(UltralightView view) {
        this.view = view;

        contextProvider = new ViewContextProvider(view);

        // Create a databind instance so translation from Java to Javascript and back is possible
        databind = new Databind(
                DatabindConfiguration
                        .builder()
                        .contextProviderFactory(new ViewContextProvider.Factory(view))
                        .build()
        );

        // Construct a new JavaAPI instance, it is a simple class provided by ultralight-java-databind
        // for importing Java classes
        this.javaApi = new JavaAPI(databind);
    }

    /**
     * Helper function to construct a name for a frame from a given set of parameters.
     *
     * @param frameId     The id of the frame
     * @param isMainFrame Whether the frame is the main frame on the page
     * @param url         The URL of the frame
     * @return A formatted frame name
     */
    private String frameName(long frameId, boolean isMainFrame, String url) {
        return "[" + (isMainFrame ? "MainFrame" : "Frame") + " " + frameId + " (" + url + ")]: ";
    }

    /**
     * Called by Ultralight when a frame in a view beings loading.
     *
     * @param frameId     The id of the frame that has begun loading
     * @param isMainFrame Whether the frame is the main frame
     * @param url         The url that the frame started to load
     */
    @Override
    public void onBeginLoading(long frameId, boolean isMainFrame, String url) {
        UltralightEngine.INSTANCE.getLogger().info(frameName(frameId, isMainFrame, url) + "The view is about to load");
    }

    /**
     * Called by Ultralight when a frame in a view finishes loading.
     *
     * @param frameId     The id of the frame that finished loading
     * @param isMainFrame Whether the frame is the main frame
     * @param url         The url the frame has loaded
     */
    @Override
    public void onFinishLoading(long frameId, boolean isMainFrame, String url) {
        UltralightEngine.INSTANCE.getLogger().info(frameName(frameId, isMainFrame, url) + "The view finished loading");
    }

    /**
     * Called by Ultralight when a frame in a view fails to load.
     *
     * @param frameId     The id of the frame that failed to load
     * @param isMainFrame Whether the frame is the main frame
     * @param url         The url that failed to load
     * @param description A description of the error
     * @param errorDomain The domain that failed to load
     * @param errorCode   An error code indicating the error reason
     */
    @Override
    public void onFailLoading(
            long frameId, boolean isMainFrame, String url, String description, String errorDomain, int errorCode) {
        UltralightEngine.INSTANCE.getLogger().error(frameName(frameId, isMainFrame, url) +
                "Failed to load " + errorDomain + ", " + errorCode + "(" + description + ")");
    }

    /**
     * Called by Ultralight when the history of a view changes.
     */
    @Override
    public void onUpdateHistory() {
        UltralightEngine.INSTANCE.getLogger().info("The view has updated the history");
    }

    /**
     * Called by Ultralight when the window object is ready. This point can be used to inject Javascript.
     *
     * @param frameId     The id of the frame that the object became ready in
     * @param isMainFrame Whether the frame is the main frame
     * @param url         The url that the frame currently contains
     */
    @Override
    public void onWindowObjectReady(long frameId, boolean isMainFrame, String url) {
        try (JavascriptContextLock lock = view.lockJavascriptContext()) {
            // Retrieve the context and convert it to a global context
            JavascriptContext context = lock.getContext();
            JavascriptGlobalContext globalContext = context.getGlobalContext();

            // Retrieve the global object (the window object)
            JavascriptObject globalObject = globalContext.getGlobalObject();

            // Set the JavaAPI as a java on it
            // Javascript will now be able to access Java classes using "java.importClass('class name')"
            //
            // Of course you could set any other arbitrary Java object here and Javascript would be able to access it.
            //
            // You can also set Javascript values.
            globalObject.setProperty("java", databind.getConversionUtils().toJavascript(context, javaApi), 0);
            globalObject.setProperty("bridge", databind.getConversionUtils().toJavascript(context, new JSBridge(databind, contextProvider)), 0);
            globalObject.setProperty("view", databind.getConversionUtils().toJavascript(context, view), 0);
            globalObject.setProperty("engine", databind.getConversionUtils().toJavascript(context, UltralightEngine.INSTANCE), 0);
        }
    }

    /**
     * Called by Ultralight when the DOM is ready. This point can be used to inject Javascript.
     *
     * @param frameId     The id of the frame that the DOM became ready in
     * @param isMainFrame Whether the frame is the main frame
     * @param url         The url that the frame currently contains
     */
    @Override
    public void onDOMReady(long frameId, boolean isMainFrame, String url) {

    }
}
