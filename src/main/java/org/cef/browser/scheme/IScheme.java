package org.cef.browser.scheme;

/**
 * @author montoyo
 */
public interface IScheme {

    SchemePreResponse processRequest(String url);

    void getResponseHeaders(SchemeResponseHeaders resp);

    boolean readResponse(SchemeResponseData data);

}
