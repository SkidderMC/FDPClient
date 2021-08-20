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

package net.ccbluex.liquidbounce.ui.ultralight.support;

import com.labymedia.ultralight.UltralightView;
import com.labymedia.ultralight.databind.context.ContextProvider;
import com.labymedia.ultralight.databind.context.ContextProviderFactory;
import com.labymedia.ultralight.javascript.JavascriptContextLock;
import com.labymedia.ultralight.javascript.JavascriptValue;

import java.util.function.Consumer;

/**
 * This class is used in case Ultralight needs a Javascript context.
 */
public class ViewContextProvider implements ContextProvider {
    private final UltralightView view;

    /**
     * Constructs a new {@link ViewContextProvider} for the given view.
     *
     * @param view The view to use for retrieving the context.
     */
    private ViewContextProvider(UltralightView view) {
        this.view = view;
    }

    /**
     * This will be called whenever Ultralight needs the Javascript context for the view.
     * Typically this just means locking it and executing the callback. However, in multi-threaded environments
     * it is possible that the callbacks need to be moved to a different thread - as in, the one hosting Ultralight.
     *
     * @param callback The callback to execute
     */
    @Override
    public void syncWithJavascript(Consumer<JavascriptContextLock> callback) {
        // Move callback to the thread Ultralight is executing... in our case, this is the case
        // already, as we only use one thread.
        //
        // In a multithreaded environment this call should move the callback to the thread hosting Ultralight.
        // This call does not need to block even if the callback is executed asynchronously.
        try (JavascriptContextLock lock = view.lockJavascriptContext()) {
            callback.accept(lock);
        }
    }

    /**
     * Factory class for context providers.
     */
    public static class Factory implements ContextProviderFactory {
        private final UltralightView view;

        public Factory(UltralightView view) {
            this.view = view;
        }

        /**
         * The factory needs to bind a provider for a given Javascript value. The value can be used to identify
         * the view based on its handle.
         * <p>
         * For example, given an UltralightView {@code view} and a JavascriptValue {@code value}, it can be tested if
         * the value belongs to the view using:
         * <blockquote>
         * <pre>
         *         JavascriptValue value = ...;
         *         UltralightView view = ...;
         *
         *         try(JavascriptContextLock lock = view.lockContext()) {
         *             if(value.getLock().getContext().getGlobalContext().getHandle() == lock.getContext().getHandle()) {
         *                 System.out.println("The value belongs to the view!");
         *             } else {
         *                 System.out.println("The value belongs to another view!");
         *             }
         *         }
         *     </pre>
         * </blockquote>
         * By maintaining a map of context handles and their respective views it is possible to map back values to their
         * views. If you only have one view, you can simply always use it.
         *
         * @param value The value to bind a provider for
         * @return A context provider for the given value
         */
        @Override
        public ContextProvider bindProvider(JavascriptValue value) {
            // We only have one view, so we can ignore the value.
            // Else use the formula pointed at above to find a view for a given value.
            return new ViewContextProvider(view);
        }
    }
}
