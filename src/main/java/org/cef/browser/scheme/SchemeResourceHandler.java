package org.cef.browser.scheme;

import org.cef.callback.CefCallback;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

/**
 * @author montoyo
 */
public class SchemeResourceHandler extends CefResourceHandlerAdapter {

    private final IScheme scheme;

    public SchemeResourceHandler(IScheme scm) {
        scheme = scm;
    }

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        SchemePreResponse resp = scheme.processRequest(request.getURL());

        switch(resp) {
            case HANDLED_CONTINUE:
                callback.Continue();
                return true;

            case HANDLED_CANCEL:
                callback.cancel();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef response_length, StringRef redirectUrl) {
        scheme.getResponseHeaders(new SchemeResponseHeaders(response, response_length, redirectUrl));
    }

    @Override
    public boolean readResponse(byte[] data_out, int bytes_to_read, IntRef bytes_read, CefCallback callback) {
        return scheme.readResponse(new SchemeResponseData(data_out, bytes_to_read, bytes_read));
    }

    public static CefSchemeHandlerFactory build(IScheme scheme) {
        return (browser, frame, scheme_name, request) -> new SchemeResourceHandler(scheme);
    }
}
