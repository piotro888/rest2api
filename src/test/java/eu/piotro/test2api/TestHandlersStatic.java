package eu.piotro.test2api;

import eu.piotro.test2api.api.annotations.RESTHandler;
import eu.piotro.test2api.http.HTTPCodes;
import eu.piotro.test2api.http.HTTPRequest;
import eu.piotro.test2api.http.HTTPResponse;

public class TestHandlersStatic {
    @RESTHandler(method = "GET", URI = "/")
    public static HTTPResponse getRoot(HTTPRequest r) {
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "OK");
    }
}
