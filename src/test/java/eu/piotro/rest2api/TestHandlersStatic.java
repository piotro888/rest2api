package eu.piotro.rest2api;

import eu.piotro.rest2api.api.annotations.RESTHandler;
import eu.piotro.rest2api.http.HTTPCodes;
import eu.piotro.rest2api.http.HTTPRequest;
import eu.piotro.rest2api.http.HTTPResponse;

import java.nio.charset.StandardCharsets;

public class TestHandlersStatic {
    @RESTHandler(method = "GET", URI = "/")
    public static HTTPResponse getRoot(HTTPRequest r) {
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "OK");
    }

    @RESTHandler(method = "GET", URI = "/utf")
    public static HTTPResponse getUtf(HTTPRequest r) {
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain;charset=utf-8", null, ".ą.", StandardCharsets.UTF_8);
    }
}
