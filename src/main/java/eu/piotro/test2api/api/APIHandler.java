package eu.piotro.test2api.api;

import eu.piotro.test2api.http.*;

/**
 * API Handler interface for registering handlers
 * @see APIForwarder
 * @since 1.0
 */
public interface APIHandler {
    /**
     * HTTP request handler
     * @param request HTTP request to handle
     * @return HTTP response
     * @throws HTTPException quick way to throw HTTP errors like 4xx, 5xx.
     */
    HTTPResponse handle(HTTPRequest request) throws HTTPException;

    String JSON = "application/json";
}
