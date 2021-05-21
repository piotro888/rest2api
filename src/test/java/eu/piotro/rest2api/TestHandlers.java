package eu.piotro.rest2api;

import eu.piotro.rest2api.api.annotations.RESTHandler;
import eu.piotro.rest2api.http.HTTPCodes;
import eu.piotro.rest2api.http.HTTPException;
import eu.piotro.rest2api.http.HTTPRequest;
import eu.piotro.rest2api.http.HTTPResponse;

import java.io.IOException;

public class TestHandlers {
    @RESTHandler(method = "GET", URI = "/d1")
    public HTTPResponse getRootDynamic(HTTPRequest r){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "OK");
    }

    @RESTHandler(method = "GET", URI = "/timeout")
    public HTTPResponse getTimeout(HTTPRequest r){
        try {
            Thread.sleep(6000);
        } catch (InterruptedException ignored) {}
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "OK");
    }

    @RESTHandler(method = "GET", URI = "/e500")
    public HTTPResponse getServerError(HTTPRequest r) throws IOException {
        throw new IOException();
    }

    @RESTHandler(method = "POST", URI = "/relay")
    public HTTPResponse postRelay(HTTPRequest r){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", r.getBody());
    }

    @RESTHandler(method = "GET", URI = "/static2")
    public HTTPResponse getStatic2(HTTPRequest r){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "OK");
    }

    @RESTHandler(method = "GET", URI = "/mm")
    public HTTPResponse getMM(HTTPRequest r){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "OK_GET");
    }

    @RESTHandler(method = "POST", URI = "/mm")
    public HTTPResponse postMM(HTTPRequest r){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "OK_POST");
    }

    @RESTHandler(method = "GET", URI = "/httpex")
    public HTTPResponse getHttpException(HTTPRequest r) throws HTTPException {
        throw new HTTPException(499, "499");
    }

    @RESTHandler(method = "GET", URI = "/")
    public static HTTPResponse staticRegisterCheck(HTTPRequest r) throws HTTPException {
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "");
    }

}
