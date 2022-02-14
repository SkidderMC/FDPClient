package org.cef.browser.scheme;

import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefResponse;

/**
 * @author montoyo
 */
public class SchemeResponseHeaders {

    private final CefResponse response;
    private final IntRef length;
    private final StringRef redirURL;

    public SchemeResponseHeaders(CefResponse r, IntRef l, StringRef url) {
        response = r;
        length = l;
        redirURL = url;
    }

    public void setMimeType(String mt) {
        response.setMimeType(mt);
    }

    public void setStatus(int status) {
        response.setStatus(status);
    }

    public void setStatusText(String st) {
        response.setStatusText(st);
    }

    public void setResponseLength(int len) {
        length.set(len);
    }

    public void setRedirectURL(String r) {
        redirURL.set(r);
    }
}

